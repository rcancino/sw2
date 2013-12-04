package com.luxsoft.siipap.pos.ui.consultas.caja;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.DateUtils;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.forms.FacturaForm;
import com.luxsoft.siipap.pos.ui.forms.caja.PagoForm;
import com.luxsoft.siipap.pos.ui.forms.caja.PagoFormModel;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeDisponibles;
import com.luxsoft.siipap.security.CancelacionDeCargoForm;
import com.luxsoft.siipap.security.CancelacionDeCargoFormModel;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;

public class CobranzaPagoContraEntrega extends FilteredBrowserPanel<Venta>{
	
	//private Sucursal sucursal;

	public CobranzaPagoContraEntrega() {
		super(Venta.class);
		//this.sucursal=Services.getInstance().getConfiguracion().getSucursal();
		
	}
	
	protected void init(){
		String[] props=new String[]{				
				"origen"
				,"pedido.entrega"				
				,"documento"
				,"numeroFiscal"
				,"fecha"
				,"clave"
				,"nombre"
				,"total"
				,"pagos"
				,"saldoCalculado"
				,"formaDePago"
				,"pedido.pagoContraEntrega.autorizo"
				,"facturista"
				,"cancelado"
				,"pedido.folio"
				};
		String[] names=new String[]{				
				"Origen"
				,"Entrega"				
				,"Docto"
				,"N.Fiscal"
				,"Fecha"
				,"Cliente"
				,"Nombre"
				,"Total"
				,"Pagos"
				,"Saldo"
				,"F.P."
				,"Responsable"
				,"Facturista"
				,"Cancelado"
				,"Pedido"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Origen", "origen");
		//installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("N.Fiscal", "numeroFiscal");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
	}

	@Override
	protected List<Venta> findData() {
		Date fecha=Services.getInstance().obtenerFechaDelSistema();
		String hql="from Venta v left join fetch v.pedido p where " +
				" p.pagoContraEntrega is not null" +
				" and (v.total-v.aplicado)>1 " +
				" and v.fecha<?";
		List<Venta> res= Services.getInstance()
			.getHibernateTemplate().find(hql,fecha);
		
		String hql2="from Venta v left join fetch v.pedido p where " +
		" p.pagoContraEntrega is not null" +
		" and v.fecha=?";
		List<Venta> res2= Services.getInstance()
		.getHibernateTemplate().find(hql2,fecha);
		
		res.addAll(res2);
		return res;
	}

	@Override
	public Action[] getActions() {		
		if(actions==null){
			actions=new Action[]{
				getLoadAction()
				,addRoleBasedContextAction(new FacturablePredicate(),POSRoles.CAJERO.name(), this, "cobrar", "Cobrar")
				,addRoleBasedContextAction(new FacturablePredicate(),POSRoles.CAJERO.name(), this, "cobrarConDeposito", "Cobrar con Deposito")
				,addRoleBasedContextAction(new CancelablePredicate(), POSRoles.CAJERO.name(), this, "cancelar", "Cancelar")
				,getViewAction()
				};
		}
		return actions;
	}
	
	public void cobrar(){
		if(getSelectedObject()==null)
			return;
		Sucursal sucursal=Services.getInstance().getConfiguracion().getSucursal();
		Venta selected=(Venta)getSelectedObject();
		Venta venta=getFactura(selected.getId());
		final BigDecimal porPagar=venta.getSaldoCalculado();
		if(selected.getFormaDePago()==null){
			JOptionPane.showMessageDialog(getControl() , "La venta no tiene registrado una forma de pago");
			return;
		}
		final PagoFormModel model=new PagoFormModel();
		model.getModel("formaDePago").addValueChangeListener(new PropertyChangeListener(){

			public void propertyChange(PropertyChangeEvent evt) {
				FormaDePago fp=(FormaDePago)evt.getNewValue();
				model.getPago().registrarImporte(porPagar);
			}
			
		});
		List<FormaDePago> TIPOS_DE_PAGO=new ArrayList<FormaDePago>();
		TIPOS_DE_PAGO.add(FormaDePago.EFECTIVO);
		TIPOS_DE_PAGO.add(FormaDePago.CHEQUE);
		if(venta.getPedido()!=null){
			if(venta.getPedido().getFormaDePago().name().equals(FormaDePago.TARJETA_CREDITO))
				TIPOS_DE_PAGO.add(FormaDePago.TARJETA_CREDITO);
			if(venta.getPedido().getFormaDePago().name().equals(FormaDePago.TARJETA_DEBITO))
				TIPOS_DE_PAGO.add(FormaDePago.TARJETA_DEBITO);
		}
		model.setFormasDePago(TIPOS_DE_PAGO.toArray(new FormaDePago[0]));
		model.setSucursal(sucursal);
		model.getPago().setCliente(selected.getCliente());
		model.getPago().registrarImporte(selected.getSaldoCalculado());
		final PagoForm form=new PagoForm(model);
		form.setSelectionDeCliente(false);
		form.setPersistirAlAplicar(false);
		form.open();
		
		if(!form.hasBeenCanceled()){
			int index=source.indexOf(selected);
			if(index!=-1){
				Pago pago=model.getPago().toPago();
				Date fecha=Services.getInstance().obtenerFechaDelSistema();
				//Venta venta=getFactura(selected.getId());
				Services.getInstance().getPagosManager().cobrarFactura(venta, pago,fecha);				
				Services.getInstance().getFacturasManager().generarAbonoAutmatico(Arrays.asList(venta));
				venta=getFactura(selected.getId());
				source.set(index, venta);
				selectionModel.clearSelection();
			}			
		}
		model.dispose();
	}
	
	public void cobrarConDeposito(){
		if(getSelectedObject()!=null){
			Venta selected=(Venta)getSelectedObject();
			int index=source.indexOf(selected);
			if(index!=-1){
				Abono pago=SelectorDeDisponibles.buscarPago(selected.getCliente());
				
				if(pago==null) return;
				Venta target=getFactura(selected.getId());
				Date fecha=Services.getInstance().obtenerFechaDelSistema();
				pago=Services.getInstance().getPagosManager().getAbono(pago.getId());
				Services.getInstance().getPagosManager().cobrarFactura(target, pago,fecha);
				Services.getInstance().getFacturasManager().generarAbonoAutmatico(Arrays.asList(target));
				target=getFactura(selected.getId());
				source.set(index, target);
			}
		}
		
	}
	
	private Venta getFactura(String id){
		return Services.getInstance().getFacturasManager().getFactura(id);
	}
	
	@Override
	protected void doSelect(Object bean) {
		Venta venta=(Venta)bean;
		FacturaForm.show(venta.getId());
	}
		
	public void cancelar(){
		if(getSelectedObject()==null)
			return;
		Venta factura=(Venta)getSelectedObject();
		Pedido pedido=factura.getPedido(); 
		if(factura.isCancelado())
			return;
		if(MessageUtils.showConfirmationMessage("Cancelar factura: "+factura.getDocumento(), "Cancelaciones")){
			int index=source.indexOf(factura);
			if(index!=-1 && validarCancelacion(factura)){
				final Date fecha=Services.getInstance().obtenerFechaDelSistema();
				CancelacionDeCargoFormModel model=new CancelacionDeCargoFormModel();
				model.setHibernateTemplate(Services.getInstance().getHibernateTemplate());
				CancelacionDeCargoForm form=new CancelacionDeCargoForm(model);
				form.open();
				if(!form.hasBeenCanceled()){
					factura=Services.getInstance().getFacturasManager().cancelarFactura(
							factura.getId()
							, fecha
							,model.getCancelacion().getUsuario()
							,model.getCancelacion().getComentario()
							);
					MessageUtils.showMessage("Factura cancelada:\n"+factura, "Cancelacion de ventas");
					source.set(index, factura);
					selectionModel.clearSelection();
					/*
					if(pedido!=null && MessageUtils.showConfirmationMessage("Eliminar el pedido:"+pedido.getFolio(), "Cancelaciones")){
						Services.getInstance().getPedidosManager().remove(pedido.getId());
					}*/
				}
			}			
		}
	}
	
	private boolean validarCancelacion(final Venta factura){
		Date fecha=Services.getInstance().obtenerFechaDelSistema();
		if(DateUtils.isSameDay(fecha, factura.getFecha())){
			return true;
		}else{
			JOptionPane.showMessageDialog(getControl(), "La factura no es del día","Validación",JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}
	
	private class FacturablePredicate implements Predicate{
		
		public boolean evaluate(Object bean) {
			Venta v=(Venta)bean;
			if(v!=null){
				return v.getSaldoCalculado().doubleValue()>0;
			}
			return false;
		}
	}
	
	public static class CancelablePredicate implements Predicate{
		public boolean evaluate(Object bean) {
			Venta v=(Venta)bean;
			if(v!=null){
				return !v.isCancelado();
			}
			return false;
		}
	}
	

}
