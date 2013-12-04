package com.luxsoft.sw3.ventas;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.luxsoft.siipap.model.Autorizacion2;

/**
 * Autorizacion general de pedidos
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@DiscriminatorValue("AUT_DE_PEDIDO")
public class AutorizacionDePedido extends Autorizacion2{
	
	
	public static enum Conceptos{
		PAGO_CONTRA_ENTREGA,DESCUENTO_ESPECIAL_CONTADO
	}

}
