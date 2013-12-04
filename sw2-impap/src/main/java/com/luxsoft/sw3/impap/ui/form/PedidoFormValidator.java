package com.luxsoft.sw3.impap.ui.form;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.Pedido.FormaDeEntrega;

/**
 * Auxiliar en la validacion de pedidos, util para simplificar el bean PedidoController
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PedidoFormValidator {
	
	private Pedido pedido;

	public void validate(final Pedido pedido,PropertyValidationSupport support) {
		
		setPedido(pedido);		
		Cliente c=getPedido().getCliente();
		
		
			//validarCondicionesDeCredito(support);
		
		//validarFormaDePago(support);
		//validarFormaDeEnvio(support);
		//if(getPedido().getPartidas().isEmpty())
			//support.getResult().addError("El pedido sin partidas no es válido");
		//validarDireccion(support);
		
	}
	
	private void validarFormaDePago(PropertyValidationSupport support){
		Cliente c=getPedido().getCliente();
		if(FormaDePago.CHEQUE_POSTFECHADO.equals(getPedido().getFormaDePago())){
			if(c!=null && c.isDeCredito())				
				if(!c.getCredito().isChequePostfechado()){
					//getValidationModel().getResult().addError("A este cliente no se le permite cheque post- fechado");
					support.getResult().addError("A este cliente no se le permite cheque post - fechado");
				}
			if(!getPedido().isDeCredito()){
				support.getResult().addError("No se permite Cheque post fechado en contado");
			}
								
		}else if(FormaDePago.CHEQUE.equals(getPedido().getFormaDePago())){
			if(c!=null && (!c.isPermitirCheque()))				
				support.getResult().addError("A este cliente no se le permite cheque ");
					
								
		}
	}
	
	private void validarFormaDeEnvio(PropertyValidationSupport support){
		if(getPedido().getEntrega().name().startsWith("ENVIO")){
			if(getPedido().getInstruccionDeEntrega()==null){
				support.getResult().addError("Debe definir la dirección de envio");
			}
		}else if(FormaDeEntrega.ENVIO_CARGO.equals(getPedido().getEntrega())){
			if(getPedido().getFlete().doubleValue()<=0)
				support.getResult().addError("El tipo de envio con cargo no aplica para la población/estado definidos");
		}
	}
	
	
	
	private void validarCondicionesDeCredito(PropertyValidationSupport support){
		Cliente c=getPedido().getCliente();
		if(c==null)	return;
		
		//Validar q el cliente tenga credito
		if(!c.isDeCredito()){
			support.getResult().addError("El cliente no tiene línea de crédito");
			return;
		} 
		
		//Valida credito no suspendido
		if(c.getCredito().isSuspendido())
			support.getResult().addError( "Crédito suspendido temporalmente ");	
		
		//Valida la linea de credito
		if(c.getCredito().getCreditoDisponible().doubleValue()<getPedido().getTotal().doubleValue()){
			support.getResult().addError( "Línea de crédito SATURADA");
		}
		
		//Valida el atraso maximo del cliente
		if(c.getCredito().getAtrasoMaximo()>15){
			support.getResult().addError( "El cliente tiene un atraso superior a 15 dias (Llamar a Crédito)");
		}
		
	}
	
	private void validarDireccion(PropertyValidationSupport support){
		if(getPedido().getEntrega().equals(FormaDeEntrega.LOCAL))
			return;
		if(getPedido().getCliente()!=null){
			Cliente c=getPedido().getCliente();
			if("1".equals(c.getClave()))
				return;
			if(c.getDireccionFiscal()!=null ){
				Direccion d=c.getDireccionFiscal();
				if(	StringUtils.isBlank(d.getCalle())
					||StringUtils.isBlank(d.getCp())
					||StringUtils.isBlank(d.getColonia()))
				{
					
					support.getResult().addError( "Direccion incorrecta");
				}
			}else{
				support.getResult().addError( "La dirección del cliente no puede ser nula");
			}
		}
	}
	
	
	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}
	
	
}
