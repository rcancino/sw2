package com.luxsoft.siipap.pos.ui.forms.caja;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.model.core.Cliente;

public class PagoValidatorSupport {
	
	private PagoModel pago;
	
	
	
	
	public PagoModel getPago() {
		return pago;
	}

	public void setPago(PagoModel pago) {
		this.pago = pago;
	}

	public void validar(PagoModel pago,PropertyValidationSupport support){
		validarImportes(support);
		validarFormaDePago(support);
	}
	
	private void validarImportes(PropertyValidationSupport support){
		if(getPago().getImporte().doubleValue()<=0)
			support.getResult().addError("El importe del pago no puede ser 0");
	}
	
	private void validarFormaDePago(PropertyValidationSupport support) {		
		FormaDePago fp=getPago().getFormaDePago();
		if(fp!=null){
			switch (fp) {
			case CHEQUE:
				validarCheque(support);
				break;
			case CHEQUE_POSTFECHADO:
				validarCheque(support);
				Cliente c=getPago().getCliente();
				if((c!=null) && (c.getCredito()!=null) && !c.getCredito().isChequePostfechado()){
					support.getResult().addError("Cliente sin autorización para recibir cheque post fechado");
				}
				break;
			case DEPOSITO:
				validarDeposito(support);
				break;
			case EFECTIVO:
				break;
			case TRANSFERENCIA:
				break;
			case TARJETA_CREDITO:
			case TARJETA_DEBITO:
				break;
			default:
				break;
			}
		}
		
	}
	
	private void validarCheque(PropertyValidationSupport support){
		if(getPago().getCliente()!=null){
			if(!getPago().getCliente().isPermitirCheque()){
				support.getResult().addError("Este cliente no está autorizado para recibir cheque ");
			}
		}
		if(getPago().getNumero()<=0)
			support.getResult().addError("Se requiere el número de cheque");
		/*if(StringUtils.isBlank(getPago().getCuenta())){
			support.getResult().addError("Se requiere el número de cuenta");
		}*/
	}
	
	private void validarDeposito(PropertyValidationSupport support){
		if(getPago().getCuentaDestino()==null)
			support.getResult().addError("La cuenta destino es mandatoria ");
		if(getPago().getFechaDeposito()==null){
			support.getResult().addError("La fecha del  deposito/transferencia es mandatoria ");
		}
	}

}
