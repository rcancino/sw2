package com.luxsoft.siipap.pos.ui.venta.forms;

import java.text.MessageFormat;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoDet;
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
		if(c!=null){
			if(c.isSuspendido()){
				support.getResult().addError("Cliente suspendido PARA TODO TIPO DE VENTA");
			}
			if(c.isJuridico()){
				support.getResult().addError( "Cliente en trámite jurídico por lo que no se le puede facturar.\n Pedir autorización al departamento de crédito");
			}
			if(c.getChequesDevueltos().doubleValue()>0){
				support.getResult().addError( "El cliente tiene cheque(s) devueltos por un monto de: "+c.getChequesDevueltos()+" NO SE LE PUEDE FACTURAR.");
			}
		}
		/*if(pedido.getTotal().doubleValue()<10){
			support.getResult().addError("El monto mínimo para generar un pedido es de 10.00 pesos");
		}*/
		
		// Papel: Modificacion al validador para salvar pedidos con anticipo y totales en cero
		
				Set<PedidoDet> partidas=  pedido.getPartidas();
				
				boolean pasa=false; 
				for (PedidoDet p :partidas)
					{
					 if(p.getClave().equals("ANTICIPO"))
						{
						 pasa= true;
						 break;
						}
					 }
				 if (pasa)	
				 	{
					 if(pedido.getTotal().doubleValue()<0){
						 support.getResult().addError("El monto para facturar no debe ser menor a 0");
					 	}
				 }
				 else
				 {
					 if(pedido.getTotal().doubleValue()<10){
						 support.getResult().addError("El monto mnimo para generar un pedido es de 10.00 pesos");
					 		}
				 }
		
		
		if(getPedido().isDeCredito())
			validarCondicionesDeCredito(support);
		
		validarFormaDePago(support);
		validarFormaDeEnvio(support);
		if(getPedido().getPartidas().isEmpty())
			support.getResult().addError("El pedido sin partidas no es válido");
		if(StringUtils.isBlank(getPedido().getComprador())){
			support.getResult().addError("Registre el nombre de la persona que levanta el pedido ");
		}
		validarDireccion(support);
		
		if(c!=null){
			String MSG_VENTAS=c.getComentarios().get("VTA_MSG_RR");
			if(StringUtils.isNotBlank(MSG_VENTAS)){
				support.getResult().addWarning(MSG_VENTAS);
			}
		}
		
		//Validar si es anticipo
		if(getPedido().isAnticipo()){
			if(getPedido().getPartidas().size()!=1){
				support.getResult().addError("Un anticipo solo puede tener una partida de tipo ANTICIPO ");
				return;
			}else{
				PedidoDet det=getPedido().getPartidas().iterator().next();
				if(!det.getProducto().getClave().equals("ANTICIPO")){
					support.getResult().addError("Un anticipo solo puede tener una partida de tipo ANTICIPO ");
				}
				if(det.getProducto().isInventariable()){
					support.getResult().addError("Un anticipo no puede ser inventariable ");
				}
			}
			
		}
		//Validar Tipo de cambio
		if(getPedido().getMoneda().equals(MonedasUtils.DOLARES)){
			if(getPedido().getTc()<=1){
				support.getResult().addError("No existe en el sistema el tipo de cambio para el dia ");
			}
		}
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
								
		}else if(FormaDePago.CHECKPLUS.equals(getPedido().getFormaDePago())){
			if(c!=null && c.isDeCredito())				
				if(!c.getCredito().isCheckplus()){
					support.getResult().addError("A este cliente no se le permite forma de pago CheckPlus");
				}
			if(!getPedido().isDeCredito()){
				support.getResult().addError("No se permite CheckPlus en contado");
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
			//support.getResult().addError( "Crédito suspendido temporalmente ");	
			support.getResult().addError( "ENLAZAR LA LLAMADA DEL CLIENTE A CREDITO");	
		
		//Valida la linea de credito
		if(c.getCredito().getCreditoDisponible().doubleValue()<getPedido().getTotal().doubleValue()){
			//support.getResult().addError( "Línea de crédito SATURADA");
			support.getResult().addError( "ENLAZAR LA LLAMADA DEL CLIENTE A CREDITO");
			
		}
		
		//Valida el atraso maximo del cliente
		int plazo=c.getCredito().getPlazo();
		int atrasoMax=20;
		switch (plazo) {
		case 30:
			atrasoMax=20;
			break;
		case 45:
			atrasoMax=15;
			break;
		case 60:
			atrasoMax=15;
			break;
		case 75:
			atrasoMax=10;
			break;
		case 90:
			atrasoMax=7;
			break;
		default:
			break;
		}
		if(c.getCredito().getAtrasoMaximo()>atrasoMax){
			//String pattern="El cliente tiene un atraso superior a {0} dias (Llamar a Crédito)";
			String pattern="ENLAZAR LA LLAMADA DEL CLIENTE A CREDITO";
			support.getResult().addError(MessageFormat.format(pattern, atrasoMax));
		}
		
		/*
		 * // Modificacion por ordenes de Direccion General (Lic. Jose Sanchez) 14/08/2013
		if(c.getCredito().getAtrasoMaximo()>15){
			support.getResult().addError( "El cliente tiene un atraso superior a 15 dias (Llamar a Crédito)");
		}*/
		
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
