package com.luxsoft.siipap.inventario.ui.consultas;


import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.jfree.data.time.MovingAverage;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.inventario.ui.reports.KardexReportForm;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.inventarios.model.MovimientoDet.TipoCIS;
import com.luxsoft.siipap.reportes.BuscadorDeVentasEnvio;
import com.luxsoft.siipap.reportes.DetalleDeDevolucionesReport;
import com.luxsoft.siipap.reportes.MovInventDetalle;
import com.luxsoft.siipap.reportes.MovInventResumen;
import com.luxsoft.siipap.reportes.ReporteDeAlcancesForm;
import com.luxsoft.siipap.reportes.ReporteDeAlcancesPorCapasForm;
import com.luxsoft.siipap.reportes.TrasladoSalidasReport;
import com.luxsoft.siipap.reportes.VentasMAsMenosBI;
import com.luxsoft.siipap.reportes.VentasPorClienteDetalle;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;


/**
 * Panel para el mantenimiento de traslados
 * 
 * @author Ruben Cancino
 *
 */
public class MovimientosCentralizadosPanel extends AbstractMasterDatailFilteredBrowserPanel<Movimiento, MovimientoDet>{

	public MovimientosCentralizadosPanel() {
		super(Movimiento.class);
		setTitle("Movimientos generales");
		
	}
	
	protected void init(){		
		super.init();
		addProperty("sucursal.nombre","documento","fecha","concepto","porInventario","comentario");
		addLabels("Sucursal","Docto","Fecha","Concepto","Por Inv","Comentario");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Docto", "documento");		
		installTextComponentMatcherEditor("Tipo", "concepto");
		installTextComponentMatcherEditor("Comentario", "comentario");
		CheckBoxMatcher<Movimiento> porInvMatcher=new CheckBoxMatcher<Movimiento>(){
			@Override
			protected Matcher<Movimiento> getSelectMatcher(Object... obj) {
				return new Matcher<Movimiento>(){
					public boolean matches(Movimiento item) {
						if(item!=null)
							return item.getPorInventario();
						return false;
					}
				};
			}
		};
		installCustomMatcherEditor("Por Inventario", porInvMatcher.getBox(), porInvMatcher);
		manejarPeriodo();
	}
	
	private JTextField productoFilter=new JTextField(5);
	private JTextField sucursalFilter=new JTextField(5);
	private JTextField comentarioFilter=new JTextField(5);

	@Override
	protected void installDetailFilterComponents(DefaultFormBuilder builder) {		
		super.installDetailFilterComponents(builder);
		builder.append("Producto",productoFilter);
		builder.append("Sucursal",sucursalFilter);
		builder.append("Comentario",comentarioFilter);
		
	}
	
	@Override
	protected EventList decorateDetailList( EventList data){
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		
		final TextFilterator prodFilterator=GlazedLists.textFilterator("clave","descripcion");
		final TextComponentMatcherEditor prodEditor=new TextComponentMatcherEditor(productoFilter,prodFilterator);
		editors.add(prodEditor);
		
		final TextFilterator sucFilterator=GlazedLists.textFilterator("sucursal.nombre");
		final TextComponentMatcherEditor sucEditor=new TextComponentMatcherEditor(sucursalFilter,sucFilterator);
		editors.add(sucEditor);
		
		final TextFilterator comentarioFilterator=GlazedLists.textFilterator("comentario");
		final TextComponentMatcherEditor comentarioEditor=new TextComponentMatcherEditor(comentarioFilter,comentarioFilterator);
		editors.add(comentarioEditor);
		
		
		final CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		final FilterList detailFilter=new FilterList(data,matcherEditor);
		return detailFilter;
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"sucursal.nombre","fecha","clave"
				,"descripcion"
				,"cantidad"
				,"costoPromedio"
				,"costoPromedioMovimiento"
				,"kilosCalculados"
				,"comentario"
				,"tipoCis"				
				};
		
		String[] labels={"Sucursal","Fecha","Clave"
				,"Descripcion"
				,"Cantidad"
				,"Costop"
				,"Costo"
				,"Kilos"
				,"Comentario"
				,"Tipo (CIS)"				
			   	};
		return GlazedLists.tableFormat(MovimientoDet.class, props,labels);
	}

	@Override
	protected Model<Movimiento, MovimientoDet> createPartidasModel() {
		return new CollectionList.Model<Movimiento, MovimientoDet>(){
			public List<MovimientoDet> getChildren(Movimiento parent) {
				String hql="from MovimientoDet det where det.movimiento.id=?";
				return ServiceLocator2.getHibernateTemplate().find(hql, parent.getId());
			}
		};
	}


	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,CommandUtils.createPrintAction(this, "print")
				};
		return actions;
	}
	
	
 
	protected List<Action> createProccessActions(){
		List<Action> actions=new ArrayList<Action>();
		actions.add(addAction("", "kardex", "Kardex"));
		/*actions.add(addAction("", "reporteAnaliticoPorMovimiento", "Analítico por Mov."));
		actions.add(addAction("", "reporteDiscrepanciasDeInventario", "Discrepancias"));
		actions.add(addAction("", "reporteExistenciaInv", "Reporte de Existencias"));
		actions.add(addAction("", "reporteMaterialEnRecorte", "Recorte"));
		actions.add(addAction("", "reporteResumenDeMovimientos", "Resumen de Movs."));
		actions.add(addAction("", "asignarTipoCis", "Asignación Tipo CIS"));
		actions.add(addAction("", "reporteDeAlcancesPorCapas", "Alcance Por Capas"));*/
		actions.add(addAction("", "reporteVentasDeEnvio", "Facturas de Envio"));
		actions.add(addAction("", "reporteMovInventDetalle", "Movs. Invent. Detalle"));
		actions.add(addAction("", "reporteMovInventResumen", "Movs. Invent. Resumen"));
		actions.add(addAction("", "reporteDetalleDeDevoluciones", "Detalle de Devoluciones"));
		actions.add(addAction("", "reportedeVentasMasMenos", "Ventas de Mas/Menos"));
		actions.add(addAction("", "reporteSalidaTrasladoG", "Traslados Salidas "));
		actions.add(addAction("", "reporteVentasPorProducto", "Productos Por Venta "));
		
		return actions;
	}
	
	public void kardex(){
		KardexReportForm.run();
	}
	
	/*
	public void reporteDeAlcancesPorCapas(){
		ReporteDeAlcancesPorCapasForm.run();
	}
	
	
	
	public void reporteAnaliticoPorMovimiento(){
		//AnaliticoXMovimientoReportForm.runReport();
	}
	
	public void reporteDiscrepanciasDeInventario(){
		//Discrepancias.run();
		
	}
	
	public void reporteExistenciaInv(){
		//ExistenciasReportForm.run();
	}
	
	public void reporteMaterialEnRecorte() {
		//MaterialEnRecorteReportForm.run();		
	}
	
	public void reporteResumenDeMovimientos() {
		//ResumenDeMovReportForm.run();
		
	}*/
	

	public void reporteVentasDeEnvio() {
		new BuscadorDeVentasEnvio().actionPerformed(null);
		
	}
	
	public void reporteMovInventDetalle(){
		new MovInventDetalle().run();
	}
	
	public void reporteMovInventResumen(){
		new MovInventResumen().run();
	}

	public void reporteDetalleDeDevoluciones(){
		new DetalleDeDevolucionesReport().run();
	}
	
	public void reportedeVentasMasMenos(){
		new VentasMAsMenosBI().run();
	}

	public void reporteSalidaTrasladoG(){
		new TrasladoSalidasReport().run();
	}
	
	public void reporteVentasPorProducto(){
		new VentasPorClienteDetalle().run();
	}

	@Override
	protected List<Movimiento> findData() {
		String hql="from Movimiento m where  m.fecha between ? and ? ";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}
	
	public void print(){
		if(getSelectedObject()!=null){
			Movimiento m=(Movimiento)getSelectedObject();
			Map params=new HashMap();
			params.put("MOVI_ID", m.getId());
			ReportUtils.viewReport(ReportUtils.toReportesPath("invent/MovGenerico.jasper"), params);
		}
	}
	
	
	public void print(Movimiento m){
		Map params=new HashMap();
		params.put("MOVI_ID", m.getId());
		ReportUtils.viewReport(ReportUtils.toReportesPath("invent/MovGenerico.jasper"), params);
	}
	
	
	public void asignarTipoCis(){
		if(!detailSelectionModel.isSelectionEmpty()){
			Object res=JOptionPane.showInputDialog(
					getControl()
					, "Elija el tipo"
					, "Tipo de consumo interno"
					, JOptionPane.INFORMATION_MESSAGE
					, null
					, MovimientoDet.TipoCIS.values()
					, null
					);
			if(res!=null){
				TipoCIS tipo=(TipoCIS)res;
				List<MovimientoDet> selected=detailSelectionModel.getSelected();
				for(MovimientoDet mov:selected){
					if(!mov.getConcepto().equals("CIS"))
						continue;
					int index=this.detailSortedList.indexOf(mov);
					if(index!=-1){
						mov.setTipoCis(tipo);
						mov=(MovimientoDet)ServiceLocator2.getUniversalDao().save(mov);
						detailSortedList.set(index, mov);
					}
				}
			}
		}
	}
	
	
private TotalesPanel totalPanel=new TotalesPanel();
	
	public JPanel getTotalesPanel(){
		return (JPanel)totalPanel.getControl();
	}
	
	
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel importe=new JLabel();
		private JLabel kilos=new JLabel();
		
		

		@Override
		protected JComponent buildContent() {
			
			JPanel panel=new JPanel(new BorderLayout());
			final FormLayout layout=new FormLayout("p,1dlu,f:max(80dlu;p)","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			importe.setHorizontalAlignment(SwingConstants.RIGHT);
			kilos.setHorizontalAlignment(SwingConstants.RIGHT);
			
			
			
			builder.appendSeparator("Totales");
			builder.nextColumn();
			builder.append("Importe",importe);
			builder.append("Kilos", kilos);
					
			builder.getPanel().setOpaque(false);
			getFilteredSource().addListEventListener(this);
			
			updateTotales();
			
			return panel;
			//return builder.getPanel();
		}
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				
			}
			updateTotales();
		}
		
		public void updateTotales(){
			BigDecimal importe=BigDecimal.ZERO;
			BigDecimal kilos=BigDecimal.ZERO;
			
			
			
			int indice=1;
			/*for(Object  r:getFilteredSource()){
				MovimientoDet an=(MovimientoDet)r;
				an.setIndice(indice++);
			}*/
					
		/*	for(Object  r:getFilteredSource()){
				MovimientoDet an=(MovimientoDet)r;
				
			    
				importe=importe.add(an.getCosto());
				kilos=kilos.add(new BigDecimal(an.getKilos()));	
				
			} 
	*/
		
			
			String pattern="{0}";
			
			this.importe.setText(MessageFormat.format(pattern, importe));
			
			this.kilos.setText(MessageFormat.format(pattern, kilos));
			
		}
		
		
		
	}
	

}
