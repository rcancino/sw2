package com.luxsoft.sw3.tesoreria.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.tesoreria.model.Inversion;


public class InversionFormModel extends DefaultFormModel{

	public InversionFormModel() {
		super(new Inversion());
		
	}
	public InversionFormModel(Inversion i){
		super(i);
	}
	
	@Override
	protected void init() {
		super.init();
		PropertyChangeListener handler=new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				getInversion().actualizarRendimientoCalculado();
				getInversion().setImporteRealISR(getInversion().getImporteISR());
			}
		};
		getModel("tasa").addValueChangeListener(handler);
		getModel("isr").addValueChangeListener(handler);
		
	}
	
	public Inversion commit(){
		return getInversion();
	}
	
	public Inversion getInversion(){
		return (Inversion)getBaseBean();
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getInversion().getCuentaDestino()!=null && getInversion().getCuentaOrigen()!=null){
			if(getInversion().getCuentaDestino().equals(getInversion().getCuentaOrigen())){
				support.addError("cuentaDestino", "No se puede usar la misma cuenta de origen y destino");
			}
		}
		if(getInversion().getImporte().doubleValue()<=0){
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
