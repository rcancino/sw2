package com.luxsoft.siipap.swing.form2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.beans.Model;
import com.jgoodies.binding.value.ComponentValueModel;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.util.DefaultValidationResultModel;
import com.jgoodies.validation.util.PropertyValidationSupport;

@SuppressWarnings("unchecked")
public  class DefaultFormModel extends Model implements IFormModel{
	
	protected Logger logger=Logger.getLogger(getClass());
	
	protected final BeanWrapperImpl wrapper;
	protected final PresentationModel pmodel;
	private final ValidationResultModel rmodel;
	private  BeanValidator beanValidator;
	private String id;
	private boolean readOnly=false;
	
	public DefaultFormModel(final Object bean){
		this(bean,false);
	}
	
	public DefaultFormModel(final Object bean,boolean readOnly){
		wrapper=new BeanWrapperImpl(bean);
		pmodel=new PresentationModel(wrapper.getWrappedInstance());
		pmodel.addBeanPropertyChangeListener(new ValidationHandler());
		rmodel=new DefaultValidationResultModel();
		beanValidator=new BeanValidationImpl();
		setReadOnly(readOnly);
		init();
	}
	
	public DefaultFormModel(final Class clazz){		
		this(BeanUtils.instantiateClass(clazz));
		
	}
	
	/**
	 * @return the pmodel
	 */
	public PresentationModel getPmodel() {
		return pmodel;
	}

	
	
	/**
	 * Template method para inicializacion 
	 * 
	 */
	protected void init(){
		
	}
	
	@SuppressWarnings("unchecked")
	public Class getBaseBeanClass(){
		return (Class)getBaseBean().getClass();
		
	}

	
	public Object getBaseBean() {
		return wrapper.getWrappedInstance();
	}

	public ComponentValueModel getComponentModel(String propertyName) {
		ComponentValueModel rm=pmodel.getComponentModel(propertyName);
		rm.setEnabled(!isReadOnly());
		rm.setEditable(!isReadOnly());		
		return rm;
	}

	public ValueModel getModel(String propertyName) {
		return pmodel.getModel(propertyName);
	}
	

	public Object getValue(String property) {
		return wrapper.getPropertyValue(property);
	}
	
	public void setValue(String property,final Object value) {
		pmodel.setValue(property, value);
	}

	public Class getPropertyType(String propertyName) {
		return wrapper.getPropertyType(propertyName);
	}

	public ValidationResultModel getValidationModel() {
		return rmodel;
	}
	
	/**
	 * Valida el bean en cuestion utilizando Hibernate's Validation API
	 * 
	 */
	public ValidationResult validate(){
		
		//Obtener resultados desde el beanValidator
		final ValidationResult res=beanValidator.validate(getBaseBean());
		
		final Class clazz=getBaseBeanClass();
		final String role=ClassUtils.getShortName(clazz);
		final PropertyValidationSupport support =new PropertyValidationSupport(clazz,role);
		addValidation(support);
		support.getResult().addAllFrom(res);
		
		rmodel.setResult(support.getResult());
		
		return support.getResult();
	}
	
	/**
	 * Template method para agregar validaciones
	 * 
	 * @param support
	 */
	protected void addValidation(PropertyValidationSupport support){
		
	}
	
	public boolean hasErrors() {
		return rmodel.hasErrors();
	}

	public void addBeanPropertyChangeListener(PropertyChangeListener listener) {
		pmodel.addBeanPropertyChangeListener(listener);		
	}

	public void removeBeanPropertyChangeListener(PropertyChangeListener listener) {
		pmodel.removeBeanPropertyChangeListener(listener);
	}

	public String getId() {
		if(id==null){
			final String clazz=StringUtils.uncapitalize(ClassUtils.getShortName(getBaseBeanClass()));
			setId(clazz+".form"); 
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.form2.IFormModel#getMainModel()
	 */
	public PresentationModel getMainModel() {
		return getPmodel();
	}

	public void setReadOnly(boolean readOnly) {
		boolean oldValue=this.readOnly;
		this.readOnly = readOnly;
		firePropertyChange("readOnly", oldValue, readOnly);
	}
	
	private class ValidationHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			//String pattern="Prop: {0} newVal:{1}";
			//System.out.println(MessageFormat.format(pattern, evt.getPropertyName(),evt.getNewValue()));
			validate();
			
		}
		
	}

	
	public void dispose() {
	
		
	}

	public BeanValidator getBeanValidator() {
		return beanValidator;
	}

	public void setBeanValidator(BeanValidator beanValidator) {
		this.beanValidator = beanValidator;
	}


	

	

}
