package com.luxsoft.siipap.pos.ui.consultas.caja;

import java.awt.Toolkit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.PredicateUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uif.panel.SimpleInternalFrame;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.pos.POSActions;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.facturacion.FacturacionController;
import com.luxsoft.siipap.pos.ui.reports.AplicacionDeSaldosReportForm;
import com.luxsoft.siipap.pos.ui.reports.ArqueoCaja;
import com.luxsoft.siipap.pos.ui.reports.CierreCaja;
import com.luxsoft.siipap.pos.ui.reports.CobranzaCamioneta;
import com.luxsoft.siipap.pos.ui.reports.CobranzaCamionetaPOS;
import com.luxsoft.siipap.pos.ui.reports.ControlDePagosCODReportForm;
import com.luxsoft.siipap.pos.ui.reports.FacturasCobradas;
import com.luxsoft.siipap.pos.ui.reports.FacturasPendientesCamioneta;
import com.luxsoft.siipap.pos.ui.reports.RelacionDeCheques;
import com.luxsoft.siipap.pos.ui.reports.SaldosAFavorReportForm;
import com.luxsoft.siipap.pos.ui.reports.VentasDiarias;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeDisponibles;
import com.luxsoft.siipap.pos.ui.venta.forms.PedidoFormView;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.CFDPrintServices;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.services.PedidosManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoPendiente;

/**
 * Panel para el mantenimiento y control de los procesos de Caja
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CajaPanel extends FilteredBrowserPanel<Pedido>{
	
	private FacturasPanel facturasBrowser;
	
	@Autowired
	private CajaController controller;
	
	@Autowired
	private FacturacionController facturacionController;
	
	//private Date fecha;

	public CajaPanel() {
		super(Pedido.class);
	}
	
	protected void init(){
		Comparator c1=GlazedLists.beanPropertyComparator(Pedido.class, "contraEntrega");
		c1=GlazedLists.reverseComparator(c1);
		Comparator c2=GlazedLists.beanPropertyComparator(Pedido.class, "folio");
		c1=GlazedLists.chainComparators(c1,c2);
		//Comparator c1=GlazedLists.beanPropertyComparator(Pedido.class, "contraEntrega","folio");
		setDefaultComparator(c1);
		addProperty(
				"folio"
				,"origen"
				,"entrega"
				,"estado"				
				,"fecha"
				,"clave"
				,"nombre"				
				,"moneda"
				,"total"
				,"contraEntrega"
				//,"totalFacturado"
				,"operador"
				,"formaDePago"
				,"puesto"
				,"comentario"
				,"pendienteDesc"
				);
		addLabels(
				"Folio"
				,"Venta"
				,"Entrega"
				,"Est"				
				,"Fecha"
				,"Cliente"
				,"Nombre"				
				,"Mon"
				,"Total"
				,"CE"
				//,"Facturado"
				,"Facturista"
				,"F.P."	
				,"Puesto"
				,"Comentario"
				,"Comentario(Aut)"
				);
		
		installTextComponentMatcherEditor("Origen", "origen");
		//installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Pedido", "folio");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		installTextComponentMatcherEditor("Total", "total");
		
	}
	
	@Override
	protected void installEditors(EventList editors) {
		super.installEditors(editors);
		MatcherEditor editor1=GlazedLists.fixedMatcherEditor(Matchers.beanPropertyMatcher(Pedido.class, "facturable", Boolean.TRUE));
		MatcherEditor editor2=GlazedLists.fixedMatcherEditor(Matchers.beanPropertyMatcher(Pedido.class, "deCredito", Boolean.FALSE));
		editors.add(editor1);
		editors.add(editor2);
	}
	
	@Override
	protected JComponent buildContent() {
		facturasBrowser=new FacturasPanel();
		facturasBrowser.getControl();
		JComponent parent=super.buildContent();
		
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setOneTouchExpandable(true);
		sp.setResizeWeight(.4);
		sp.setTopComponent(parent);
		sp.setBottomComponent(buildFacturasPanel());
		return sp;
	}
	
	
	public void triggerLoad(){
		if(isInitialized()){
			if(logger.isDebugEnabled()){
				logger.debug("Cargando datos..: "+new Date());
			}
			load();
		}		
	}
	
	protected JComponent buildFacturasPanel(){		
		JXTable grid=facturasBrowser.getGrid();
		JScrollPane sp=new JScrollPane(grid);
		ToolBarBuilder builder=new ToolBarBuilder();
		builder.add(facturasBrowser.getCancelAction());
		builder.add(facturasBrowser.getImprimirAction());
		
		SimpleInternalFrame frame=new SimpleInternalFrame("Facturas generadas",builder.getToolBar(),sp);
		
		return frame;
	}
	
	public JPanel getFilterPanel() {
		if(filterPanel==null){
			
			//filterPanel=getFilterPanelBuilder().getPanel();
			
			filterPanel=new JPanel(new VerticalLayout());
			
			filterPanel.add(getFilterPanelBuilder().getPanel());
			installFilters(filterPanelBuilder);
			filterPanelBuilder.appendSeparator("Facturas");
			filterPanel.add(facturasBrowser.getFilterPanel());
		}
		return filterPanel;
	}
	/*
	protected void installFilters(final DefaultFormBuilder builder){
		super.installFilters(builder);
		builder.appendSeparator("Facturas");
		builder.append(facturasBrowser.getFilterPanel());
	}
*/
	@Override
	protected void doSelect(Object bean) {
		Pedido pedido=(Pedido)bean;
		PedidoFormView.showPedido(pedido.getId());
	}

	@Override
	public Action[] getActions() {
		if(actions==null){
			
			Action buscarAction=addAction("buscar.id","buscar", "Buscar");
			buscarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/page_find.png"));
			
			List<Action> actions=ListUtils.predicatedList(new ArrayList<Action>(), PredicateUtils.notNullPredicate());
			
			//actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),this, "cobrar", "Cobrar"));
			actions.add(addRoleBasedContextAction(new FacturarPredicate(), POSRoles.CAJERO.name(),this, "facturar", "Facturar"));
			//actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "devolucionRmd", "Devolución"));
			actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),this, "cancelar", "Cancelar"));
			actions.add(buscarAction);
			actions.add(getLoadAction());
			actions.add(addAction(POSActions.GeneracionDePedidos.getId(),"regresarPendiente", "Regresar a Pendiente"));
			actions.add(addAction(POSRoles.CAJERO.name(),"consultarDisponibles", "Disponibles"));
			actions.add(addAction(POSRoles.CAJERO.name(), "generarCFD", "Generar factura (CFD)"));
			this.actions=actions.toArray(new Action[actions.size()]);
		}
		return actions;
	}
	
	
	
	@Override
	protected List<Action> createProccessActions() {		
		
		//Protecte our selves of null actions return by super class action factory methods
		List<Action> actions=ListUtils.predicatedList(new ArrayList<Action>(), PredicateUtils.notNullPredicate());
		//actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "arqueo", "Arqueo"));
		/*actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "corteDeCaja", "Corte"));		
		actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "generarFichasDeDeposito", "Fichas"));
		actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "registroDeGasto", "Gasto"));
		actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "cancelacionDeGasto", "Cancelar Gasto"));
		actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "rembolso", "Rembolso"));
		actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "morralla", "Morralla"));
		//actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "fondoFijo", "Fondo fijo"));
		actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "cambiarChequePorEfectivo", "Cambio de cheque"));*/
		actions.add(addAction("", "reporteFacturasPendientesCamioneta", "Facturas pendientes (CAM)"));
		actions.add(addAction("", "reporteRelacionDeCheques", "Relación de Cheques"));
		actions.add(addAction("", "reporteVentasDiarias", "Ventas diarias"));
		actions.add(addAction("", "reporteFacturasCobradas", "Facturas cobradas"));
		//actions.add(addAction("", "reporteCierreCaja", "Cierre"));
		//actions.add(addAction("", "reporteArqueoCaja", "Arqueo"));
		actions.add(addAction("", "reporteCobranzaCamioneta", "Cobranza (CAM)"));
		actions.add(addAction("", "aplicacionDeSaldos", "Aplicacion de Saldos"));
		actions.add(addAction("", "disponiblesSuc", "Saldos a Favor"));
		actions.add(addAction("", "reporteControlDePagosCOD", "Pago De Facturas COD"));
		
		
		 
		return actions;
	}

	public void buscar(){
		if(periodo==null)
			periodo=Periodo.getPeriodoDelMesActual();
		cambiarPeriodo();
	}
	
	/**
	 * Necesario para evitar NPE por la etiqueta de periodo
	 */
	protected void updatePeriodoLabel(){
		//periodoLabel.setText("Periodo:" +periodo.toString());
	}
	
	public void load(){
		super.load();
		facturasBrowser.load();
	}
	
	protected void beforeLoad(){
		super.beforeLoad();
		logger.info("Cargando pendientes...");
		
	}

	@Override
	protected List<Pedido> findData() {
		return getManager().buscarFacturables(Services.getInstance().getConfiguracion().getSucursal());
	}
	
	@Override
	protected void afterLoad() {
		super.afterLoad();
		logger.info("Load completed");
		if(sortedSource.size()>0){
			//mostrarAdvertencia();
		}
	}
	
	private JDialog warnningDialog;
	private JOptionPane warnningPane;
	
	public JDialog getWarnningDialog(){
		if(warnningDialog==null){			
			warnningDialog=warnningPane.createDialog(getControl(), "Pedidos pendientes");
		}
		return warnningDialog;
	}
	
	public void mostrarAdvertencia(){
		if(sortedSource.size()>0){
			if(warnningPane==null){
				warnningPane=new JOptionPane("Existen "+sortedSource.size()+ " pedidos pendientes de facturar",JOptionPane.INFORMATION_MESSAGE,JOptionPane.DEFAULT_OPTION);
			}
			
			warnningPane.setMessage("Existen "+sortedSource.size()+ " pedidos pendientes de facturar");
			getWarnningDialog().setVisible(true);
		}
	}
	
	public void facturar(){
		if(getSelectedPedido()!=null){
			Pedido target=getManager().get(getSelectedPedido().getId());
			facturacionController.facturarPedido(target);
			load();
		}	
	}
	
	public void generarCFD(){
		Venta venta=(Venta)this.facturasBrowser.getSelectedObject();
		if(venta!=null){
			venta=Services.getInstance().getFacturasManager().buscarVentaInicializada(venta.getId());
			ComprobanteFiscal cfd=Services.getInstance().getComprobantesDigitalManager().cargarComprobante(venta);
			if(cfd==null){
				cfd=Services.getInstance().getComprobantesDigitalManager().generarComprobante(venta);
				logger.info("CFD generado: "+cfd);
			}
			CFDPrintServices.imprimirFacturaEnMostrador(venta, cfd);	
			
		}
	}
	
	public void consultarDisponibles(){
		SelectorDeDisponibles.buscarPago();
		
	}
	
	public void cancelar(){
		controller.cancelar(getSelectedFactura());
	}
	
	public void regresarPendiente(){
		List<Pedido> selected=new ArrayList<Pedido>(getSelected());
		for(Pedido p:selected){
			if(!p.isFacturable())
				continue;
			
			int index=source.indexOf(p);
			if(index!=-1){
				p=getManager().get(p.getId());				
				final PedidoPendiente pendiente=p.getPendiente();				
				p.setPendiente(null);
				p.setFacturable(false);
				p=getManager().save(p);
				
				if(pendiente!=null){
					Services.getInstance().getUniversalDao().remove(PedidoPendiente.class, pendiente.getId());
				}
				source.set(index, p);
			}
		}
	}
	
	/** Reportes ***/
	
	public void reporteFacturasPendientesCamioneta(){
		FacturasPendientesCamioneta.run();
	}
	
	public void reporteControlDePagosCOD(){
		ControlDePagosCODReportForm.run();
	}
	
	public void reporteRelacionDeCheques(){
		RelacionDeCheques.run();
	}
	
	public void reporteVentasDiarias(){
		VentasDiarias.run();
	}
	
	public void reporteFacturasCobradas(){
		FacturasCobradas.run();
	}
	
	public void reporteCierreCaja(){
		CierreCaja.run();
	}
	
	public void reporteArqueoCaja(){
		ArqueoCaja.run();
	}
	
	public void reporteCobranzaCamioneta(){
		CobranzaCamioneta.run();
	}
	
	public void aplicacionDeSaldos() {
		AplicacionDeSaldosReportForm.run();
	}
	
	public void disponiblesSuc() {
		SaldosAFavorReportForm.run();
	}
	
	public Pedido getSelectedPedido(){
		return (Pedido)getSelectedObject();
	}
	public Venta getSelectedFactura(){
		return (Venta)this.facturasBrowser.getSelectedObject();
	}

	public CajaController getController() {
		return controller;
	}

	public void setController(CajaController controller) {
		this.controller = controller;
	}
	
	 

	private PedidosManager getManager(){
		return Services.getInstance().getPedidosManager();
	}
	
	/**
	 * Predicate para controlar la accion de facturar en funcion del pedido seleccionado
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	private class FacturarPredicate implements Predicate{
		public boolean evaluate(Object bean) {
			if(getSelectedPedido()!=null){
				if(getSelectedPedido().isFacturable())
					return getManager().isFacturable(getSelectedPedido());
			}
			return false;
		}
		
	}
	
	private Timer timer;
	
	TimerTask task=new TimerTask() {
		@Override
		public void run() {
			//System.out.println("Cargando datos en timer......");
			//load();
		}
	};
	
	@Override
	public void open() {
		//load();
		timer=new Timer();
		timer.schedule(task, 1000, 30000);
		
	}
	@Override
	public void close() {
		super.close();
		//System.out.println("Cancelando tarea de cargado en background..");
		task.cancel();
		timer.purge();
	}
	
	
}
