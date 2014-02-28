package com.luxsoft.siipap.pos.ui.cfdi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.JXTable;
import org.springframework.beans.factory.annotation.Autowired;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uif.panel.SimpleInternalFrame;
import com.luxsoft.cfdi.CFDIPrintUI;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.pos.POSActions;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.facturacion.FacturacionController;
import com.luxsoft.siipap.pos.facturacion.FacturacionController.CFDIVenta;
import com.luxsoft.siipap.pos.facturacion.FacturacionDeAnticiposController;
import com.luxsoft.siipap.pos.ui.reports.AplicacionAnticiposReport;
import com.luxsoft.siipap.pos.ui.reports.RelacionAnticiposReport;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.services.PedidosManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Panel para el mantenimiento y control de los procesos de Caja
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CFDIFacturacionCreditoPanel extends FilteredBrowserPanel<Pedido>{
	
	private CFDIVentasPanel facturasBrowser;
	
	
	
	@Autowired
	private FacturacionController facturacionController;
	@Autowired
	private FacturacionDeAnticiposController facturacionDeAnticiposController;

	public CFDIFacturacionCreditoPanel() {
		super(Pedido.class);
	}
	
	protected void init(){
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
				,"operador"
				,"formaDePago"
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
				,"Facturista"
				,"F.P."				
				,"Comentario"
				,"Comentario(Aut)"
				);
		
		installTextComponentMatcherEditor("Cliente", "clave","nombre");		
		installTextComponentMatcherEditor("Total", "total");
		manejarPeriodo();
	}
	
	@Override
	protected void installEditors(EventList editors) {
		super.installEditors(editors);
		MatcherEditor editor1=GlazedLists.fixedMatcherEditor(Matchers.beanPropertyMatcher(Pedido.class, "facturable", Boolean.TRUE));
		//MatcherEditor editor2=GlazedLists.fixedMatcherEditor(Matchers.beanPropertyMatcher(Pedido.class, "deCredito", Boolean.FALSE));
		editors.add(editor1);
		//editors.add(editor2);
	}
	
	@Override
	protected JComponent buildContent() {
		facturasBrowser=new CFDIVentasPanel(){
			@Override
			protected List<Venta> findData() {
				Date dia=Services.getInstance().obtenerFechaDelSistema();
				return Services.getInstance().getHibernateTemplate().find(
						"from Venta v left join fetch v.pedido p where v.origen=\'CRE\' and v.fecha=?", dia);
			}
			
		};
		facturasBrowser.getControl();
		JComponent parent=super.buildContent();
		
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setOneTouchExpandable(true);
		sp.setResizeWeight(.4);
		sp.setTopComponent(parent);
		sp.setBottomComponent(buildFacturasPanel());
		return sp;
	}
	
	public void open(){
		load();
	}
	
	protected JComponent buildFacturasPanel(){		
		JXTable grid=facturasBrowser.getGrid();
		JScrollPane sp=new JScrollPane(grid);
		ToolBarBuilder builder=new ToolBarBuilder();
		builder.add(facturasBrowser.getCancelAction());
		SimpleInternalFrame frame=new SimpleInternalFrame("Facturas generadas",builder.getToolBar(),sp);
		
		return frame;
	}

	@Override
	public Action[] getActions() {
		if(actions==null){
			
			Action buscarAction=addAction("buscar.id","buscar", "Buscar");
			buscarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/page_find.png"));
			
			List<Action> actions=ListUtils.predicatedList(new ArrayList<Action>(), PredicateUtils.notNullPredicate());
			actions.add(addRoleBasedContextAction(new FacturarPredicate(), POSRoles.CAJERO.name(),this, "generarVenta", "Generar venta"));
			actions.add(addAction(null, "reimprimir", "Re-Imprimir"));
			
			actions.add(buscarAction);
			actions.add(getLoadAction());
			actions.add(addAction(POSActions.GeneracionDePedidos.getId(),"regresarPendiente", "Regresar a Pendiente"));
			
			actions.add(addAction(POSActions.GeneracionDePedidos.getId(),"timbrar", "Timbrar CFDI"));
			
			actions.add(addAction(null,"generarVentaConAnticipo", "Venta con anticipo)"));
			actions.add(addRoleBasedContextAction(new FacturarPredicate(), POSRoles.CAJERO.name(),this, "generarVentaSinTimbrar", "Generar venta S/Timbrar"));
			this.actions=actions.toArray(new Action[actions.size()]);
		}
		return actions;
	}
	
	
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("", "reporteRelacionAnticiposFac", "Relacion de Anticipos"));
		procesos.add(addAction("", "reporteAplicacionAnticiposFac", "Aplicacion de Anticipos"));
		return procesos;
 		
	}
	
	public void reporteRelacionAnticiposFac() {
		RelacionAnticiposReport.run();
	}
	
	public void reporteAplicacionAnticiposFac() {
		AplicacionAnticiposReport.run();
	}
	
	private Action facturarAction;

	public Action getFacturarAction(){
		if(facturarAction==null){
			facturarAction=addContextAction(new Facturable(), POSRoles.FACTURISTA.name(), "facturar", "Facturar (Crédito)");
			facturarAction.putValue(Action.NAME, "Facturar (Crédito)");
			facturarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/money_dollar.png"));
		}
		return facturarAction;
	}

	public void buscar(){
		if(periodo==null)
			periodo=Periodo.getPeriodoDelMesActual();
		cambiarPeriodo();
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-5);
	}
	
	/**
	 * Necesario para evitar NPE por la etiqueta de periodo
	
	protected void updatePeriodoLabel(){
		//periodoLabel.setText("Periodo:" +periodo.toString());
	} */
	
	public void load(){
		super.load();
		facturasBrowser.load();
	}

	@Override
	protected List<Pedido> findData() {
		String hql="from Pedido p " +
				" where p.fecha between ? and  ?" +
				" 	and p.tipo='CREDITO' and p.sucursal.id=? " +
				"   and p.totalFacturado=0 and p.facturable=true";
		Object params[]={periodo.getFechaInicial(),periodo.getFechaFinal()
				,Services.getInstance().getConfiguracion().getSucursal().getId()
				};
		return Services.getInstance().getHibernateTemplate().find(hql,params);
		//return getManager().buscarFacturables(Services.getInstance().getConfiguracion().getSucursal(),Pedido.Tipo.CREDITO);
	}
	
	public void generarVenta(){
		if(getSelectedObject()!=null){
			Pedido pedido=(Pedido)getSelectedObject();
			pedido=getManager().get(pedido.getId());
			int index=source.indexOf(pedido);
			if(index!=-1){
				//facturacionController.facturarPedido(pedido);
				CFDIVenta cfdiVenta=facturacionController.generarVenta(pedido);
				if(cfdiVenta!=null){
					pedido=getManager().get(pedido.getId());
					source.set(index, pedido);
			
					timbrar(cfdiVenta);
					
					load();
				}
			}			
		}
	}
	
	public void generarVentaSinTimbrar(){
		if(getSelectedObject()!=null){
			Pedido pedido=(Pedido)getSelectedObject();
			pedido=getManager().get(pedido.getId());
			int index=source.indexOf(pedido);
			if(index!=-1){
				//facturacionController.facturarPedido(pedido);
				CFDIVenta cfdiVenta=facturacionController.generarVenta(pedido);
				if(cfdiVenta!=null){
					pedido=getManager().get(pedido.getId());
					source.set(index, pedido);
					
					load();
				}
			}			
		}
	}
	
	public void timbrar(){
		Venta venta=(Venta)this.facturasBrowser.getSelectedObject();
		if(venta==null)
			return;
		CFDI cfdi=Services.getCFDIManager().buscarCFDI(venta);
		
		if(cfdi.getTimbreFiscal().getUUID()!=null){
			MessageUtils.showMessage("CFDI ya generado para la venta UUID: "+cfdi.getTimbreFiscal().getUUID(), "CFDI");
			return;
		}
		CFDIVenta cfdiVenta=new CFDIVenta(venta, cfdi);
		timbrar(cfdiVenta);
	}
	
	
	
	
	public void timbrar(CFDIVenta cfdiVenta){
		try {
			logger.info("Timbrando CFDI: "+cfdiVenta.getCfdi());
			Services.getCFDIManager().timbrar(cfdiVenta.getCfdi());
			facturasBrowser.load();
			//Mandar imprimir
			imprimirJuegos(cfdiVenta);
		} catch (Exception e) {
			e.printStackTrace();
			MessageUtils.showMessage(ExceptionUtils.getRootCauseMessage(e), "Timbrado de CFDI");
			return;
		}
	}
	
	public void reimprimir(){
		Venta venta=(Venta)this.facturasBrowser.getSelectedObject();
		if(venta!=null){
			CFDI cfdi=Services.getCFDIManager().buscarCFDI(venta);
			CFDIVenta cfdiVenta=new CFDIVenta(venta, cfdi);
			imprimirJuegos(cfdiVenta);
		}
	}
	
	
	public void imprimirJuegos(CFDIVenta cfdiVenta){
		String[] tantos;
		if(cfdiVenta.getVenta().getOrigen().equals(OrigenDeOperacion.CRE)){
			tantos=new String[]{"CLIENTE","ARCHIVO"};
		}else
			tantos=new String[]{"CLIENTE","ARCHIVO"};
		Date time=Services.getInstance().obtenerFechaDelSistema();
		Venta venta=Services.getInstance().getFacturasManager().buscarVentaInicializada(cfdiVenta.getVenta().getId());
		for(String destino:tantos){
			CFDIPrintUI.impripirComprobante(
					venta
					, cfdiVenta.getCfdi()
					, destino
					,time,Services.getInstance().getHibernateTemplate()
					, false)
					;
		}
	}
	
	public void generarVentaConAnticipo(){
		String sel=JOptionPane.showInputDialog("Número de pedido:");
		if(StringUtils.isBlank(sel))
			return;
		Pedido pedido=getManager().buscarPorFolio(Long.valueOf(sel));
		if(pedido==null){
			MessageUtils.showMessage("No existe el pedido: "+sel, "Facturación con anticipo");
			return;
		}
		if(pedido.isFacturado()){
			MessageUtils.showMessage("Pedido ya facturado: "+sel, "Facturación con anticipo");
			return;
		}
		
		facturacionDeAnticiposController.facturarPedidoConAnticipo(pedido);
		//pedido=getManager().get(pedido.getId());
		facturasBrowser.load();
			
	}
	
	
	public void regresarPendiente(){
		List<Pedido> selected=new ArrayList<Pedido>(getSelected());
		for(Pedido p:selected){
			if(!p.isFacturable())
				continue;
			//if(p.isPorAutorizar())
				//continue;
			int index=source.indexOf(p);
			if(index!=-1){
				p=getManager().get(p.getId());				
				p.setFacturable(false);
				//p=getManager().save(p);
				p=(Pedido)Services.getInstance().getUniversalDao().save(p);
				source.set(index, p);
			}
		}
	}
	
	
	
	public Pedido getSelectedPedido(){
		return (Pedido)getSelectedObject();
	}
	public Venta getSelectedFactura(){
		return (Venta)this.facturasBrowser.getSelectedObject();
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
	
	private class Facturable implements Predicate{		
		public boolean evaluate(Object bean) {
			Pedido p=(Pedido)bean;
			if(p==null) return false;
			return getManager().isFacturable(p);
		}		
	}
}
