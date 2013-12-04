package com.luxsoft.sw3.cxp.consultas;

import java.text.MessageFormat;

import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.service.AnalisisDeCompraManager;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.cxp.forms.FacturaDeComprasForm;
import com.luxsoft.sw3.cxp.forms.FacturaDeComprasFormModel;

public class FacturaDeComprasController {
	
	public static AnalisisDeFactura registrarFacura(){
		final FacturaDeComprasFormModel model=new FacturaDeComprasFormModel();
		final FacturaDeComprasForm form=new FacturaDeComprasForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			//CXPFactura fac=(CXPFactura)model.getBaseBean();
//			if(fac.getAutorizacion()==null){
//				if(MessageUtils.showConfirmationMessage("Deséa autorizar la factura?", "Autorización")){
//					User user=SeleccionDeUsuario.findUser(ServiceLocator2.getHibernateTemplate());
//					if(user!=null){
//						AutorizacionDeCargoCXP autorizacion=new AutorizacionDeCargoCXP();
//						autorizacion.setAutorizo(user.getFullName());
//						autorizacion.setComentario("FACTURA AUTORIZADA");
//						autorizacion.setFechaAutorizacion(new Date());
//						autorizacion.setIpAdress(KernellSecurity.getIPAdress());
//						autorizacion.setMacAdress(KernellSecurity.getMacAdress());
//						fac.setAutorizacion(autorizacion);
//					}
//				}
//			}
			//return CXPServiceLocator.getInstance().getFacturasManager().save(fac);
			model.commit();
			return getManager().salvarAnalisis(model.getAnalisis());
		}
		return null;
	}
	
	public static AnalisisDeFacturaRow editarFactura(AnalisisDeFacturaRow source){
		AnalisisDeFactura target=getManager()
		.get(source.getId());
		if(target.getRequisicionDet()!=null){
			MessageUtils.showMessage("Análisis ya esta requisitado", "Mantenimiento de análisis");
			return source;
		}
		CXPFactura factura=target.getFactura();
		if(factura.getPagos().abs().doubleValue()>0){
			String msg="Imposible modificar la factura: {0} tiene pagos aplicados por: {1}";
			MessageUtils.showMessage(
					MessageFormat.format(msg, factura.getId()
							,factura.getPagos())
					,"Factura NO MODIFICABLE");
			return source;
		}
		
		final FacturaDeComprasFormModel model=new FacturaDeComprasFormModel(target);
		final FacturaDeComprasForm form=new FacturaDeComprasForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			model.commit();
			AnalisisDeFactura res= getManager()
				.salvarAnalisis(model.getAnalisis());
			return new AnalisisDeFacturaRow(res);
		}
		return source;
	}
	
	public static boolean eliminarFactura(AnalisisDeFacturaRow row){
		AnalisisDeFactura bean=getManager().get(row.getId());
		if(bean.getFactura().getAnalizadoComoFlete().doubleValue()>0){
			MessageUtils.showMessage("Factura de flete, no se puede eliminar en esta consulta", "Cancelación de Facturas");
			return false;
		}		
		getManager().eliminar(bean.getId());
		return true;
	}
	
	private static AnalisisDeCompraManager getManager(){
		return ServiceLocator2
		.getAnalisisDeCompraManager();
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				//registrarFacura();
				AnalisisDeFactura a=getManager().get(65696L);
				editarFactura(new AnalisisDeFacturaRow(a));
			}
		});
	}


}
