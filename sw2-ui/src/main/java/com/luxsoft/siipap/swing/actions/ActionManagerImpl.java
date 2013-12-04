package com.luxsoft.siipap.swing.actions;

import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Action;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import com.luxsoft.siipap.service.KernellSecurity;


public class ActionManagerImpl implements ActionManager,ApplicationContextAware,InitializingBean{
	
	private ApplicationContext context;
	private ActionConfigurer configurer;
	private Logger logger=Logger.getLogger(getClass());
	
	/**
	 * Buffer para almacenar beans de tipo action
	 */
	private Map<String,Action > actionMap=new TreeMap<String, Action>();

	/**
	 * Obtiene la accion solicitada ya sea del contenedor del contenedor Spring o bien del buffer temporal
	 * 
	 */
	public Action getAction(final String id) {
		//System.out.println("BUSCANDO AUT PARA: "+id);
		if(context.containsBean(id)){
			Object obj=context.getBean(id);
			
			if(obj instanceof Action){				
				Action action=(Action)obj;
				action.setEnabled(KernellSecurity.instance().isActionGranted(action));
				return action;
			}else{
				String msg=MessageFormat.format("El bean con id: {0} no es una implementacion de javax.swing.Action",id);
				logger.error(msg);
				return ActionUtils.getNotFoundAction(id,msg);
			}				
		}else if(actionMap.containsKey(id)){
			return actionMap.get(id);
		
		}else{
			String msg=MessageFormat.format("Ne existe una Accion con id: {0} registrada en el contexto de este ActionManager en en su buffer temporal", id);
			logger.error(msg);
			return ActionUtils.getNotFoundAction(id,msg);
		}		
	}
	
	
	
	/**
	 * Permite registrar acciones y las almacena fuera en un buffer Map
	 * Es util para registrar acciones no administradas por Spring. Es ampliamente recomendable que 
	 * las acciones sean administradas por Spring por lo tanto debe usar este metodo solo para casos
	 * esporadicos
	 * 
	 */
	public Action registerAction(Action a,String id){
		if(actionMap.containsKey(id)){
			String msg=MessageFormat.format("La Accion {0} ya existe y esta registrada en este ActionManager", id);
			logger.debug(msg);
			return a;
		}else{
			configure(a, id);
			actionMap.put(id, a);
			return a;
		}
	}

	public void configure(Action action, String id) {		
		getConfigurer().configure(action, id);
		
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context=applicationContext;
	}

	public ActionConfigurer getConfigurer() {
		return configurer;
	}

	public void setConfigurer(ActionConfigurer configurer) {
		this.configurer = configurer;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(getConfigurer(),"La propiedad configurer es mandatoria");		
	}
	
	
	

}
