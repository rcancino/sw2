package com.luxsoft.siipap.cxc.ui;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.model.Pago;

/**
 * FormModel para el funcionamiento de una forma  de Pago 
 * 
 * @author Ruben Cancino
 *
 */
public class PagoFormModel extends AbonoFormModel{
	
	

	public PagoFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
		
	}
	
	public Pago getPago(){
		return (Pago)getAbono();
	}

	/*@Override
	public void clienteChanged() {
		if(getPago().getCliente()!=null)
			setValue("cobrador", getPago().getCliente().getCobrador());
	}*/

	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getPago().getCobrador()==null)
			support.addError("cobrador", "Se requiere definir el cobrador");
	}

	
	

}
