package com.luxsoft.sw3.tesoreria.ui.forms;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.tesoreria.model.ComisionBancaria;


public class ComisionBancariaFormModel extends DefaultFormModel{

	public ComisionBancariaFormModel() {
		super(new ComisionBancaria());
		
	}
	public ComisionBancariaFormModel(ComisionBancaria i){
		super(i);
	}
	
	public ComisionBancaria commit(){
		return getComisionBancaria();
	}
	
	public ComisionBancaria getComisionBancaria(){
		return (ComisionBancaria)getBaseBean();
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		
		if(getComisionBancaria().getComision().doubleValue()<=0){
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
