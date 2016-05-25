package com.luxsoft.siipap.cxc.ui.consultas;

import java.awt.Dialog;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;

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
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.model.AntiguedadDeSaldo;
import com.luxsoft.siipap.cxc.model.CargoRow;
import com.luxsoft.siipap.cxc.ui.model.AntiguedadDeSaldosModel;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.reports.AntiguedadDeSaldoReportForm;
import com.luxsoft.siipap.reports.AntiguedadDeSaldosPorCteReport;
import com.luxsoft.siipap.reports.CargosNoCobradosCredito;
import com.luxsoft.siipap.reports.ClientesNuevosBI;
import com.luxsoft.siipap.reports.EdoDeMovCxc;
import com.luxsoft.siipap.reports.ExcepcionesEnDesctoReportForm;
import com.luxsoft.siipap.reports.ExcepcionesEnPrecioReportForm;
import com.luxsoft.siipap.reports.FacturasCanceladasBi;
import com.luxsoft.siipap.reports.FacturasCanceladasNCBI;
import com.luxsoft.siipap.reports.FacturasPendientes;
import com.luxsoft.siipap.reports.FacturasPendientesCamioneta;
import com.luxsoft.siipap.reports.FacturasPendientesCamionetaTab;
import com.luxsoft.siipap.reports.NotasPorDiaReport;
import com.luxsoft.siipap.reports.ProgramacionDeCobroSemanalReport;
import com.luxsoft.siipap.reports.ProyeccionDeCobranzaReport;
import com.luxsoft.siipap.reports.ResultadoDeCobranzaCxCReport;
import com.luxsoft.siipap.reports.SaldosPendienteXAbogadoConUltimoReport;
import com.luxsoft.siipap.reports.SaldosPendienteXAbogadoReportForm;
import com.luxsoft.siipap.reports.VentasDiariasBI;
import com.luxsoft.siipap.reports.VentasGlobalesBI;
import com.luxsoft.siipap.reports.VentasPorFacturistaBI;
import com.luxsoft.siipap.reports.VentasXSucReportForm;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.GridUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

public class AntiguedadDeSaldosPanel extends AbstractMasterDatailFilteredBrowserPanel<AntiguedadDeSaldo, CargoRow>{ 
//extends FilteredBrowserPanel<AntiguedadDeSaldo>{

	private final AntiguedadDeSaldosModel model;
	
	public AntiguedadDeSaldosPanel(AntiguedadDeSaldosModel model) {
		super(AntiguedadDeSaldo.class);
		this.model=model;
		source=GlazedListsSwing.swingThreadProxyList(this.model.getReportList());
	}	
	
	@Override
	protected void init(){
		addProperty("cliente","plazo","tipoVencimiento","limite","cuentasPorCobrar"	,"fechaMaxima"
				,"saldo","porVencer"
				,"vencido","vencido1_30","vencido31_60","vencido61_90","vencido90","participacion");
		
		addLabels("Cliente","Plazo","Vto","Limite","# F","Atraso Max"
				,"Saldo","Por Vencer"
				,"Vencido","D 1 a 30","D 31 a 60","D 61 a 90","Más de 90","Part(%)");
		installTextComponentMatcherEditor("Cliente", "cliente");
	}
	
	


	@Override
	protected TableFormat createDetailTableFormat() { 
		String props[]={"nombreRazon","sucursalName","tipo","documento","fecha","vencimiento","plazo","atraso","total","saldo"};
		String names[]={"Cliente","Suc","Tipo","Docto","Fecha","Vto","Plazo","Atraso","Total","Saldo"};
		return GlazedLists.tableFormat(CargoRow.class,props,names);
	}

	@Override
	protected Model<AntiguedadDeSaldo, CargoRow> createPartidasModel() {
		return new CollectionList.Model<AntiguedadDeSaldo, CargoRow>(){
			public List<CargoRow> getChildren(AntiguedadDeSaldo bean) {				
				 return bean.getCargos();
			}			
		};		
	}
	
	@Override
	protected Comparator getDefaultDetailComparator() {
		Comparator c=GlazedLists.beanPropertyComparator(CargoRow.class, "atraso");
		return GlazedLists.reverseComparator(c);
	}



	/*** Filtros para el detalle ***/
	
	private JTextField documentField=new JTextField(5);
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Documento",documentField);
	}	
	
	@Override
	protected EventList decorateDetailList( EventList data){
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		
		TextFilterator docFilterator=GlazedLists.textFilterator("documento");
		TextComponentMatcherEditor docEditor=new TextComponentMatcherEditor(documentField,docFilterator);
		editors.add(docEditor);
		
		CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		FilterList detailFilter=new FilterList(data,matcherEditor);
		return detailFilter;
	}
	
	protected void adjustMainGrid(final JXTable grid){
		grid.packAll();
	}
	
	@Override
	public JComponent buildDetailGridPanel(JXTable detailGrid){
		JScrollPane sp=new JScrollPane(detailGrid);
		JTabbedPane tp=new JTabbedPane();
		tp.addTab("Documentos", sp);
		tp.addTab("Distribución",ResourcesUtils.getIconFromResource("images2/chart_pie.png"),buildGraficaDeDistribucion());
		tp.addTab("Vencimientos",ResourcesUtils.getIconFromResource("images2/chart_bar.png"),buildGraficaDeDistribucion());
		tp.addTab("KPI",ResourcesUtils.getIconFromResource("images2/chart_curve.png"),buildGraficaDeDistribucion(),"Key Performance indicators");
		return tp;
	}

	public AntiguedadDeSaldosModel getModel() {
		return model;
	}

	public void load(){
		getLoadAction().setEnabled(false);
		SwingWorker<List<CargoRow>,String> worker=new SwingWorker<List<CargoRow>, String>(){
			@Override
			protected List<CargoRow> doInBackground() throws Exception {
				return model.findData();
			}
			@Override
			protected void done() {
				try {
					selectionModel.clearSelection();
					model.loadData(get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}finally{
					grid.packAll();
					getLoadAction().setEnabled(true);
				}
			}
			
			
		};
		TaskUtils.executeSwingWorker(worker);
	}

	
	
	public void open(){
		if(!source.isEmpty())
			grid.packAll();
		GridUtils.restorColumnsVisibility(grid,"antiguedadSaldosGrid");
	}
	
	public void close(){
		GridUtils.saveColumnsVisibility(grid,"antiguedadSaldosGrid");
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addAction(CXCActions.CXC_AntiguedadSaldos.getId(), "imprimir", "Reporte de Antigüedad")
				
				
				,addAction("", "reporteFacturasPendientes", "Facturas Pendientes Camioneta")
				,addAction("", "reporteFacturasPendientesTab", "Facturas Pendientes Camioneta Emb")
				,addAction("", "reporteVentasDiarias", "Ventas Diarias")
				,addAction("", "ventasMensuales", "Ventas Mensuales")
				,addAction("", "reporteVentasGlobales", "Ventas Globales")		
				,addAction("", "reporteFacsPorFacturista", "Ventas Por Facturista")
				,addAction("", "reporteFacturasCanceladas", "Facturas Canceladas")
				,addAction("", "reporteFacturasCancNC", "Facturas Canceladas NC")
				,addAction("", "reporteExcepcionesDescuento", "Excepciones en Descuento")
				,addAction("", "reporteExcepcionesPrecio", "Excepciones en Precio")
				,addAction("", "reporteClientesNuevos", "Clientes Nuevos")
				,addAction("", "reporteCargosNoCobrados", "Cargos No Cobrados De Credito")
				,addAction("", "reporteClientesSinVenta", "Reporte De Clientes Sin Venta")
				,addAction("", "reporteEstadoDeMovimientosCxc", "Estado de Mov. Cxc")
				,addAction("", "reporteAntiguedadDeSaldosCte", "Reporte de Antigüedad por Cte.")
				,addAction("", "reporteProgramacionDeCobro", "Reporte de Programación de Cobro")
				,addAction("", "reporteProyeccionDeCobranza", "Reporte de Proyección De Cobranza")
				,addAction("", "reporteResultadoDeCobranza", "Reporte de Resultado De Cobranza")
				,addAction("", "reporteClientesConAtrasoAviso", "Aviso de Clientes Con Atraso")			
				,addAction("", "reporteClientesConAtraso", "Clientes Suspendidos por Atraso")
				,addAction("", "reporteVentasPorVendedor", "Ventas Por Vendedor")
				,addAction("", "saldosPendientes", "Saldos Pendientes Por Abogado")
				,addAction("", "notasPorDia", "Notas de CreditoPorDia")
				
				};
		
		return actions;
	}
	
	public void notasPorDia(){
		NotasPorDiaReport report=new NotasPorDiaReport();
		report.run();
	}
	public void ventasMensuales() {
		VentasXSucReportForm.run();
	}
	
	public void reporteAntiguedadDeSaldosCte(){
		AntiguedadDeSaldosPorCteReport.run();
	}
	public void reporteProgramacionDeCobro(){
		ProgramacionDeCobroSemanalReport.run();
	}
	public void reporteResultadoDeCobranza(){
		ResultadoDeCobranzaCxCReport.run();
	}
	
	public void reporteProyeccionDeCobranza(){
		ProyeccionDeCobranzaReport.run();
	}
	
	/*public void reporteAntiguedadDeSaldosCte(){
		AntiguedadDeSaldosPorCteReport.run();
	}*/
	
	
	public void reporteClientesConAtrasoAviso(){
		
		
		ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/ClientesAvisoAtrasoCredito.jasper"),null);
	}
	
	public void reporteClientesConAtraso(){
		ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/ClientesSuspendidosCredito.jasper"),null);
	}
	
	
	public void reporteFacturasPendientes(){
		FacturasPendientesCamioneta.run();
	}
	public void reporteFacturasPendientesTab(){
		FacturasPendientesCamionetaTab.run();
	}
	
	public void reporteClientesNuevos(){
		ClientesNuevosBI.run();
	}
	
	public void reporteFacturasCanceladas(){
		FacturasCanceladasBi.run();
	}
	
	public void reporteFacturasCancNC(){
		FacturasCanceladasNCBI.run();
	}
	
	public void reporteFacsPorFacturista(){
		VentasPorFacturistaBI.run();
	}
	
	public void reporteVentasGlobales(){
		VentasGlobalesBI.run();
	}
	
	public void reporteVentasDiarias(){
		VentasDiariasBI.run();
	}
	
	public void reporteCargosNoCobrados(){
		CargosNoCobradosCredito.run();
	}
	
	public void reporteEstadoDeMovimientosCxc(){
		EdoDeMovCxc.run();
	}
	
	
	public void reporteExcepcionesPrecio(){
		ExcepcionesEnPrecioReportForm.run();
	}
	
	public void reporteExcepcionesDescuento(){
		ExcepcionesEnDesctoReportForm.run();
	}
	
	public void reporteClientesSinVenta(){
		
	}
	
	public void saldosPendientes(){
		SaldosPendienteXAbogadoConUltimoReport.run();;
	}
	
	
	
	public void imprimir(){
		AntiguedadDeSaldoReportForm action=new AntiguedadDeSaldoReportForm();
		action.execute();
		
	}
	
	/**
	 * Grafica para representar la distribucion de la cartera
	 * 
	 * @return
	 */
	public JComponent buildGraficaDeDistribucion(){
		return new JPanel();
	}
	
	public JComponent buildGraficaDeVencimientos(){
		return new JPanel();
	}
	
	public JComponent buildGraficaDeKPI(){
		return new JPanel();
	}
	
	private TotalesPanel totalPanel=new TotalesPanel();
	
	public JPanel getTotalesPanel(){
		return (JPanel)totalPanel.getControl();
	}
	
	
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel saldoTotal=new JLabel();
		private JLabel saldoVencido=new JLabel();
		private JLabel porVencer=new JLabel();
		private JLabel vencido1_30=new JLabel();
		private JLabel vencido31_60=new JLabel();
		private JLabel vencido61_90=new JLabel();
		private JLabel vencido91=new JLabel();

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:max(100dlu;p):g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			saldoTotal.setHorizontalAlignment(SwingConstants.RIGHT);
			saldoVencido.setHorizontalAlignment(SwingConstants.RIGHT);
			porVencer.setHorizontalAlignment(SwingConstants.RIGHT);
			vencido1_30.setHorizontalAlignment(SwingConstants.RIGHT);
			vencido31_60.setHorizontalAlignment(SwingConstants.RIGHT);
			vencido61_90.setHorizontalAlignment(SwingConstants.RIGHT);
			vencido91.setHorizontalAlignment(SwingConstants.RIGHT);
			
			builder.append("Saldo",saldoTotal);
			builder.append("Por Vencer",porVencer);
			builder.append("Vencido",this.saldoVencido);
			builder.append("1-30  Días",this.vencido1_30);
			builder.append("31-60 Días",this.vencido31_60);
			builder.append("61-90 Días",this.vencido61_90);
			builder.append(">90 Días",this.vencido91);
			
			builder.getPanel().setOpaque(false);
			getFilteredSource().addListEventListener(this);
			updateTotales();
			return builder.getPanel();
		}
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				
			}
			updateTotales();
		}
		
		public void updateTotales(){
			CantidadMonetaria saldo=CantidadMonetaria.pesos(0);
			CantidadMonetaria vencido=CantidadMonetaria.pesos(0);
			CantidadMonetaria porVencer=CantidadMonetaria.pesos(0);
			CantidadMonetaria d1_30=CantidadMonetaria.pesos(0);
			CantidadMonetaria d31_60=CantidadMonetaria.pesos(0);
			CantidadMonetaria d61_90=CantidadMonetaria.pesos(0);
			CantidadMonetaria d90=CantidadMonetaria.pesos(0);
			for(Object  r:getFilteredSource()){
				AntiguedadDeSaldo an=(AntiguedadDeSaldo)r;
				saldo=saldo.add(an.getSaldo());
				vencido=vencido.add(an.getVencido());
				porVencer=porVencer.add(an.getPorVencer());
				d1_30=d1_30.add(an.getVencido1_30());
				d31_60=d31_60.add(an.getVencido31_60());
				d61_90=d61_90.add(an.getVencido61_90());
				d90=d90.add(an.getVencido90());
				
			}
			String pattern="{0}  ({1})";
			
			saldoTotal.setText(MessageFormat.format(pattern, saldo.amount(),part(saldo,saldo)));
			this.saldoVencido.setText(MessageFormat.format(pattern, vencido.amount(),part(saldo,vencido)));
			this.porVencer.setText(MessageFormat.format(pattern, porVencer.amount(),part(saldo,porVencer)));
			this.vencido1_30.setText(MessageFormat.format(pattern, d1_30.amount(),part(saldo,d1_30)));
			this.vencido31_60.setText(MessageFormat.format(pattern, d31_60.amount(),part(saldo,d31_60)));
			this.vencido61_90.setText(MessageFormat.format(pattern, d61_90.amount(),part(saldo,d61_90)));
			this.vencido91.setText(MessageFormat.format(pattern, d90.amount(),part(saldo,d90)));
		}
		
		private NumberFormat nf=NumberFormat.getPercentInstance();
		
		private String part(final CantidadMonetaria total,final CantidadMonetaria part){
			
			double res=0;
			if(total.amount().doubleValue()>0){
				res=part.divide(total.amount()).amount().doubleValue();
			}
			return StringUtils.leftPad(nf.format(res),5);
		}

		
	}

	 
	
	
	
	
}
