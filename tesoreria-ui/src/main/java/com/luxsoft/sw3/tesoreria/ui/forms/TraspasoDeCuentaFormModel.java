package com.luxsoft.sw3.tesoreria.ui.forms;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.tesoreria.model.TraspasoDeCuenta;

public class TraspasoDeCuentaFormModel extends DefaultFormModel{

	public TraspasoDeCuentaFormModel() {
		super(new TraspasoDeCuenta());
		
	}
	
	public TraspasoDeCuenta commit(){
		return getTraspaso();
	}
	
	public TraspasoDeCuenta getTraspaso(){
		return (TraspasoDeCuenta)getBaseBean();
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getTraspaso().getCuentaDestino()!=null && getTraspaso().getCuentaOrigen()!=null){
			if(getTraspaso().getCuentaDestino().equals(getTraspaso().getCuentaOrigen())){
				support.addError("cuentaDestino", "No se puede usar la misma cuenta de origen y destino");
			}
		}
		if(getTraspaso().getImporte().doubleValue()<=0){
			support.addError("", "Importe invalido");
		}
		
	}
	
	private EventList cuentas;
	
	public EventList getCuentas() {
		if(cuentas==null){
			cuentas=GlazedLists.eventList(
					ServiceLocator2.getHibernateTemplate().find("from Cuenta")
					);
		}
		return cuentas;
	}

}
