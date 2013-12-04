package com.luxsoft.siipap.swing.form2;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.luxsoft.siipap.annotation.UIProperty;

/**
 * Implementacion basica de BeanPropertiesExtractor
 * Extrae informacion importante para la UI relacionada con metadata (annoations)
 * de un bean
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public class BeanPropertiesExtractorImpl implements BeanPropertiesExtractor{

	/**
	 * Regresa una lista de las propiedades de un bean
	 * que se pueden ocupar en una forma
	 * 
	 * @param bean
	 * @return
	 */
	public Set<String> getUIProperties(Class clazz) {
		final Set<String> properties=new HashSet<String>();
		
		for(Field f:clazz.getDeclaredFields()){
			if(f.isAnnotationPresent(UIProperty.class)){
				properties.add(f.getName());
			}			
		}
		return properties;
	}
	
	/**
	 * Regresa verdadero si la propriedad es de solo lectura, segun 
	 * las annotations correspondientes a la pripiedad. Regresa verdadero
	 * en caso de cualquier error
	 * 
	 * @param bean
	 * @param property
	 * @return
	 */	
	public boolean isReadOnly(final Class clazz, final String property){
		/**
		Field f=findFiled(bean, property);
		if(f!=null){
			if(f.isAnnotationPresent(UIProperty.class)){
				return f.getAnnotation(UIProperty.class).readOnly();
			}
		}	
		**/
		if(getUIAnnotation(clazz, property)!=null)
			return getUIAnnotation(clazz, property).readOnly();
		return false;
	}
	
	/**
	 * Obtiene la etiqueta por defecto para la propiedad determinada
	 * 
	 * @param bean
	 * @param property
	 * @return
	 */
	public String getLabel(final Class clazz, final String property){
		if(getUIAnnotation(clazz, property)!=null)
			return getUIAnnotation(clazz, property).label();
		return null;
	}
	
	private UIProperty getUIAnnotation(final Class bean ,final String property){
		Field f=findFiled(bean, property);
		if(f!=null){
			if(f.isAnnotationPresent(UIProperty.class)){
				return f.getAnnotation(UIProperty.class);
			}
		}		
		return null;
	}
	
	/**
	 * Permite presentar las propiedades de tipo double como un porcentage
	 * reflejado por un factor
	 * 
	 * @param bean
	 * @param proprty
	 * @return
	 */	
	public boolean isPorcentage(Class clazz, String property) {
		if(getUIAnnotation(clazz, property)!=null)
			return getUIAnnotation(clazz, property).isPorcentage();
		return false;
	}

	
	/**
	 * Busca en forma recursiva el Field para una propiedad de un Bean
	 * 
	 * @param bean
	 * @param property
	 * @return
	 */
	private Field findFiled(Class clazz,String property){
		try {
			Field f=clazz.getDeclaredField(property);
			if(f==null){
				return findFiled(clazz.getSuperclass(), property);
			}
			return f;
		} catch (Exception e) {
			return null;
		}
	}

	
}
