package com.luxsoft.sw3.impap.ui.consultas;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uif.panel.SimpleInternalFrame;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.ventas.PedidosManager;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.impap.services.CFDPrintServices;
import com.luxsoft.sw3.impap.ui.controllers.FacturacionController;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Panel para el mantenimiento y control de los procesos de Caja
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class FacturacionCreditoPanel extends FilteredBrowserPanel<Pedido>{
	
	private FacturasPanel facturasBrowser;
	

	public FacturacionCreditoPanel() {
		super(Pedido.class);
		setTitle("Pedidos");
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
				,"totalFacturado"
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
				,"Facturado"
				,"Facturista"
				,"F.P."				
				,"Comentario"
				,"Comentario(Aut)"
				);
		
		installTextComponentMatcherEditor("Cliente", "clave","nombre");		
		installTextComponentMatcherEditor("Total", "total");
		manejarPeriodo();
		
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-90);
	}	
	
	@Override
	protected JComponent buildContent() {
		facturasBrowser=new FacturasPanel(){

			@Override
			protected List<Venta> findData() {
				return ServiceLocator2.getHibernateTemplate().find(
						"from Venta v left join fetch v.pedido p where " +
						" v.origen=\'CRE\' and date(v.fecha) between ? and ?"
						,new Object[]{FacturacionCreditoPanel.this.periodo.getFechaInicial()
								,FacturacionCreditoPanel.this.periodo.getFechaFinal()}
						);
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
		SimpleInternalFrame frame=new SimpleInternalFrame("Facturas",builder.getToolBar(),sp);
		
		return frame;
	}

	@Override
	public Action[] getActions() {
		if(actions==null){
			
			Action buscarAction=addAction("buscar.id","buscar", "Buscar");
			buscarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/page_find.png"));
			
			List<Action> actions=ListUtils.predicatedList(new ArrayList<Action>(), PredicateUtils.notNullPredicate());
			actions.add(addRoleBasedContextAction(null, null,this, "facturar", "Facturar"));
			actions.add(buscarAction);
			actions.add(getLoadAction());
			//actions.add(addAction(null,"regresarPendiente", "Regresar a Pendiente"));
			
			actions.add(addAction(null,"generarCFD", "Generar factura (CFD)"));
			
			
			this.actions=actions.toArray(new Action[actions.size()]);
		}
		return actions;
	}
	
	private Action facturarAction;

	public Action getFacturarAction(){
		if(facturarAction==null){
			facturarAction=addContextAction(new Facturable(), null, "facturar", "Facturar (Crédito)");
			facturarAction.putValue(Action.NAME, "Facturar ");
			facturarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/money_dollar.png"));
		}
		return facturarAction;
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

	@Override
	protected List<Pedido> findData() {
		String hql="from Pedido p where p.fecha between ? and ? ";
		return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}
	
	public void generarCFD(){
		Venta venta=(Venta)this.facturasBrowser.getSelectedObject();
		if(venta!=null){
			boolean res=MessageUtils.showConfirmationMessage("Generar CFD para factura:"+venta.getDocumento(), "Facturación");
			if(res)
				generarCFD(venta);
		}
	}
	
	public void generarCFD(Venta...ventas){
		for(Venta venta:ventas){
			venta=ServiceLocator2.getVentasManager().buscarVentaInicializada(venta.getId());
			ComprobanteFiscal cfd=ServiceLocator2.getCFDManager().cargarComprobante(venta);
			if(cfd==null){
				cfd=ServiceLocator2.getCFDManager().generarComprobante(venta);
				logger.info("CFD generado: "+cfd);
			}			
			CFDPrintServices.impripirComprobante(venta, cfd, null, true);
		}
	}
	
	public void facturar(){
		if(getSelectedObject()!=null){
			Pedido pedido=(Pedido)getSelectedObject();
			pedido=getManager().get(pedido.getId());
			if(!isFacturable(pedido)){
				MessageUtils.showMessage("Pedido ya facturado","Facturación");
			}else{
				int index=source.indexOf(pedido);
				if(index!=-1){
					List<Venta> facturas=FacturacionController.getInstance().facturar(pedido);
					if(facturas.size()>0){
						pedido=getManager().get(pedido.getId());
						source.set(index, pedido);						
						facturasBrowser.getSource().addAll(facturas);
						boolean res=MessageUtils.showConfirmationMessage("Facturas generadas:\n "
								+facturas+"\n Desea generar comprobante fiscal?"
								, "Facturación");
						if(res){
							generarCFD(facturas.toArray(new Venta[0]));
						}
					}
				}
			}
						
		}
	}
	
	/*
	public void regresarPendiente(){
		List<Pedido> selected=new ArrayList<Pedido>(getSelected());
		for(Pedido p:selected){
			if(!p.isFacturable())
				continue;
			int index=source.indexOf(p);
			if(index!=-1){
				p=getManager().get(p.getId());				
				p.setFacturable(false);
				p=(Pedido)ServiceLocator2.getUniversalDao().save(p);
				source.set(index, p);
			}
		}
	}
	*/
	
	
	public Pedido getSelectedPedido(){
		return (Pedido)getSelectedObject();
	}
	public Venta getSelectedFactura(){
		return (Venta)this.facturasBrowser.getSelectedObject();
	}

	
	private PedidosManager getManager(){
		return ServiceLocator2.getPedidosManager();
	}
	
	public boolean isFacturable(final Pedido pedido){
		if(pedido.isFacturado())
			return false;
		if(pedido.isPorAutorizar())
			return pedido.getAutorizacion()!=null;
		if(pedido.getTotal().doubleValue()<=0)
			return false;
		if(StringUtils.containsIgnoreCase(pedido.getComentario2(), "CANCELADO"))
			return false;		
		return true;
	}
	
	
	
	private class Facturable implements Predicate{		
		public boolean evaluate(Object bean) {
			Pedido p=(Pedido)bean;
			if(p==null) return false;
			return isFacturable(p);
		}		
	}
}
