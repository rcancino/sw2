package com.luxsoft.sw3.impap.ui.consultas;

import java.util.Date;
import java.util.List;

import javax.swing.Action;

import com.luxsoft.siipap.cxc.ui.consultas.FacturaForm;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.ventas.Pedido;

public class FacturasPanel extends FilteredBrowserPanel<Venta>{
	
	

	public FacturasPanel() {
		super(Venta.class);
	}
	
	protected void init(){
		String[] props=new String[]{
				"sucursal.nombre"
				,"origen"
				,"pedido.entrega"				
				,"pedido.folio"
				,"documento"
				,"numeroFiscal"
				,"fecha"
				,"clave"
				,"nombre"
				,"total"
				,"pagos"
				,"saldoCalculado"
				,"facturista"
				,"cancelado"
				,"impreso"
				};
		String[] names=new String[]{				
				"Suc"
				,"Ori"
				,"Entrega"
				,"Pedido"
				,"Docto"
				,"N.Fiscal"
				,"Fecha"
				,"Cliente"
				,"Nombre"
				,"Total"
				,"Pagos"
				,"Saldo"
				,"Facturista"
				,"Cancelado"
				,"Impreso"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("N.Fiscal", "numeroFiscal");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		installTextComponentMatcherEditor("Total", "total");
		manejarPeriodo();
	}

	@Override
	protected List<Venta> findData() {
		return ServiceLocator2.getHibernateTemplate().find(
				"from Venta v left join fetch v.pedido p " +
				"where v.sucursal.id=? and v.origen!=\'CRE\' " +
				"and date(v.fecha) between ? and ? ",new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}

	@Override
	protected void doSelect(Object bean) {
		Venta venta=(Venta)bean;
		FacturaForm.show(venta.getId());
	}
	
	private Action cancelAction;
	
	public Action getCancelAction(){
		if(cancelAction==null){
			cancelAction=addRoleBasedContextAction(new CancelablePredicate(),null, this, "cancelar", "Cancelar");
			cancelAction.putValue(Action.SMALL_ICON, getIconFromResource("images/edit/delete_edit.gif"));
		}
		return cancelAction;
	}
	
	private Action imprimirAction;
	
	public Action getImprimirAction(){
		if(imprimirAction==null){
			imprimirAction=addRoleBasedContextAction(new ImprimiblePredicate(), null, this, "imprimir", "Imprimir");
			imprimirAction.putValue(Action.SMALL_ICON, getIconFromResource("images/file/printview_tsk.gif"));
		}
		return imprimirAction;
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
			if(index!=-1 ){
				final Date fecha=ServiceLocator2.obtenerFechaDelSistema();
				factura=ServiceLocator2.getFacturasManager().cancelarFactura(factura.getId(),fecha);
				source.set(index, factura);
				selectionModel.clearSelection();
			}			
		}
	}
	
	
	
	public void imprimir(){
		if(getSelectedObject()==null)
			return;
		Venta factura=(Venta)getSelectedObject();
		if(factura.getImpreso()==null){
			//ReportUtils2.imprimirFactura(factura);
			
		}
	}
	
	private Action generarCFDAction;
	
	private Action imprimirCFDAction;
	
	public Action getGenerarCFDAction(){
		if(generarCFDAction==null){
			generarCFDAction=addRoleBasedContextAction(null, null, this, "generarCFD", "Generar CFD");
			generarCFDAction.putValue(Action.SMALL_ICON, getIconFromResource("images/file/printview_tsk.gif"));
		}
		return generarCFDAction;
	}
	

	
	public Action getImprimirCFDAction(){
		if(imprimirCFDAction==null){
			imprimirCFDAction=addRoleBasedContextAction(null, null, this, "imprimirCFD", "Imprimir CFD");
			imprimirCFDAction.putValue(Action.SMALL_ICON, getIconFromResource("images/file/printview_tsk.gif"));
		}
		return imprimirCFDAction;
	}
	
	public void generarCFD(){
		Venta venta=(Venta)getSelectedObject();
		if(venta!=null){
			ComprobanteFiscal cfd=ServiceLocator2.getCFDManager().cargarComprobante(venta);
			logger.info("Factura digital generada: "+cfd);
			if(cfd==null){
				//CFDPrintServices.impripirComprobanteAlFacturar(venta,cfd);
			}
		}
	}
	
	public void imprimirCFD(Venta venta,ComprobanteFiscal cfd){
		
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
