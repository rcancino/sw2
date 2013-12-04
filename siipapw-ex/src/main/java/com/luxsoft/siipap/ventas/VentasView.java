package com.luxsoft.siipap.ventas;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ca.odell.glazedlists.matchers.Matcher;

import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swx.GlobalActions;
import com.luxsoft.siipap.ventas.model.VentaRow;


/**
 * Panel con la lista de ventas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class VentasView extends FilteredBrowserPanel<VentaRow>{

	private VentasMainModel model;
	private PedidosEditor pedidoEditor;
	private FacturadoEditor facturadoEditor;
	
	private Action cancelarAction;
	private Action facturarAction;
	
	
	public VentasView() {
		super(VentaRow.class);
		addProperty("id","sucursal","nombre","fecha","factura","estado","origen","tipo","total");
		addLabels("Id","Sucursal","Nombre","Fecha","CXPFactura","Estado","Origen","Tipo","Total");
				
		//installTextComponentMatcherEditor("Sucursal", new String[]{"sucursal"});		
		installTextComponentMatcherEditor("Cliente", new String[]{"nombre"});
		pedidoEditor=new PedidosEditor();
		facturadoEditor=new FacturadoEditor();
		installCustomMatcherEditor("Pedidos", pedidoEditor.getBox(), pedidoEditor);
		installCustomMatcherEditor("Facturados", facturadoEditor.getBox(), facturadoEditor);
	}
	
	protected Action getCancelarAction(){
		if(cancelarAction==null){
			cancelarAction=new AbstractAction("cancelar"){
				public void actionPerformed(ActionEvent e) {
					cancelar();
				}				
			};
		}
		CommandUtils.configAction(cancelarAction, "cancelarFacturas", null);
		return cancelarAction;
	}
	
	protected Action getFacturarAction(){
		if(facturarAction==null){
			facturarAction=new AbstractAction("facturar"){
				public void actionPerformed(ActionEvent e) {
					facturar();
				}
			};
		}
		CommandUtils.configAction(facturarAction, "facturarPedidos",null);		
		return facturarAction;
	}
	
	protected void cancelar(){
		
	}
	protected void facturar(){
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Action[] getActions() {
		if(actions==null){			
			actions=new Action[]{
					getLoadAction()
					,getViewAction()					
					,getFacturarAction()
					,getCancelarAction()					
					,getDeleteAction()
					};
			ajustarAcciones();
		}
		return actions;
	}
	
	private void ajustarAcciones(){
		viewAction.putValue(Action.NAME, "Consultar");
		CommandUtils.configAction(deleteAction, GlobalActions.EliminarPedidos.getId(), "");
	}
	
	@Override
	protected List<VentaRow> findData() {
		return model.getVentasSource();
	}

	public VentasMainModel getModel() {
		return model;
	}
	public void setModel(VentasMainModel model) {
		this.model = model;
	}

	private class PedidosEditor extends CheckBoxMatcher<VentaRow>{
		@Override
		protected Matcher<VentaRow> getSelectMatcher(Object... obj) {
			return new PedidoMatcher();
		}		
		class PedidoMatcher implements Matcher<VentaRow>{
			public boolean matches(VentaRow item) {
				//return item.getEstado().equals(Estado.P);
				return false;
			}			
		}		
	}
	
	private class FacturadoEditor extends CheckBoxMatcher<VentaRow>{
		@Override
		protected Matcher<VentaRow> getSelectMatcher(Object... obj) {
			return new PedidoMatcher();
		}
		
		class PedidoMatcher implements Matcher<VentaRow>{
			public boolean matches(VentaRow item) {
				//return item.getEstado().equals(Estado.F);
				return false;
			}			
		}		
	}
	

}
