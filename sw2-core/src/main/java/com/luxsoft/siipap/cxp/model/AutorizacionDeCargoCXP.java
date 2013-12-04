package com.luxsoft.siipap.cxp.model;

import java.text.MessageFormat;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.luxsoft.siipap.model.Autorizacion2;

/**
 * Autorizacion para el pago de una factura de cxp  
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@DiscriminatorValue("AUT_PAGO_CXP")
public class AutorizacionDeCargoCXP extends Autorizacion2{
	
	public String toString(){
		return MessageFormat.format("Pago autorizado por: {0} en:{1,date,long}",getAutorizo(),getFechaAutorizacion());
	}

}
