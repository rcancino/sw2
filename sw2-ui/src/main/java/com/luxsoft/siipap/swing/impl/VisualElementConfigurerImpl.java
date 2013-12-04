package com.luxsoft.siipap.swing.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.luxsoft.siipap.swing.ResourceLocator;
import com.luxsoft.siipap.swing.VisualElement;
import com.luxsoft.siipap.swing.VisualElementConfigurer;



/**
 * Implementation of <code>VisualElementConfigurer</code> tha delegates
 * configuration to a <code>ResourceLocator</code> injected by Spring IoC Container.
 * 
 * <p> It also implements <code>BeanPostProcessor</code> to automatically configure all
 * beans tha implement <code>VisualElement</code> interface 
 * 
 * @author Ruben Cancino
 *
 */
public class VisualElementConfigurerImpl implements VisualElementConfigurer,BeanPostProcessor{
	
	private ResourceLocator resourceLocator;
	private Logger logger=Logger.getLogger(getClass());

	public void configure(VisualElement element,String id) {
		if(logger.isDebugEnabled()){
			logger.debug("Configurando elemento de tipo: "+element.getClass().getSimpleName());
			logger.debug("ResourceLoader: "+getResourceLocator());
		}
		
		String labelk=id+'.'+VisualElement.Type.LABEL.name().toLowerCase();
		String desck=id+'.'+VisualElement.Type.DESCRIPTION.name().toLowerCase();
		String toolk=id+'.'+VisualElement.Type.TOOLTIP.name().toLowerCase();
		String iconk=id+'.'+VisualElement.Type.ICON.name().toLowerCase();
		String imagk=id+'.'+VisualElement.Type.IMAGE.name().toLowerCase();
		element.setLabel(getResourceLocator().getMessage(labelk,labelk));
		element.setDescription(getResourceLocator().getMessage(desck,desck));
		element.setTooltip(getResourceLocator().getMessage(toolk,toolk));
		element.setIcon(getResourceLocator().getIcon(iconk));
		element.setImage(getResourceLocator().getImage(imagk));
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if(bean instanceof VisualElement)
			configure((VisualElement)bean,beanName);
		return bean;
	}

	public ResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}

	

}
