package com.luxsoft.siipap.pos.ui.consultas;

import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

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
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;

public class FacturasGeneradasPanel extends FilteredBrowserPanel<Venta>{
	
	//private Sucursal sucursal;

	public FacturasGeneradasPanel() {
		super(Venta.class);
		//sucursal=Services.getInstance().getConfiguracion().getSucursal();
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
	}

	@Override
	protected List<Venta> findData() {
		Sucursal sucursal=Services.getInstance().getConfiguracion().getSucursal();
		return Services.getInstance().getHibernateTemplate().find(
				"from Venta v left join fetch v.pedido p " +
				"where v.sucursal.id=? and v.pedido!=null order by v.log.creado" 
				,new Object[]{sucursal.getId()});
	}

	@Override
	protected void doSelect(Object bean) {
		Venta venta=(Venta)bean;
		FacturaForm.show(venta.getId());
	}
	
	
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getImprimirAction()	};
		return actions;
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
			imprimirAction=addRoleBasedContextAction(new NotNullSelectionPredicate(), POSRoles.CAJERO.name(), this, "imprimir", "Re-Imprimir");
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
	
	public void imprimir(){
		if(getSelectedObject()==null)
			return;
		Venta factura=(Venta)getSelectedObject();
		ReportUtils2.imprimirFacturaCopia(factura.getId());
	}

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	
	

}
