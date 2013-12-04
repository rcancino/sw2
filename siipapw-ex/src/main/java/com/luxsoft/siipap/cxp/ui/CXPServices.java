package com.luxsoft.siipap.cxp.ui;

import java.text.MessageFormat;

import javax.swing.SwingUtilities;

import org.springframework.util.Assert;

import com.luxsoft.siipap.cxp.model.CXPAnticipo;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.CXPNota;
import com.luxsoft.siipap.cxp.model.CXPPago;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.cxp.ui.form.AnalisisForm;
import com.luxsoft.siipap.cxp.ui.form.AnalisisModel;
import com.luxsoft.siipap.cxp.ui.form.AnticipoForm;
import com.luxsoft.siipap.cxp.ui.form.AnticipoFormModel;
import com.luxsoft.siipap.cxp.ui.form.NotaDeCreditoForm;
import com.luxsoft.siipap.cxp.ui.form.NotaDeCreditoFormModel;
import com.luxsoft.siipap.cxp.ui.form.RequisicionForm;
import com.luxsoft.siipap.cxp.ui.selectores.SelectorDeContraRecibos;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.Requisicion.Estado;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.sw3.cxp.forms.RequisicionDeComprasForm;

/**
 * Facade para los principales rutinas de CXP
 * 
 * @author Rubén Cancino 
 *
 */
public final class CXPServices {
	
	public static CXPFactura generarAnalisisDesdeRecibo(){
		ContraReciboDet recibo=SelectorDeContraRecibos.buscarReciboDeFacturas();
		if(recibo!=null){
			CXPFactura factura=new CXPFactura();
			factura.setProveedor(recibo.getRecibo().getProveedor());
			factura.setRecibo(recibo);
			return generarAnalisis(factura);
		}
		return null;
	}
	
	public static CXPFactura  generarAnalisis(){
		CXPFactura factura=new CXPFactura();
		return generarAnalisis(factura);
	}
	
	public static CXPFactura generarAnalisis(CXPFactura factura){
		AnalisisModel model=new AnalisisModel(factura);
		
		AnalisisForm form=new AnalisisForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			CXPFactura target=model.getAnalisis();
			target=CXPServiceLocator.getInstance().getFacturasManager().save(target);
			//return target;
			return CXPServiceLocator.getInstance().getFacturasManager().get(target.getId());
		}
		return null;
	}
	
	public static CXPFactura editarAnalisis(Long analisisId){
		CXPFactura analisis=CXPServiceLocator.getInstance().getFacturasManager().get(analisisId);
		if(analisis.getPagos().doubleValue()!=0){
			MessageUtils.showMessage("El analisis ya tiene pagos aplicados no se puede modificar", "Modificacion de análisis");
			return null;
		}
		
		if(analisis.getRequisitado().doubleValue()!=0 && (analisis.isFraccionada()==false)){
			MessageUtils.showMessage("El analisis ya esta requisitado no se puede modificar"
					, "Modificacion de análisis");
			return null;
		}
		AnalisisModel model=new AnalisisModel(analisis);
		AnalisisForm form=new AnalisisForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			CXPFactura target=model.getAnalisis();
			target=CXPServiceLocator.getInstance().getFacturasManager().save(target);
			return target;
		}
		return null;
	}
	
	public static Requisicion generarRequisicion(Periodo periodo){
		Requisicion  bean=RequisicionForm.showForm();		
		if(bean!=null){
			if(validarPeriodo(bean,periodo)){
				bean.setOrigen(Requisicion.COMPRAS);
				bean.setEstado(Estado.REVISADA);
				bean=ServiceLocator2.getRequisiciionesManager().save(bean);
				return bean;
			}
		}
		return null;
	}
	
	public static Requisicion modificarRequisicion(final Long id){
		Requisicion bean=ServiceLocator2.getRequisiciionesManager().buscarRequisicionDeCompras(id);
		
		//Requisicion  proxy=RequisicionForm.showForm(bean,false);
		Requisicion proxy=RequisicionDeComprasForm.showForm(bean, false);
		if(proxy!=null){
			if(proxy.getPagoCxp()!=null){
				MessageUtils.showMessage("La requisición se ha utilizado para genera el pago:\n" +
						" "+proxy.getPagoCxp().getId(), " Es necesario eliminar el este pago ");
				return null;
			}
			bean.setOrigen(Requisicion.COMPRAS);
			bean.setEstado(Estado.AUTORIZADA);
			bean=ServiceLocator2.getRequisiciionesManager().save(bean);
			return bean;
		}
		return null;
	}
	 
	
	public static void eliminarRequisicion(Requisicion req){
		ServiceLocator2.getRequisiciionesManager().remove(req.getId());
	}
	
	public static boolean validarPeriodo(final Requisicion req,final Periodo periodo){
		
		if(periodo.isBetween(req.getFecha())){
			return true;
		}else{
			String msg=MessageFormat.format("La fecha de la requisicion: {0,date,short}" +
					"\n no corresponde al periodo de trabajo {1} " +
					"\n imposible actualizar", req.getFecha(),periodo);
			MessageUtils.showMessage(msg,"Impresión de requisición");
			return false;
		}
	}
	
	public static CXPPago aplicarPago(final Requisicion r){
		Assert.isTrue(r.getPagoCxp()==null,"La requisición ya se ha aplicado");
		String patter="Genera pago  de forma automática para la req: {0} por un monto de: {1} ";
		boolean res=MessageUtils.showConfirmationMessage(MessageFormat.format(patter, r.getId(),r.getTotal()), "CxP Aplicación de pagos");
		if(res){
			CXPPago pago=CXPServiceLocator.getInstance()
			.getAbonosManager()
			.aplicarPago(r);
			return pago;
		}
		return null;
	}
	
	public static CXPAnticipo aplicarAnticipo(final Requisicion r){
		Assert.isTrue(r.getPagoCxp()==null,"La requisición ya se ha aplicado");
		Assert.isTrue(r.getConcepto().getClave().equals("ANTICIPO"),"La requisicion no es de tipo anticipo");
		String patter="Genera anticipo  de forma automática para la req: {0} por un monto de: {1} ";
		boolean res=MessageUtils.showConfirmationMessage(MessageFormat.format(patter, r.getId(),r.getTotal()), "CxP Generación de anticipo");
		if(res){
			CXPAnticipo anticipo=CXPServiceLocator.getInstance()
			.getAbonosManager()
			.aplicarAnticipo(r);
			return anticipo;
		}
		return null;
	}
	
	public static CXPNota generarNota(){
		NotaDeCreditoFormModel model=new NotaDeCreditoFormModel();
		NotaDeCreditoForm form=new NotaDeCreditoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			CXPNota res=CXPServiceLocator.getInstance()
			.getAbonosManager().salvarNota(model.commit());
			res=CXPServiceLocator.getInstance().getAbonosManager().buscarNota(res.getId());  
			return res;
		}
		return null;
	}
	
	public static CXPNota editarNota(final Long id){
		CXPNota nota=CXPServiceLocator.getInstance().getAbonosManager().buscarNota(id);
		NotaDeCreditoFormModel model=new NotaDeCreditoFormModel(nota);
		NotaDeCreditoForm form=new NotaDeCreditoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			CXPNota res=CXPServiceLocator.getInstance().getAbonosManager().salvarNota(model.commit());
			res=CXPServiceLocator.getInstance().getAbonosManager().buscarNota(res.getId());  
			return res;
		}
		return null;
	}
	
	public static CXPAnticipo editarAnticipo(final Long id){
		CXPAnticipo anticipo=CXPServiceLocator.getInstance().getAbonosManager().buscarAnticipo(id);
		AnticipoFormModel model=new AnticipoFormModel(anticipo);
		AnticipoForm form=new AnticipoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			CXPAnticipo res=CXPServiceLocator.getInstance().getAbonosManager().salvarAnticipo(model.commit());
			res=CXPServiceLocator.getInstance().getAbonosManager().buscarAnticipo(res.getId());  
			return res;
		}
		return null;
	}
	 
	
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				//editarAnalisis(18l);
				generarAnalisis();
				System.exit(0);
			}			
		});
	}

}
