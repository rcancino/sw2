package com.luxsoft.siipap.swing.actions;

import javax.swing.Action;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;


/**
 * BeanPostProcessor que configura las acciones con ayuda de un ActionConfigurer
 * 
 * @author Ruben Cancino
 *
 */
public class ActionDecorator implements BeanPostProcessor{
	
	private ActionConfigurer actionConfigurer;
	private Logger logger=Logger.getLogger(getClass());
	
	/**
	 * Configures an action object
	 * 
	 * @param action
	 * @param id
	 */
	public void configAction(Action action,String id){
		if(logger.isDebugEnabled()){
			logger.debug("Configurando accion:  "+id);
		}
		getActionConfigurer().configure(action,id);
		action.putValue("ID",id);
	}

	/**
	 * Nothing to do before actions are set
	 */
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {		
		return bean;
	}

	/**
	 * Configs every bean that implements <code>javax.swing.Action</code>
	 */
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if(bean instanceof Action)
			configAction((Action)bean,beanName);
		return bean;
	}
	
	/**
	 * ActionConfigurer injected through Spring IoC container 
	 * 
	 * @return
	 */
	public ActionConfigurer getActionConfigurer() {
		return actionConfigurer;
	}

	public void setActionConfigurer(ActionConfigurer actionConfigurer) {
		this.actionConfigurer = actionConfigurer;
	}

}
