package com.luxsoft.siipap.swing.form2;

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.springframework.util.ClassUtils;

import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.util.PropertyValidationSupport;

/**
 * Implementacion de BeanValidator para hibernate
 * 
 * @author Ruben Cancino
 *
 */
public class BeanValidationImpl implements BeanValidator {
	
	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.compras.catalogos.BeanValidation#validate(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	/*
	public ValidationResult validate(final Object bean){
		final Class clazz=bean.getClass();
		final String role=ClassUtils.getShortName(clazz);
		//final PropertyValidationSupport support =new PropertyValidationSupport(clazz,role);
		final ValidationResult res=new ValidationResult();
		
		final ClassValidator validator=new ClassValidator(clazz);
		final InvalidValue[] invalid=validator.getInvalidValues(bean);
		for(InvalidValue iv:invalid){
			String propName=iv.getPropertyName();						
			//support.addError(propName, iv.getMessage());
			res.addError(iv.getMessage());
		}
		//final ValidationResult res=support.getResult();
		return res;
	}
	*/
	
	public ValidationResult validate(final Object bean){
		final Class clazz=bean.getClass();
		final String role=ClassUtils.getShortName(clazz);
		final PropertyValidationSupport support =new PropertyValidationSupport(clazz,role);
		
		final ClassValidator validator=new ClassValidator(clazz);
		final InvalidValue[] invalid=validator.getInvalidValues(bean);
		for(InvalidValue iv:invalid){
			
			String propName=iv.getPropertyName();						
			support.addError(propName, iv.getMessage());
		}
		final ValidationResult res=support.getResult();
		return res;
	}

}
