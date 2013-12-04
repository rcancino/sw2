package com.luxsoft.sw3.bi.consultas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.panel.SimpleInternalFrame;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;

import com.luxsoft.siipap.reportes.BajaEnVentas;
import com.luxsoft.siipap.reportes.ClientesSinVentas;
import com.luxsoft.siipap.reportes.ComparativoMejoresClientes;
import com.luxsoft.siipap.reportes.ComparativoVentasXLinea;
import com.luxsoft.siipap.reportes.ComparativoVentasXLineaXCte;
import com.luxsoft.siipap.reportes.LineaProdXCliente;
import com.luxsoft.siipap.reportes.LineasMejoresClientes;
import com.luxsoft.siipap.reportes.MejoresClientes;
import com.luxsoft.siipap.reportes.VentasPorCliente;
import com.luxsoft.siipap.reportes.VentasXLineaXDia;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.bi.BIVentasPorCliente;

public class BIVentasPorClientePanel extends FilteredBrowserPanel<BIVentasPorCliente>{

	private BIVentasPorClienteController controller;
	
	public BIVentasPorClientePanel() {
		super(BIVentasPorCliente.class);
		
	}
	
	protected void init(){
		controller=new BIVentasPorClienteController();
		addProperty(
				"clienteNombre"
				,"importeBruto"
				,"importeNeto"
				,"costo"
				,"utilidad"
				,"utitlidadPorcentual"
				,"kilos"
				,"participacion"
				,"participacionUtilidad"
				);
		addLabels(
				"Cliente"
				,"Venta Bruta"
				,"Venta Neta"
				,"Costo"
				,"Utilidad"
				,"Util(%)"
				,"Kilos"
				,"Part"
				,"Part Util"
				);
		
		installTextComponentMatcherEditor("Cliente", "clienteNombre");
		manejarPeriodo();
		Comparator c=GlazedLists.beanPropertyComparator(this.beanClazz, "importe");
		setDefaultComparator(GlazedLists.reverseComparator(c));
	}

	protected void manejarPeriodo(){
		periodo=controller.getPeriodo();
	}
	

	protected void nuevoPeriodo(Periodo p){
		controller.setPeriodo(p);
		this.periodo=controller.getPeriodo();
		load();
	}	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{getLoadAction()
				};
		return actions;
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {
		grid.getColumnExt("Venta Neta").setCellRenderer(Renderers.buildBoldDecimalRenderer(2));
		grid.getColumnExt("Utilidad").setCellRenderer(Renderers.buildBoldDecimalRenderer(2));
		grid.getColumnExt("Part").setCellRenderer(Renderers.getPorcentageRenderer(1));
		grid.getColumnExt("Part Util").setCellRenderer(Renderers.getPorcentageRenderer(1));
		grid.getColumnExt("Util(%)").setCellRenderer(Renderers.getPorcentageRenderer(1));
		
	}

	@Override
	protected List<BIVentasPorCliente> findData() {
		return controller.findData();
	}
	

	/*
	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={
				//"CARGO_ID",
				"sucursalNombre",
				"docto",
				"FECHA",
				"CLIENTE_ID",
				"ORIGEN",
				"IMPORTE_CORTES",
				"IMPORTE_BRUTO",
				"DESCUENTOS",
				"cargos",
				"FLETE",
				"importe",
				"impuesto",
				"total",
				"DEVOLUCION2",
				"BONIFICACION",
				"COSTO",
				"KILOS",
				"CANCELADO"
		};
		return GlazedLists.tableFormat(BIVenta.class, props,props);
	}
	*/	
	

	@Override
	protected void doSelect(Object bean) {
		BIVentasPorCliente bi=(BIVentasPorCliente)bean;
		controller.cargarDetalles(bi);
	}

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	
	
	@Override
	protected JComponent buildContent() {
		JComponent parent=super.buildContent();
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setOneTouchExpandable(true);
		sp.setResizeWeight(.4);
		sp.setTopComponent(parent);
		sp.setBottomComponent(buildDeailPanel());
		return sp;
	}
	
	private JTabbedPane tabPanel;
	
	public JComponent buildDeailPanel(){
		tabPanel=new JTabbedPane();
		
		BIVentasPorPeriodoPanel vpPanel=new BIVentasPorPeriodoPanel(controller.getVentasPorPeriodo());
		//SimpleInternalFrame vpFrame=new SimpleInternalFrame("Por periodo");
		//vpFrame.setContent(vpPanel.getControl());
		tabPanel.addTab("Por Periodo", vpPanel.getControl());
		return tabPanel;
	}



	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
			this.sortedSource.addListEventListener(totalPanel);
		}
		return (JPanel)totalPanel.getControl();
	}
	
	
	protected class TotalesPanel extends AbstractControl implements ListEventListener{
	
		private JLabel importe;
		private JLabel utilidad;

		@Override
		protected JComponent buildContent() {
			
			importe=new JLabel();
			importe.setHorizontalAlignment(JLabel.RIGHT);
			utilidad=new JLabel();
			utilidad.setHorizontalAlignment(JLabel.RIGHT);
			FormLayout layout=new FormLayout("p,2dlu,f:50dlu:g","");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Venta Neta:",importe);
			builder.append("Utilidad: ",utilidad);
			builder.getPanel().setOpaque(false);			
			return builder.getPanel();
		}
		
		private void updateTotales(){
			CantidadMonetaria importeNeto=CantidadMonetaria.pesos(0);
			CantidadMonetaria utilidad=CantidadMonetaria.pesos(0);
			for(Object o:sortedSource){
				BIVentasPorCliente a=(BIVentasPorCliente)o;
				importeNeto=importeNeto.add(CantidadMonetaria.pesos(a.getImporteNeto()));
				utilidad=utilidad.add(CantidadMonetaria.pesos(a.getUtilidad()));
				
			}
			importe.setText(importeNeto.toString());
			this.utilidad.setText(utilidad.toString());
			
			
		}
		
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.hasNext()){
				updateTotales();
			}
		}

	}

	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("", "reporteBajaEnVentas", "Baja en Ventas"));
		procesos.add(addAction("","reporteMejoresClientes", "Mejores Clientes"));
		procesos.add(addAction("", "reporteVentasPorCliente", "Ventas Por Cliente"));
		procesos.add(addAction("", "reporteClientesSinVenta", "Clientes Sin Venta"));
		procesos.add(addAction("", "reporteComparativoMejoresClientes", "Comparativo Mejores Clientes"));
		procesos.add(addAction("", "reporteLineasMejoresClientes", "Mejores Clientes Por Linea"));
		procesos.add(addAction("", "reporteLineasXProdCliente", "Ventas Cliente Por Linea "));
		procesos.add(addAction("", "reporteComparativoLineas", "Comparativo Ventas Por Linea "));
		procesos.add(addAction("", "reporteVentasXLineaXDia", "Ventas Por Linea Por Dia "));
		procesos.add(addAction("", "reporteComparativoLineasporCliente", "Comparativo Ventas Por Linea Por Cte"));
		return procesos;
	}
	
	public void reporteBajaEnVentas(){
		BajaEnVentas report= new BajaEnVentas();
		report.run();
	}
	
	public void reporteMejoresClientes(){
		MejoresClientes report= new  MejoresClientes();
		report.run();
	}
	public void reporteVentasPorCliente(){
		VentasPorCliente report= new  VentasPorCliente();
		report.run();
	}
	public void reporteClientesSinVenta(){
		ClientesSinVentas report= new ClientesSinVentas ();
		report.run();
	}
	public void reporteComparativoMejoresClientes(){
		ComparativoMejoresClientes report =new ComparativoMejoresClientes();
		report.run();
	}
	
	public void reporteLineasMejoresClientes(){
		LineasMejoresClientes report=new LineasMejoresClientes();
		report.run();
	}

	public void reporteLineasXProdCliente(){
		LineaProdXCliente report =new LineaProdXCliente();
		report.run();
	}
	
	public void reporteComparativoLineas(){
		ComparativoVentasXLinea report=new ComparativoVentasXLinea();
		report.run();
			
	}
	
	public void reporteComparativoLineasporCliente(){
		ComparativoVentasXLineaXCte report=new ComparativoVentasXLineaXCte();
		report.run();
			
	}
	
	public void reporteVentasXLineaXDia(){
		VentasXLineaXDia report=new VentasXLineaXDia();
		report.run();
			
	}
	

}
