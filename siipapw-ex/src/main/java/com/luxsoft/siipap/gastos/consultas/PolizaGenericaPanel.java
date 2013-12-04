package com.luxsoft.siipap.gastos.consultas;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;
import org.springframework.util.Assert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.gastos.GasActions;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.contabilidad.AsientoContable;
import com.luxsoft.siipap.model.contabilidad.Poliza;
import com.luxsoft.siipap.service.contabilidad.ExportadorGenericoDePolizas;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.FechaMayorAMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMenorAMatcher;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;

/**
 * Consulta generica de Polizas contables
 * 
 * 
 * @author Ruben Cancino
 *
 */
public  class PolizaGenericaPanel extends FilteredBrowserPanel<Poliza>{
	
	protected Periodo periodo;
	private String prefijoDeArchivo="D";
	
	protected Action generarPoliza;
	protected FechaMayorAMatcher mayor=new FechaMayorAMatcher();
	protected FechaMenorAMatcher menor=new FechaMenorAMatcher();
	protected Generador generador;
	
	
	@SuppressWarnings("unchecked")
	public PolizaGenericaPanel() {
		super(Poliza.class);
		init();
	}
	
	protected void init(){
		periodo=Periodo.periodoDeloquevaDelMes();
		mayor.getFechaField().setValue(periodo.getFechaInicial());
		menor.getFechaField().setValue(periodo.getFechaFinal());
		
		addProperty(new String[]{"fecha","tipo","concepto","debe","haber","cuadre","fecha","year","mes"});
		addLabels(  new String[]{"Fecha","tipo","concepto","Debe","Haber","Cuadre","fecha","year","mes"});
		
		installCustomMatcherEditor("Fecha Ini", mayor.getFechaField(), mayor);
		installCustomMatcherEditor("Fecha Fin", menor.getFechaField(), menor);
		installTextComponentMatcherEditor("A Favor", "afavor");
		installTextComponentMatcherEditor("Cuenta", "cuenta");
		installTextComponentMatcherEditor("Año", "year");
		installTextComponentMatcherEditor("Mes", "mes");
		
		//
		installTextComponentDetailMatcherEditor("CXPFactura", "descripcion3");
	}
	
	public void setGenerador(Generador generador) {
		this.generador = generador;
	}

	private void resolverPeriodo(){
		if(mayor.getCurrentDate()!=null)
			if(menor.getCurrentDate()!=null){
				periodo=new Periodo(mayor.getCurrentDate(),menor.getCurrentDate());
			}else{
				periodo=new Periodo(mayor.getCurrentDate());
			}
		else
			periodo=Periodo.periodoDeloquevaDelMes();	
	}
	
	public void load(){
		
		if(generador!=null){
			source.clear();
			SwingWorker<String, Poliza> worker=new SwingWorker<String, Poliza>(){			
				protected String doInBackground() throws Exception {
					resolverPeriodo();
					for(Date fecha:periodo.getListaDeDias()){
						try {
							Poliza pol=generador.generar(fecha);//ServiceLocator2.getComprasDeGastosManager().generarPolizaDeGastos(fecha);
							if(pol!=null)
								publish(pol);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
					return "OK";
				}
				@SuppressWarnings("unchecked")
				protected void process(List<Poliza> chunks) {
					for(Poliza p:chunks){
						source.add(p);
						
					}
				}			
				
			};
			worker.execute();
		}else{
			if(logger.isDebugEnabled()){
				logger.debug("Generando poliza en forma simple");
			}                            
			super.load();
		}
		
	}
	
	protected String getPrefijoDeArchivo(){
		return prefijoDeArchivo;
	}
	public void setPrefijoDeArchivo(String prefijoDeArchivo) {
		this.prefijoDeArchivo = prefijoDeArchivo;
	}
	
	protected TotalesHandler totalesHandler=new TotalesHandler();

	protected  JComponent buildContent(){
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setTopComponent(super.buildContent());
		sp.setBottomComponent(buildRegistrosPanel());
		sp.setOneTouchExpandable(true);
		sp.setResizeWeight(.6);
		grid.getSelectionModel().addListSelectionListener(totalesHandler);
		return sp;
	}
	
	protected JXTable detalleGrid;
	protected FilterList<AsientoContable> detallesFiltrados;
	
	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JComponent buildRegistrosPanel(){
		detalleGrid=ComponentUtils.getStandardTable();
		
		final String[] cols={"cuenta","concepto","descripcion","descripcion2","descripcion3","debe","haber","sucursal"};
		final String[] names={"Cuenta","Concepto","Descripción","Desc2","Desc3","Debe","Haber","Sucursal"};
		final TableFormat<AsientoContable> tf=GlazedLists.tableFormat(AsientoContable.class, cols,names);
		
		final Model<Poliza, AsientoContable> model=new Model<Poliza, AsientoContable>(){
			public List<AsientoContable> getChildren(Poliza parent) {
				return parent.getRegistros();
			}
		};
		final CollectionList<Poliza, AsientoContable> colList=new CollectionList<Poliza, AsientoContable>(getSelected(),model);
		detallesFiltrados=getDetailsFilterList(colList);
		final SortedList<AsientoContable> sortedAsientos=new SortedList<AsientoContable>(detallesFiltrados,null);
		sortedAsientos.addListEventListener(new TotalesPartidasHandler());
		final EventTableModel<AsientoContable> tm=new EventTableModel<AsientoContable>(sortedAsientos,tf);
		detalleGrid.setModel(tm);
		final EventSelectionModel<AsientoContable> selection=new EventSelectionModel<AsientoContable>(sortedAsientos);
		detalleGrid.setSelectionModel(selection);
		new TableComparatorChooser<AsientoContable>(detalleGrid,sortedAsientos,true);
		ComponentUtils.decorateActions(detalleGrid);
		final JScrollPane sp=new JScrollPane(detalleGrid);
		sp.setPreferredSize(new Dimension(200,350));
		return sp;
	}
	
	@SuppressWarnings("unchecked")
	protected FilterList getDetailsFilterList(final EventList detailList){
		//detailList.addListEventListener(new TotalesPartidasHandler());
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		CompositeMatcherEditor editor=new CompositeMatcherEditor(editors);
		for(MatcherEditor e:detailMatcherEditors){
			editors.add(e);
		}
		final FilterList filterList=new FilterList(detailList,editor);
		return filterList;
	}
	
	protected EventList<MatcherEditor> detailMatcherEditors=new BasicEventList<MatcherEditor>();
	protected Map<String, JComponent> detailMatcherComponents=new LinkedHashMap<String, JComponent>();
	 
	/**
	 * Instala  filtros de tipo texto para la lista de detalle
	 *  
	 * @param label
	 * @param propertyNames
	 */
	public void installTextComponentDetailMatcherEditor(final String label,String...propertyNames){
		Assert.notEmpty(propertyNames,"Debe indicar por lo menos una propiedad de filtrado");
		final JTextField tf=new JTextField(10);
		final TextFilterator filterator=GlazedLists.textFilterator(propertyNames);
		final TextComponentMatcherEditor editor=new TextComponentMatcherEditor(tf,filterator);
		detailMatcherEditors.add(editor);
		detailMatcherComponents.put(label, tf);
	}
	
	protected void installCustomComponentsInFilterPanel(DefaultFormBuilder builder){
		builder.appendSeparator("Detalles");
		for(Map.Entry<String, JComponent> entry:detailMatcherComponents.entrySet()){
			builder.append(entry.getKey(),entry.getValue());
		}
	}
	
	private JLabel totalDebe;
	private JLabel totalHaber;
	private JPanel totalesPanel;
	private JCheckBox porPoliza=new JCheckBox("",true);
	
	public JPanel getTotalesPanel(){
		if(totalesPanel==null){
			porPoliza.setOpaque(false);
			porPoliza.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					updateTotales();
				}				
			});
			totalDebe=new JLabel();
			totalDebe.setHorizontalAlignment(JLabel.RIGHT);
			totalHaber=new JLabel();
			totalHaber.setHorizontalAlignment(JLabel.RIGHT);
			FormLayout layout=new FormLayout("p,2dlu,f:50dlu:g","");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("General",porPoliza);
			builder.append("Debe:",totalDebe);
			builder.append("Haber: ",totalHaber);
			totalesPanel=builder.getPanel();
			totalesPanel.setOpaque(false);
		}
		return totalesPanel;
		
	}
	
	private void updateTotales(){
		CantidadMonetaria debe=CantidadMonetaria.pesos(0);
		CantidadMonetaria haber=CantidadMonetaria.pesos(0);
		if(porPoliza.isSelected()){
			for(Object o:getSelected()){
				Poliza pol=(Poliza)o;
				for(AsientoContable a:pol.getRegistros()){
					debe=debe.add(a.getDebe());
					haber=haber.add(a.getHaber());
				}
			}
		}else{
			for(AsientoContable a:detallesFiltrados){
				debe=debe.add(a.getDebe());
				haber=haber.add(a.getHaber());
			}
		}		
		totalDebe.setText(debe.toString());
		totalHaber.setText(haber.toString());
	}
	
	protected void generarPoliza(){
		if(!getSelected().isEmpty()){
			final ExportadorGenericoDePolizas manager=new ExportadorGenericoDePolizas();
			for(Object o:getSelected()){
				Poliza poliza=(Poliza)o;
				File file=manager.exportar(poliza,getPrefijoDeArchivo());
				if(file!=null){
					MessageUtils.showMessage("Poliza generada:\n"+file.getAbsolutePath(), "Poliza de gastos");
				}
			}
		}
	}
	
	public Action getGenerarPolizaAction(){
		if(generarPoliza==null){
			generarPoliza=new AbstractAction("generarPoliza"){
				public void actionPerformed(ActionEvent e) {
					generarPoliza();
				}				
			};
			CommandUtils.configAction(generarPoliza, GasActions.GenerarPolizaDeGastos.getId(), null);
		}
		return generarPoliza;
	}
	
	private class TotalesHandler implements ListSelectionListener{
		public void valueChanged(ListSelectionEvent e) {
			if(!e.getValueIsAdjusting()){
				updateTotales();
			}
		}
	}
	private class TotalesPartidasHandler implements ListEventListener{
		public void listChanged(ListEvent listChanges) {
			if(listChanges.hasNext()){
				System.out.println("Actualizando.."+listChanges);
				updateTotales();
			}
		}
	}
	
	/**
	 * Interfaz para facilitar la generacion individual de poilzas
	 * 
	 * @author Ruben Cancino
	 *
	 */
	public static interface Generador{
		
		public Poliza generar(Object...params );
		
	}

	
	
	
}
