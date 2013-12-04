package com.luxsoft.siipap.util;

import java.text.MessageFormat;

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;

public final class ValidationUtils {
	
	/**
	 * Imprime en el System.out el valor de las propiedades invalidas
	 * para el bean
	 * 
	 * @param bean
	 */
	public static void debugValidation(Object bean){
		final InvalidValue[] invalid=validate(bean);
		for(InvalidValue iv:invalid){
			String pattern="Error en propiedad: {0} msg:{1} valor:{2}";
			System.out.println(MessageFormat.format(pattern, iv.getPropertyName(),iv.getMessage(),iv.getValue()));
		}
	}
	
	/**
	 * Extrae valores invalidos del bean
	 * 
	 * @param bean
	 * @return
	 */
	public static InvalidValue[] validate(Object bean){
		final ClassValidator validator=new ClassValidator(bean.getClass());
		final InvalidValue[] invalid=validator.getInvalidValues(bean);
		return invalid;
	}
	
	public static boolean isValid(Object bean){
		return validate(bean).length==0;
	}
	
	public static String validar(Object bean){
		InvalidValue[] vals=validate(bean);
		if(vals.length>0){
			StringBuffer buffer=new StringBuffer();
			for(InvalidValue iv:vals){
				buffer.append(MessageFormat.format("Valor invalido: {0} propiedad: {1}  Message: {2}",iv.getValue(),iv.getPropertyName(),iv.getMessage()));
			}
		}
		return null;
	}
			
}
