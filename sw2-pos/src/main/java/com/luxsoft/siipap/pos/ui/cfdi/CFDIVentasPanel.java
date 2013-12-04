package com.luxsoft.siipap.pos.ui.cfdi;

import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.lang.time.DateUtils;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.consultas.caja.CobranzaPagoContraEntrega.CancelablePredicate;
import com.luxsoft.siipap.pos.ui.forms.FacturaForm;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.security.CancelacionDeCargoForm;
import com.luxsoft.siipap.security.CancelacionDeCargoFormModel;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Venta;


import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.services.Services;


public class CFDIVentasPanel extends FilteredBrowserPanel<Venta>{
	
	

	public CFDIVentasPanel() {
		super(Venta.class);
	}
	
	protected void init(){
		String[] props=new String[]{
				"origen"	
				,"pedido.folio"
				,"documento"				
				,"fecha"
				,"nombre"
				,"moneda"
				,"tc"
				,"total"
				,"pagos"
				,"saldoCalculado"
				,"facturista"
				,"cancelado"
				,"impreso"
				,"timbrado"
				};
		String[] names=new String[]{
				"Ori"
				,"Pedido"
				,"Docto"				
				,"Fecha"
				,"Nombre"
				,"Mon"
				,"T.C."
				,"Total"
				,"Pagos"
				,"Saldo"
				,"Facturista"
				,"Cancelado"
				,"Impreso"
				,"Timbrado"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Pedido", "pedido.folio");
		installTextComponentMatcherEditor("Documento", "documento");		
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		installTextComponentMatcherEditor("Total", "total");
	}

	@Override
	protected List<Venta> findData() {
		Sucursal sucursal=Services.getInstance().getConfiguracion().getSucursal();
		Date dia=Services.getInstance().obtenerFechaDelSistema();
		return Services.getInstance().getHibernateTemplate().find(
				"from Venta v left join fetch v.pedido p " +
				"where v.sucursal.id=? and v.origen!=\'CRE\' " +
				"and v.fecha=?",new Object[]{sucursal.getId(), dia});
	}

	@Override
	protected void doSelect(Object bean) {
		Venta venta=(Venta)bean;
		FacturaForm.show(venta.getId());
	}
	
	private Action cancelAction;
	
	public Action getCancelAction(){
		if(cancelAction==null){
			cancelAction=addRoleBasedContextAction(new CancelablePredicate(), POSRoles.CAJERO.name(), this, "cancelar", "Cancelar");
			cancelAction.putValue(Action.SMALL_ICON, getIconFromResource("images/edit/delete_edit.gif"));
		}
		return cancelAction;
	}
	
	private Action imprimirAction;
	
	public Action getImprimirAction(){
		if(imprimirAction==null){
			imprimirAction=addRoleBasedContextAction(new ImprimiblePredicate(), POSRoles.CAJERO.name(), this, "imprimir", "Imprimir");
			imprimirAction.putValue(Action.SMALL_ICON, getIconFromResource("images/file/printview_tsk.gif"));
		}
		return imprimirAction;
	}
	
	public void cancelar(){
		if(getSelectedObject()==null)
			return;
		Venta factura=(Venta)getSelectedObject();		
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
					factura=Services.getInstance().getFacturasManager().getFactura(factura.getId());
					source.set(index, factura);
					selectionModel.clearSelection();					
				}
				
			}			
		}
	}
	
	private boolean validarCancelacion(final Venta factura){
		Date fecha=Services.getInstance().obtenerFechaDelSistema();
		String hql="from Entrega e where e.factura.id=?";
		List<Entrega> res=Services.getInstance().getHibernateTemplate().find(hql, factura.getId());
		if(!res.isEmpty()){
			JOptionPane.showMessageDialog(getControl(), "La factura ya esta asignada para entrega","Validación",JOptionPane.WARNING_MESSAGE);
			return false;
		}
		if(DateUtils.isSameDay(fecha, factura.getFecha())){
			return true;
		}else{
			JOptionPane.showMessageDialog(getControl(), "La factura no es del día","Validación",JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}
	
	public void imprimir(){
		if(getSelectedObject()==null)
			return;
		Venta factura=(Venta)getSelectedObject();
		if(factura.getImpreso()==null){
			ReportUtils2.imprimirFactura(factura);
		}
	}
	
	private Action generarCFDAction;
	
	private Action imprimirCFDAction;
	
	public Action getGenerarCFDAction(){
		if(generarCFDAction==null){
			generarCFDAction=addRoleBasedContextAction(null, POSRoles.CAJERO.name(), this, "generarCFDI", "Generar CFD");
			generarCFDAction.putValue(Action.SMALL_ICON, getIconFromResource("images/file/printview_tsk.gif"));
		}
		return generarCFDAction;
	}
	

	
	public Action getImprimirCFDAction(){
		if(imprimirCFDAction==null){
			imprimirCFDAction=addRoleBasedContextAction(null, POSRoles.CAJERO.name(), this, "imprimirCFD", "Imprimir CFD");
			imprimirCFDAction.putValue(Action.SMALL_ICON, getIconFromResource("images/file/printview_tsk.gif"));
		}
		return imprimirCFDAction;
	}
	
	public void generarCFD(){
		Venta venta=(Venta)getSelectedObject();
		if(venta!=null){
			CFDI cfdi=Services.getCFDIManager().generarFactura(venta);
			logger.info("Factura digital generada: "+cfdi);
		}
	}
	
	
	
	public static class ImprimiblePredicate implements Predicate{
		public boolean evaluate(Object bean) {
			Venta v=(Venta)bean;
			if(v!=null){
				return v.getImpreso()==null;
			}
			return false;
		}
	}

}
