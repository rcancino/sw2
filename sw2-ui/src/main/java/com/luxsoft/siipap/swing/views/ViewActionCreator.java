package com.luxsoft.siipap.swing.views;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.luxsoft.siipap.swing.View;


/**
 * Clase que genera una accion adecuada para mostrar una visata
 * y la registra en el ActionFactory. 
 * 
 * @author Ruben Cancino
 *
 */
public class ViewActionCreator implements BeanPostProcessor{
	
	private Logger logger=Logger.getLogger(getClass());
	
	public void createShowViewAction(final View view){
		if(view.getId()==null){
			logger.error("No se permite registrar vistas con id nulo");
			return;
		}
		if(logger.isDebugEnabled()){
			logger.debug("Creando y configurando accion para vista: "+view.getId());
		}
		/*
		ShowViewAction action=new ShowViewAction(view.getId());
		if(view instanceof VisualElement){
			VisualElement ve=(VisualElement)view;
			action.putValue(Action.NAME, ve.getLabel());
			action.putValue(Action.SHORT_DESCRIPTION, ve.getTooltip());
			action.putValue(Action.LONG_DESCRIPTION, ve.getDescription());
			action.putValue(Action.SMALL_ICON, ve.getIcon());
		}
		
		ActionFactory.instance().registerAction(action, view.getId());
		*/
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if(bean instanceof View){
			createShowViewAction((View)bean);
		}
		return bean;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {		
		return bean;
	}

	

}
