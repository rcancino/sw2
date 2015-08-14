package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;


/**
 * Mantenimiento y control de las polizas de ventas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public abstract class PanelGenericoDePolizasMultiples extends AbstractMasterDatailFilteredBrowserPanel<Poliza, PolizaDet>{

	
	private String clase;
	
	public PanelGenericoDePolizasMultiples() {
		super(Poliza.class);
	}

	@Override
	protected void agregarMasterProperties() {
		addProperty("id","tipo","clase","folio","fecha","referencia","debe","haber","cuadre","descripcion");
		addLabels("Id","Tipo","Clase","Folio","Fecha","Referencia","Debe","Haber","Cuadre","Descripción");
		installTextComponentMatcherEditor("Descripción", "descripcion");
		installTextComponentMatcherEditor("Referencia", "referencia");
		manejarPeriodo();
	}
	
	

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"poliza.folio","cuenta.clave","concepto","descripcion2","referencia","referencia2","debe","haber","asiento","tipo"};
		
		return GlazedLists.tableFormat(PolizaDet.class, props,getDetalleNames());
	}
	
	protected String[] getDetalleNames(){
		String[] names={
				"Poliza"
				,"Cuenta"
				,"Concepto"
				,"Descripción"
				,"Origen"
				,"Sucursal"
				,"Debe"
				,"Haber"
				,"Asiento"
				,"Tipo"
				};
		return names;
	}

	@Override
	protected Model<Poliza, PolizaDet> createPartidasModel() {
		return new Model<Poliza,PolizaDet>(){
			public List<PolizaDet> getChildren(Poliza parent) {
				if(parent.getId()==null)
					return parent.getPartidas();
				return getHibernateTemplate().find("from PolizaDet d where d.poliza.id=?",parent.getId());
			}
			
		};
	}
	
	protected void adjustDetailGrid(final JXTable grid){
		grid.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					selectIntoDetail();
			}			
		});
	}
	
	public void selectIntoDetail(){
		if(!detailSelectionModel.getSelected().isEmpty()){
			drill((PolizaDet)detailSelectionModel.getSelected().get(0));
		}
	}
	
	/**
	 * TemplateMethod para personalizar en las sub-clases el comportamiento del taladreo
	 * 
	 * @param det
	 */
	public void drill(PolizaDet det){		
		
	}
	
	public void salvar(){
		for(Object object:getSelected()){
			Poliza selected=(Poliza)object;
			int index=source.indexOf(selected);
			if(index!=-1){
				selected=salvar(selected);
				source.set(index, selected);
			}
		}
	}
	
	public Poliza salvar(Poliza poliza){
		//boolean existe=ServiceLocator2.getPolizasManager().existe(poliza);
		return ServiceLocator2.getPolizasManager().salvarPoliza(poliza);
	}
	
	

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()				
				,getInsertAction()
				,getDeleteAction()
				,CommandUtils.createPrintAction(this, "imprimirPoliza")
				,addAction(null, "salvar", "Salvar póliza")
												};
		return actions;
	}
	
	public abstract List<Poliza> generarPolizas(final Date fecha);

	public void insert(){
		final Date fecha=SelectorDeFecha.seleccionar();
		if(fecha!=null){
			final SwingWorker<List<Poliza>, String> worker=new SwingWorker<List<Poliza>, String>(){
				protected List<Poliza> doInBackground() throws Exception {
					return generarPolizas(fecha);
				}
				protected void done() {
					try {
						List<Poliza> res=get();
						System.out.println("Polizasa generadas: "+res.size());
						for(Poliza p:res){
							source.add(p);
							afterInsert(p);
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}				
			};
			executeLoadWorker(worker);
		}
	}


	//private JTextField cuentaField=new JTextField(5);
	private CuentasMatcherEditor cuentaEditor=new CuentasMatcherEditor();
	private JTextField concepto=new JTextField(5);
	private JTextField referencia1=new JTextField(5);
	private JTextField referencia2=new JTextField(5);
	private AsientoMatcherEditor asiento=new AsientoMatcherEditor();
	//private JTextField asiento=new JTextField(5);
	private JTextField tipo=new JTextField(5);
	private JTextField descripcion=new JTextField(5);
	
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Sucursal",referencia2);
		builder.append("Asiento",asiento.getField());
		builder.append("Cuenta",cuentaEditor.getField());
		builder.append("Tipo",tipo);
		builder.append("Origen ",referencia1);
		builder.append("Concepto ",concepto);
		builder.append("Descripción ",descripcion);
	}
	
	protected EventList<PolizaDet> partidasFiltered;
	protected EventList<PolizaDet> partidasSource;
	
	@Override
	protected EventList decorateDetailList( EventList data){
		partidasSource=data;
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		
		editors.add(new TextComponentMatcherEditor(referencia2,GlazedLists.textFilterator("referencia2")));
		//editors.add(new TextComponentMatcherEditor(asiento,GlazedLists.textFilterator("asiento")));
		editors.add(asiento);
		//TextComponentMatcherEditor cuentaEditor=new TextComponentMatcherEditor(cuentaField,GlazedLists.textFilterator("cuenta"));
		//cuentaEditor.setMode(TextMatcherEditor.EXACT);
		//editors.add(cuentaEditor);
		editors.add(cuentaEditor);
		editors.add(new TextComponentMatcherEditor(tipo,GlazedLists.textFilterator("tipo")));
		editors.add(new TextComponentMatcherEditor(referencia1,GlazedLists.textFilterator("referencia")));
		editors.add(new TextComponentMatcherEditor(concepto,GlazedLists.textFilterator("descripcion")));
		editors.add(new TextComponentMatcherEditor(descripcion,GlazedLists.textFilterator("descripcion2")));
		
		
		CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		FilterList detailFilter=new FilterList(data,matcherEditor);
		partidasFiltered=detailFilter;
		return detailFilter;
	}
	

	@Override
	protected List<Poliza> findData() {
		String hql="from Poliza p " +
				" where p.clase=? " +
				"   and DATE(p.fecha) between ? and ?";
		Object[] params={getClase(),periodo.getFechaInicial(),periodo.getFechaFinal()};
		return ServiceLocator2
			.getHibernateTemplate()
			.find(hql,params);
	}

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}

	public void imprimirPoliza(){
		if(getSelectedObject()!=null){
			imprimirPoliza((Poliza)getSelectedObject());
		}
	}
	
	
	public void imprimirPoliza(Poliza bean){
		Map params=new HashMap();
		params.put("ID", bean.getId());
		params.put("ORDEN", "");
		String path=ReportUtils.toReportesPath("contabilidad/Poliza.jasper");
		if(ReportUtils.existe(path))
			ReportUtils.viewReport(path, params);
		else
			JOptionPane.showMessageDialog(this.getControl()
					,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
	}
	
	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
			partidasFiltered.addListEventListener(totalPanel);
			this.detailSortedList.addListEventListener(totalPanel);
		}
		return (JPanel)totalPanel.getControl();
	}
	
	
	protected class TotalesPanel extends AbstractControl implements ListEventListener{
	
		private JLabel totalDebe;
		private JLabel totalHaber;
		private JLabel cuadre;
		private JCheckBox porPoliza=new JCheckBox(" Totales por plóliza",false);
		

		@Override
		protected JComponent buildContent() {
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
			cuadre=new JLabel();
			cuadre.setHorizontalAlignment(JLabel.RIGHT);
			FormLayout layout=new FormLayout("p,2dlu,f:50dlu:g","");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			//porPoliza.setEnabled(false);
			builder.append("General",porPoliza);
			builder.append("Debe:",totalDebe);
			builder.append("Haber: ",totalHaber);			
			builder.append("Cuadre: ",cuadre);
			builder.getPanel().setOpaque(false);			
			return builder.getPanel();
		}
		
		private void updateTotales(){
			CantidadMonetaria debe=CantidadMonetaria.pesos(0);
			CantidadMonetaria haber=CantidadMonetaria.pesos(0);
			if(porPoliza.isSelected()){
				for(Object o:getFilteredSource()){
					Poliza pol=(Poliza)o;
					debe=debe.add(CantidadMonetaria.pesos(pol.getDebe()));
					haber=haber.add(CantidadMonetaria.pesos(pol.getHaber()));
				}
			}else{
				for(PolizaDet a:partidasFiltered){
					debe=debe.add(a.getDebeCM());
					haber=haber.add(a.getHaberCM());
				}
			}
			
			totalDebe.setText(debe.toString());
			totalHaber.setText(haber.toString());
			cuadre.setText(debe.subtract(haber).toString());
		}
		
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.hasNext()){
				updateTotales();
			}
		}
		
		

	}

	public static class CuentasMatcherEditor extends AbstractMatcherEditor<PolizaDet> implements DocumentListener,ActionListener{
		
		private final JTextField textField;
		
		public CuentasMatcherEditor(){
			textField=new JTextField(10);
			textField.addActionListener(this);
			textField.getDocument().addDocumentListener(this);
		}
		
		public JTextField getField(){
			return textField;
		}

		public void changedUpdate(DocumentEvent e) {filter();}
		public void insertUpdate(DocumentEvent e) {filter();}
		public void removeUpdate(DocumentEvent e) {filter();}
		public void actionPerformed(ActionEvent e) {filter();}
		
		protected void filter(){
			if(StringUtils.isBlank(textField.getText()))
				fireMatchAll();
			else{
				fireChanged(new EXMatcher(textField.getText()));
			}
			
		}
		
		private class  EXMatcher implements Matcher<PolizaDet>{
			
			private final String[] cuentas;
			
			public EXMatcher(String cuentasString){
				
				this.cuentas=cuentasString.split(",");
			}
			
			public boolean matches(PolizaDet item) {
				
				return ArrayUtils.contains(cuentas,item.getCuenta().getClave());
			}
			
		}
		
		
	}
	

public static class AsientoMatcherEditor extends AbstractMatcherEditor<PolizaDet> implements DocumentListener,ActionListener{
		
		private final JTextField textField;
		
		public AsientoMatcherEditor(){
			textField=new JTextField(10);
			textField.addActionListener(this);
			textField.getDocument().addDocumentListener(this);
		}
		
		public JTextField getField(){
			return textField;
		}

		public void changedUpdate(DocumentEvent e) {filter();}
		public void insertUpdate(DocumentEvent e) {filter();}
		public void removeUpdate(DocumentEvent e) {filter();}
		public void actionPerformed(ActionEvent e) {filter();}
		
		protected void filter(){
			if(StringUtils.isBlank(textField.getText()))
				fireMatchAll();
			else{
				fireChanged(new ASMatcher(textField.getText()));
			}
			
		}
		
		private class  ASMatcher implements Matcher<PolizaDet>{
			
			private final String data;
			
			public ASMatcher(String pattern){
				this.data=StringUtils.substringBefore(pattern,"@");
			}
			
			public boolean matches(PolizaDet item) {
				return StringUtils.containsIgnoreCase(item.getAsiento(), data);
			}
			
		}
		
		
	}
	

protected HibernateTemplate getHibernateTemplate(){
	return ServiceLocator2.getHibernateTemplate();
}
	

}
