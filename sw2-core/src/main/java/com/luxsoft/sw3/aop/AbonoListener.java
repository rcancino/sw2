package com.luxsoft.sw3.aop;

import org.apache.log4j.Logger;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.task.TaskExecutor;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.utils.LoggerHelper;

public class AbonoListener implements PostUpdateEventListener,PostInsertEventListener ,ApplicationContextAware{
	
	ApplicationContext context;
	Logger logger=LoggerHelper.getLogger();
	
	public void onPostInsert(PostInsertEvent event) {
		Object object=event.getEntity();
		if(NotaDeCredito.class.isAssignableFrom(object.getClass())){
			NotaDeCredito a=(NotaDeCredito)object;
			Cliente cliente=a.getCliente();
			if(cliente.getCredito()!=null){
				logger.info("Mandando actualizar en  el saldo del cliente: ");
				updateCliente(cliente);
			}
		}
	}
	
	public void onPostUpdate(PostUpdateEvent event) {
		Object object=event.getEntity();
		if(Abono.class.isAssignableFrom(object.getClass())){
			Abono a=(Abono)object;
			Cliente cliente=a.getCliente();
			if(cliente.getCredito()!=null){
				logger.info("Mandando actualizar en  el saldo del cliente: ");
				updateCliente(cliente);
			}
		}
		
	}
	
	private void updateCliente(final Cliente cliente){
		Runnable task=new ActualizarSaldoAtrasoClienteTask(cliente);
		TaskExecutor executor=(TaskExecutor)context.getBean("taskExecutor");
		executor.execute(task);
	}
	
	public void setApplicationContext(ApplicationContext applicationContext)throws BeansException {
		this.context=applicationContext;
		
	}
	
	
	

}
