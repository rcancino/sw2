package com.luxsoft.siipap.tesoreria.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Currency;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.matchers.ThreadedMatcherEditor;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.inventarios.model.Movimiento.Concepto;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;


/**
 * PresentationModel para la forma de CargoAbonoForm
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CargoAbonoFormModel extends DefaultFormModel{
	
	

	public CargoAbonoFormModel(CargoAbono mov) {
		super(mov);
		
	}
	
	@Override
	protected void init() {
		
	}
	
	protected void addValidation(PropertyValidationSupport support){
		if(!getCargoAbono().getMoneda().equals(getCargoAbono().getCuenta().getMoneda())){
			support.getResult().addError("La moneda de la cuenta debe ser: "+getCargoAbono().getMoneda());
		}
	}

	
	public boolean isDeosito(){
		return getCargoAbono().getDeposito().doubleValue()>0;
	}
	
	public CargoAbono getCargoAbono(){
		return (CargoAbono)getBaseBean();
	}
	
	public CargoAbono commit(){
		return getCargoAbono();
	}
	
	

}
