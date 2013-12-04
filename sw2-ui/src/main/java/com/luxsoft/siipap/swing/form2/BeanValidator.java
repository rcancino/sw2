package com.luxsoft.siipap.swing.form2;

import com.jgoodies.validation.ValidationResult;

/**
 * Facilita el acoplamiento de JGoodies'validation con alguna otra libreria
 * de validacion
 *  
 * @author Ruben Cancino
 *
 */
public interface BeanValidator {

	/**
	 * Obtiene un ValidationResult del estado actual del bean
	 * 
	 * @param bean
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public abstract ValidationResult validate(final Object bean);

}