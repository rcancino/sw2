package com.luxsoft.sw3.aop;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.task.TaskExecutor;


import com.luxsoft.siipap.cxc.model.Juridico;
import com.luxsoft.siipap.cxc.service.ClienteServices;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.core.ClienteManager;
import com.luxsoft.utils.LoggerHelper;

public class JuridicoListener implements PostInsertEventListener ,ApplicationContextAware{
	
	ApplicationContext context;
	Logger logger=LoggerHelper.getLogger();
	
	public void onPostInsert(PostInsertEvent event) {
		Object object=event.getEntity();
		if(object instanceof Juridico){
			Juridico a=(Juridico)object;
			Cliente cliente=a.getCargo().getCliente();
			logger.info("Mandando actualizar en  el cliente: ");
			updateCliente(cliente);
		}
	}
	
	
	
	private void updateCliente(final Cliente cliente){
		Runnable task=new Runnable() {
			
			public void run() {
				ClienteManager manager=(ClienteManager)context.getBean("clienteManager");
				Cliente cc=manager.get(cliente.getId());
				cc.setJuridico(true);
				cc.getLog().setModificado(new Date());
				manager.getClienteDao().save(cc);
			}
		};
		TaskExecutor executor=(TaskExecutor)context.getBean("taskExecutor");
		executor.execute(task);
	}
	
	public void setApplicationContext(ApplicationContext applicationContext)throws BeansException {
		this.context=applicationContext;
		
	}
	

}
