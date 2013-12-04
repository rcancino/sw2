package com.luxsoft.siipap.pos.ui.consultas.caja;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

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
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model2.VentaContraEntrega;
import com.luxsoft.sw3.services.Services;

public class CobranzaPagoContraEntregaPanel extends FilteredBrowserPanel<VentaContraEntrega>{
	
	private Sucursal sucursal;

	public CobranzaPagoContraEntregaPanel() {
		super(VentaContraEntrega.class);
		//this.sucursal=Services.getInstance().getConfiguracion().getSucursal();
		
	}
	
	protected void init(){
		String[] props=new String[]{				
				"documento"
				,"fecha"
				,"nombre"
				,"total"
				,"saldo"
				,"atraso"
				,"fpago"
				,"facturista"
				,"pedido"
				,"embarque"
				,"chofer"
				,"salida"
				,"regreso"
				,"asignacion"
				,"retrasoAsignacion"
				};
		String[] names=new String[]{				
				
				};
		addProperty(props);
		//addLabels(names);
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("Cliente", "nombre");
	}

	@Override
	protected List<VentaContraEntrega> findData() {
		//Date fechaFin=Services.getInstance().obtenerFechaDelSistema();
		//Date fechaIni=DateUtils.addDays(fechaFin, -7);
		if(sucursal==null){
			this.sucursal=Services.getInstance().getConfiguracion().getSucursal();
		}
		String sql="SELECT V.CARGO_ID as id,v.docto as documento,v.fecha,v.nombre,v.total " +
				",v.total-IFNULL((SELECT sum(a.importe) FROM sx_cxc_aplicaciones a where a.cargo_id=v.cargo_id),0) as saldo" +
				",v.FPAGO as fpago,V.MODIFICADO_USERID AS facturista,V.PEDIDO_FOLIO as pedido,(CASE WHEN (SELECT X.CARGO_ID FROM sx_cxc_cargos_cancelados X WHERE V.CARGO_ID=X.CARGO_ID) IS null THEN false ELSE true END) AS cancelado" +
				",q.transporte_id as chofer" +
				",q.documento as embarque" +
				",q.salida,q.regreso,CASE WHEN ROUND(TO_DAYS(CURRENT_DATE)-TO_DAYS(V.FECHA),0)<1 THEN 0 ELSE ROUND(TO_DAYS(CURRENT_DATE)-TO_DAYS(V.FECHA),0) END AS atraso,v.origen " +
				",S.ASIGNACION "+
				"FROM sx_ventas v left join sx_entregas e on(v.CARGO_ID=e.VENTA_ID) left join sx_embarques q on(q.EMBARQUE_ID=e.EMBARQUE_ID) JOIN sx_asignacion_ce S ON(S.VENTA_ID=V.CARGO_ID) WHERE V.fecha >='2012/12/31' " +
				"and v.origen='CAM' AND V.CE IS true AND v.sucursal_id=? AND v.total-IFNULL((SELECT sum(a.importe) FROM sx_cxc_aplicaciones a where a.cargo_id=v.cargo_id),0)>0 ";
		Object[] params={
			//new SqlParameterValue(Types.DATE, fechaIni),	
			//new SqlParameterValue(Types.DATE, fechaFin),
			new SqlParameterValue(Types.INTEGER, sucursal.getId())
		};
		return Services.getInstance().getJdbcTemplate()
				.query(sql, params, new BeanPropertyRowMapper(VentaContraEntrega.class));
	}

	@Override
	public Action[] getActions() {		
		if(actions==null){
			actions=new Action[]{
				getLoadAction()
				,addRoleBasedContextAction(new FacturablePredicate(),POSRoles.CAJERO.name(), this, "cobrar", "Cobrar")
				,addRoleBasedContextAction(new FacturablePredicate(),POSRoles.CAJERO.name(), this, "cobrarConDeposito", "Cobrar con Deposito")
				,addRoleBasedContextAction(new CancelablePredicate(), POSRoles.CAJERO.name(), this, "cancelar", "Cancelar")
				,addAction(null, "refreshSeleccion", "Refrescar selecció")
				,getViewAction()
				};
		}
		return actions;
	}
	
	private VentaContraEntrega getVentaRow(){
		return (VentaContraEntrega)getSelectedObject();
	}
	
	public void cobrar(){
		VentaContraEntrega row=getVentaRow();
		if(row==null)
			return;
		boolean ok=validarEmbarque(row);
		if(!ok){
			return ;
		}
		if(row.getRetrasoAsignacion()>3){
			MessageUtils.showMessage("El  retraso en asignación a 3 dias no se puede pagar", "Cobranza CAM");
			return;
		}
		Venta venta=getFactura(row.getId());
		final BigDecimal porPagar=venta.getSaldoCalculado();
		if(venta.getFormaDePago()==null){
			JOptionPane.showMessageDialog(getControl() , "La venta no tiene registrado una forma de pago");
			return;
		}
		final PagoFormModel model=new PagoFormModel();
		model.getModel("formaDePago").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {				
				model.getPago().registrarImporte(porPagar);
			}			
		});
		List<FormaDePago> TIPOS_DE_PAGO=new ArrayList<FormaDePago>();
		TIPOS_DE_PAGO.add(FormaDePago.EFECTIVO);
		TIPOS_DE_PAGO.add(FormaDePago.CHEQUE);
		if(venta.getPedido()!=null){
			if(venta.getPedido().getFormaDePago().equals(FormaDePago.TARJETA_CREDITO))
				TIPOS_DE_PAGO.add(FormaDePago.TARJETA_CREDITO);
			if(venta.getPedido().getFormaDePago().equals(FormaDePago.TARJETA_DEBITO))
				TIPOS_DE_PAGO.add(FormaDePago.TARJETA_DEBITO);
		}
		model.setFormasDePago(TIPOS_DE_PAGO.toArray(new FormaDePago[0]));
		model.setSucursal(sucursal);
		model.getPago().setCliente(venta.getCliente());
		model.getPago().registrarImporte(venta.getSaldoCalculado());
		final PagoForm form=new PagoForm(model);
		form.setSelectionDeCliente(false);
		form.setPersistirAlAplicar(false);
		form.open();
		
		if(!form.hasBeenCanceled()){
			int index=source.indexOf(row);
			if(index!=-1){
				Pago pago=model.getPago().toPago();
				Date fecha=Services.getInstance().obtenerFechaDelSistema();
				Services.getInstance().getPagosManager().cobrarFactura(venta, pago,fecha);				
				Services.getInstance().getFacturasManager().generarAbonoAutmatico(Arrays.asList(venta));
				venta=getFactura(venta.getId());
				refreshRow(row);
			}			
		}
		model.dispose();
	}
	
	public void cobrarConDeposito(){
		VentaContraEntrega row=getVentaRow();
		if(row!=null){
			boolean ok=validarEmbarque(row);
			if(ok){
				if(row.getRetrasoAsignacion()>3){
					MessageUtils.showMessage("El retraso en asignación es mayor a 3 dias no se puede pagar", "Cobranza CAM");
					return;
				}
				Venta target=getFactura(row.getId());
				int index=source.indexOf(row);
				if(index!=-1){
					Abono pago=SelectorDeDisponibles.buscarPago(target.getCliente());
					if(pago==null) return;
					Date fecha=Services.getInstance().obtenerFechaDelSistema();
					pago=Services.getInstance().getPagosManager().getAbono(pago.getId());
					Services.getInstance().getPagosManager().cobrarFactura(target, pago,fecha);
					Services.getInstance().getFacturasManager().generarAbonoAutmatico(Arrays.asList(target));
					//target=getFactura(target.getId());
					//source.set(index, target);
					refreshRow(row);
				}
			}
		}
	}
	
	private Venta getFactura(String id){
		return Services.getInstance().getFacturasManager().getFactura(id);
	}
	
	private void refreshRow(VentaContraEntrega row){
		int index=source.indexOf(row);
		if(index!=-1){
			String sql="SELECT V.CARGO_ID as id,v.docto as documento,v.fecha,v.nombre,v.total " +
					",v.total-IFNULL((SELECT sum(a.importe) FROM sx_cxc_aplicaciones a where a.cargo_id=v.cargo_id),0) as saldo,v.FPAGO as fpago" +
					",V.MODIFICADO_USERID AS facturista,V.PEDIDO_FOLIO as pedido" +
					",(CASE WHEN (SELECT X.CARGO_ID FROM sx_cxc_cargos_cancelados X WHERE V.CARGO_ID=X.CARGO_ID) IS null THEN false ELSE true END) AS cancelado" +
					",q.transporte_id as chofer,q.documento as embarque,q.salida,q.regreso,CASE WHEN ROUND(TO_DAYS(CURRENT_DATE)-TO_DAYS(V.FECHA),0)<1 THEN 0 ELSE ROUND(TO_DAYS(CURRENT_DATE)-TO_DAYS(V.FECHA),0) END AS atraso" +
					",v.origen,S.ASIGNACION " +
					"FROM sx_ventas v left join sx_entregas e on(v.CARGO_ID=e.VENTA_ID)  join sx_asignacion_ce s on(s.VENTA_ID=v.cargo_id) " +
					" left join sx_embarques q on(q.EMBARQUE_ID=e.EMBARQUE_ID) " +
					" WHERE v.CARGO_ID=? " 
					;
			List<VentaContraEntrega> res=Services.getInstance().getJdbcTemplate()
					.query(sql, new Object[]{row.getId()}
					, new BeanPropertyRowMapper(VentaContraEntrega.class));
			if(!res.isEmpty()){
				source.set(index, res.get(0));
				System.out.println("Refrescado: "+res.get(0));
				//selectionModel.clearSelection();
			}
		}
	}
	public void refreshSeleccion(){
		for(Object o:getSelected() ){
			refreshRow((VentaContraEntrega)o);
		}
	}
	
	private boolean validarEmbarque(VentaContraEntrega row){
		if(row.getEmbarque().doubleValue()==0){
			MessageUtils.showMessage("Venta no embacada", "Cobranza contra entrega");
			return false;
		}
		if(row.getRegreso()==null){
			MessageUtils.showMessage("Embarque no ha regresado", "Cobranza contra entrega");
			return false;
		}
		return true;
			
	}
	
	@Override
	protected void doSelect(Object bean) {
		VentaContraEntrega  row=(VentaContraEntrega)bean;
		FacturaForm.show(row.getId());
	}
		
	public void cancelar(){
		VentaContraEntrega row=getVentaRow();
		if(row==null)
			return;
		Venta factura=getFactura(row.getId());		
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
					refreshRow(row);					
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
			VentaContraEntrega v=(VentaContraEntrega)bean;
			if(v!=null){
				return v.getSaldo().doubleValue()>0;
			}
			return false;
		}
	}
	
	public static class CancelablePredicate implements Predicate{
		public boolean evaluate(Object bean) {
			VentaContraEntrega v=(VentaContraEntrega)bean;
			if(v!=null){
				return !v.isCancelado();
			}
			return false;
		}
	}
	

}
