package com.luxsoft.siipap.pos.ui.forms.caja;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;

public class PagoconDepositoFormModel extends DefaultFormModel{

	public PagoconDepositoFormModel(PagoConDeposito pago) {
		super(pago);
		
	}

	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(StringUtils.isBlank(getDeposito().getBanco())){
			support.getResult().addError("Registre el banco origen ");
			return;
		}if(getDeposito().getCuenta()==null){
			support.getResult().addError("Registre la cuenta destino");
			return;
		}if(getDeposito().getFechaDeposito()==null){
			support.getResult().addError("Registre la fecha del deposito o transferencia");
			return;
		}if(StringUtils.isBlank(getDeposito().getSolicito())){
			support.getResult().addError("Registre quien solicita ");
			return;
		}
		
	}
	
	private PagoConDeposito getDeposito(){
		return (PagoConDeposito)getBaseBean();
	}
	
	

}
