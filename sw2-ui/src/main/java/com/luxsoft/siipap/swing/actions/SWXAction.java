package com.luxsoft.siipap.swing.actions;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.swing.SWBeans;
import com.luxsoft.siipap.swing.utils.MessageUtils;

/**
 * Tipo de acciones para ser ejecutadas desde el Framework. Mantiene una referencia al objeto Application y al contenedor
 * desde el cual fue inicializado 
 * 
 * @author Ruben Cancino
 *
 */
public abstract class SWXAction extends AbstractAction implements ApplicationContextAware,InitializingBean{
	
	protected ApplicationContext context;
	protected Application application;
	protected Logger logger=Logger.getLogger(getClass());

	
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context=applicationContext;
		Application app=(Application)context.getBean(SWBeans.Application.toString());
		setApplication(app);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			execute();
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex);
			MessageUtils.showError("Error ejecutando acción",ex);
		}
		
	}
	
	protected abstract void execute();
	
	
	
	/**
	 * Utitlity method para extraer objetos del contenedor. 
	 * 
	 * @param id
	 * @return
	 */
	public Object getContextBean(final String id){
		if(context.containsBean(id)){
			return context.getBean(id);
		}
		if(logger.isDebugEnabled()){
			String msg=MessageFormat.format("No existe el objeto {0} en el contenedor", id);
			logger.debug(msg);
		}
		return null;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(context,"El ApplicationContext es mandatorio para este tipo de acciones");
		Assert.notNull(application,"El objeto Application es mandatorio para este tipo de acciones");
	}
	
	

	public Application getApplication() {
		if(application==null)
			application=Application.instance();
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public ApplicationContext getContext() {
		return context;
	}
/**
	public void setContext(ApplicationContext context) {
		this.context = context;
	}
	
	**/
	
	public void setLabel(String label){
		putValue(SHORT_DESCRIPTION, label);
	}

}
