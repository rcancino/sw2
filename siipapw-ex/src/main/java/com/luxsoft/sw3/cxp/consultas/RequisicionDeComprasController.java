package com.luxsoft.sw3.cxp.consultas;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;


import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.Requisicion.Estado;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.tesoreria.RequisicionesManager;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.sw3.cxp.forms.RequisicionDeComprasForm;
import com.luxsoft.sw3.cxp.forms.RequisicionDeComprasForm2;
import com.luxsoft.utils.LoggerHelper;

public class RequisicionDeComprasController {
	
	static Logger logger=LoggerHelper.getLogger();
	
	public static Requisicion generarRequisicion(Periodo periodo){
		Requisicion  bean=RequisicionDeComprasForm2.showForm();		
		if(bean!=null){
			if(validarPeriodo(bean,periodo)){
				bean.setOrigen(Requisicion.COMPRAS);
				bean.setEstado(Estado.AUTORIZADA);
				bean=ServiceLocator2.getRequisiciionesManager().save(bean);
				return bean;
			}
		}
		return null;
	}
	
	public static Requisicion editar(Requisicion source,final Periodo periodo){
		Requisicion  target=getManager().buscarRequisicionDeCompras(source.getId());
		if(target.getPagoCxp()!=null){
			MessageUtils.showMessage("La requisición se ha utilizado para genera el pago:\n" +
					" "+target.getPagoCxp().getId(), " Es necesario eliminar el este pago ");
			return null;
		}
		target=RequisicionDeComprasForm2.showForm(target);		
		if(target!=null){
			if(validarPeriodo(target,periodo)){
				target.setOrigen(Requisicion.COMPRAS);
				target.setEstado(Estado.AUTORIZADA);
				logger.debug("Analisis por desvincular..."+target.getAnalisisPorActualizar().size());
				logger.info("Actualizando requisicion: "+target+ "Id: "+target.getId());
				target=getManager().save(target);
				
				
				return target;
			}
		}
		return null;
	}
	
	public static void eliminarRequisicion(Requisicion req){
		Assert.isNull(req.getPago(),"Pago registrado en tesoreria");
		getManager().remove(req.getId());
	}
	
	public static RequisicionesManager getManager(){
		return ServiceLocator2.getRequisiciionesManager();
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
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				generarRequisicion(Periodo.getPeriodoDelMesActual());
			}
		});
	}

}
