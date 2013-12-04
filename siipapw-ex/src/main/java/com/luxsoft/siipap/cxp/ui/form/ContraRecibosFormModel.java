package com.luxsoft.siipap.cxp.ui.form;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxp.model.ContraRecibo;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.cxp.service.ContraReciboManager;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;

public class ContraRecibosFormModel extends MasterDetailFormModel {

	
	public ContraRecibosFormModel() {
		super(ContraRecibo.class);
	}
	
	public ContraRecibosFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
		if(getRecibo().getId()!=null){
			source.addAll(getManager().buscarPartidas(getRecibo()));
		}
	}
	
	public ContraRecibo getRecibo(){
		return (ContraRecibo)getBaseBean();
	}
	
	protected void addValidation(PropertyValidationSupport support) {
		if(getRecibo().getTotal().doubleValue()==0){
			//support.addError("partidas", "No existen documentos para el contra recibo");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object insertDetalle(Object obj) {
		ContraReciboDet det=(ContraReciboDet)obj;
		boolean res=getRecibo().agregarPartida(det);
		if(res){
			source.add(det);
			getRecibo().actualizarImporte();
			return true;
		}
		return null;
	}
	
	@Override
	public boolean deleteDetalle(Object obj) {
		if(!isReadOnly()){
			ContraReciboDet det=(ContraReciboDet)obj;
			boolean res= getRecibo().removerPartida(det);
			if(res)
				source.remove(det);
			getRecibo().actualizarImporte();
			return res;
		}
		return false;
	}

	@Override
	public boolean manejaTotalesEstandares() {
		return false;
	}
	
	private ContraReciboManager getManager(){
		return CXPServiceLocator.getInstance().getRecibosManager();
	}

}
