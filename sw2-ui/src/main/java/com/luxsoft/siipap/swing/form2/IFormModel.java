package com.luxsoft.siipap.swing.form2;

import java.beans.PropertyChangeListener;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.value.ComponentValueModel;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.ValidationResultModel;

/**
 * Describe un modelo para implementar formas para un bean base con soporte a validacion
 * y que genera instancias ValueModel para las propiedades del mismo  
 * 
 * @author Ruben Cancino
 *
 */
public interface IFormModel {
	
	/**
	 * Determina si el estado es para solo lectura
	 * 
	 * @return
	 */
	public boolean isReadOnly();
	
	/**
	 * Identificador unico de este FormModel
	 * 
	 * @return
	 */
	public String getId();
	
	/**
	 * Bean base del modelo
	 * 
	 * @return
	 */
	public Object getBaseBean();
	
	/**
	 * Clase del bean base
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Class getBaseBeanClass();
	
	/**
	 * Regresa un ValueModel para la propiedad indicada. Es posible
	 * que las implementaciones puedan regresar un ValueModel para propiedades
	 * anidadas Eje: Articulo.linea.nombre
	 * 
	 * @param propertyName
	 * @return
	 */
	public ValueModel getModel(final String propertyName);
	
	/**
	 * Regresa un ComponentValueModel para la propiedad indicada. Es posible
	 * que las implementaciones puedan regresar un ComponentValueModel para propiedades
	 * anidadas Eje: Articulo.linea.nombre
	 * 
	 * @param propertyName
	 * @return
	 */
	public ComponentValueModel getComponentModel(final String propertyName);
	
	/**
	 * Regresa el valor de una propiedad, la cual puede ser nested
	 * 
	 * @param property
	 * @return
	 */
	public Object getValue(final String property);
	
	
	public void setValue(final String property,final Object value);
	
	/**
     * Checks and answers whether this model's validation result has errors.
     * 
     * @return true if the validation result has errors, false otherwise
     * 
     * @see #getSeverity()
     * @see #hasMessages()
     */
    boolean hasErrors();
    
    /**
     * Regresa el tipo de la propriedad ej:
     *  
     *  
     *  
     * @param property
     * @return
     */
    @SuppressWarnings("unchecked")
	public Class getPropertyType(final String propertyName);
    
    /**
     * Valida el bean base. Es posible que las implementaciones puedan validar
     * propiedades que en si sean otro bean
     * Eje Articulo.linea.nombre
     * 
     * @return
     */
    public ValidationResult validate();
    
    /**
     * Acceso al ValidationResultModel
     * 
     * @return
     */
    public ValidationResultModel getValidationModel();    
    
    
    public PresentationModel getMainModel();
    
    
    /**
     * Adds a PropertyChangeListener to the list of bean listeners. The 
     * listener is registered for all bound properties of the target bean.<p>
     * 
     * The listener will be notified if and only if this BeanAdapter's current 
     * bean changes a property. It'll not be notified if the bean changes.<p>
     *  
     * If listener is <code>null</code>, no exception is thrown and 
     * no action is performed.
     *
     * @param listener      the PropertyChangeListener to be added
     *
     * @see #removeBeanPropertyChangeListener(PropertyChangeListener)
     * @see #removeBeanPropertyChangeListener(String, PropertyChangeListener)
     * @see #addBeanPropertyChangeListener(String, PropertyChangeListener)
     * @see #getBeanPropertyChangeListeners()
     */
    public  void addBeanPropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * Removes a PropertyChangeListener from the list of bean listeners. 
     * This method should be used to remove PropertyChangeListeners that 
     * were registered for all bound properties of the target bean.<p>
     * 
     * If listener is <code>null</code>, no exception is thrown and 
     * no action is performed.
     *
     * @param listener      the PropertyChangeListener to be removed
     * 
     * @see #addBeanPropertyChangeListener(PropertyChangeListener)
     * @see #addBeanPropertyChangeListener(String, PropertyChangeListener)
     * @see #removeBeanPropertyChangeListener(String, PropertyChangeListener)
     * @see #getBeanPropertyChangeListeners()
     */
    public void removeBeanPropertyChangeListener(PropertyChangeListener listener);
    
   /**
    * Release recursos
    * 
    */
    public void dispose();

}
