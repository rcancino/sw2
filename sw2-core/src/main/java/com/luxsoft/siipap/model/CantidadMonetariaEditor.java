package com.luxsoft.siipap.model;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.util.Currency;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

/**
 * Permite convertir Strings en CantidadMonetaria usando el formato:
 * 
 * monto:moneda
 * ej:
 * 	256.50:MXN //Pesos
 *  256.50:USD //Dolares
 *  256.50:EUR //Euros
 * 
 * @author Ruben Cancino
 *
 */
public class CantidadMonetariaEditor extends PropertyEditorSupport{
	
	public static String DEFAULT_MONETARY_CURRENCY="MXN";

	@Override
	public String getAsText() {
		return getValue().toString();
	}

	/**
	 * Permite asignar el valor de la propiedad CantidadMonetaria con
	 * un formato de tipo monto:moneda
	 * ej:
	 * 		256.50:MXN //Pesos
	 *	  	256.50:USD //Dolares
	 *  	256.50:EUR //Euros
	 *  en donde la primera parte es el monto de la cantidad con dos decimales
	 *  y la moneda es posterior a ':'
	 *  Si no se especifica la moneda se ocupa el valor de DEFAULT_MONETARY_CURRENCY
	 *  
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		Assert.isTrue(StringUtils.isNotEmpty(text),"Error en formato para cantidad monetaria verifique que sea de tipo cantidad:moneda");
		String[] parts=text.split(":");
		BigDecimal valor=new BigDecimal(parts[0]);
		Currency mon=Currency.getInstance(DEFAULT_MONETARY_CURRENCY);
		if(parts.length>1)
			mon=Currency.getInstance(parts[1]);
		setValue(new CantidadMonetaria(valor,mon));
	}
	
	

}
