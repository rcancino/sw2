package com.luxsoft.siipap.swing.form2;

import java.util.Set;

/**
 * Implementacion basica de BeanPropertiesExtractor
 * Extrae informacion importante para la UI relacionada con metadata (annoations)
 * de un bean
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public interface BeanPropertiesExtractor {
	
	
	/**
	 * Regresa una lista de las propiedades de un bean
	 * que se pueden ocupar en una forma
	 * 
	 * @param bean
	 * @return
	 */
	
	public Set<String> getUIProperties(final Class clazz);
	
	/**
	 * Regresa verdadero si la propriedad es de solo lectura, segun 
	 * las annotations correspondientes a la pripiedad. Regresa verdadero
	 * en caso de cualquier error
	 * 
	 * @param bean
	 * @param property
	 * @return
	 */
	public boolean isReadOnly(final Class clazz, final String property);
	
	/**
	 * Obtiene la etiqueta por defecto para la propiedad determinada
	 * 
	 * @param bean
	 * @param property
	 * @return
	 */
	public String getLabel(final Class bean, final String property);
	
	/**
	 * Permite presentar las propiedades de tipo double como un porcentage
	 * reflejado por un factor
	 * 
	 * @param bean
	 * @param proprty
	 * @return
	 */	
	public boolean isPorcentage(final Class clazz,final String property);

}
