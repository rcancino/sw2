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

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.ChequeDevuelto;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.service.ClienteServices;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.utils.LoggerHelper;

public class CargoListener implements PostUpdateEventListener,PostInsertEventListener ,ApplicationContextAware{
	
	ApplicationContext context;
	Logger logger=LoggerHelper.getLogger();
	
	public void onPostInsert(PostInsertEvent event) {
		Object object=event.getEntity();
		if(Cargo.class.isAssignableFrom(object.getClass())){
			Cargo a=(Cargo)object;
			Cliente cliente=a.getCliente();
			if(cliente.getCredito()!=null){
				logger.info("Mandando actualizar en  el saldo del cliente: ");
				updateCliente(cliente);
			}
		}if(object instanceof ChequeDevuelto){
			Cargo a=(Cargo)object;
			Cliente cliente=a.getCliente();
			logger.info("Mandando actualizar en  el saldo de cheque devuelto: ");
			updateSaldoCheque(cliente);
		}if(object instanceof NotaDeCargo){
			NotaDeCargo nc=(NotaDeCargo)object;
			if(nc.getOrigen().equals(OrigenDeOperacion.CHE)){
				Cliente cliente=nc.getCliente();
				logger.info("Mandando actualizar en  el saldo de cheque devuelto: ");
				updateSaldoCheque(cliente);
			}
		}if(object instanceof NotaDeCreditoBonificacion){
			NotaDeCreditoBonificacion nc=(NotaDeCreditoBonificacion)object;
			if(nc.getOrigen().equals(OrigenDeOperacion.CHE)){
				Cliente cliente=nc.getCliente();
				logger.info("Mandando actualizar en  el saldo de cheque devuelto: ");
				updateSaldoCheque(cliente);
			}
		}if(object instanceof Aplicacion){
			Aplicacion nc=(Aplicacion)object;
			if(nc.getCargo().getOrigen().equals(OrigenDeOperacion.CHE)){
				Cliente cliente=nc.getCargo().getCliente();
				logger.info("Mandando actualizar en  el saldo de cheque devuelto: ");
				updateSaldoCheque(cliente);
			}
		}
	}
	
	public void onPostUpdate(PostUpdateEvent event) {
		Object object=event.getEntity();
		if(Cargo.class.isAssignableFrom(object.getClass())){
			Cargo a=(Cargo)object;
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
	
	private void updateSaldoCheque(final Cliente cliente){
		Runnable task=new Runnable() {
			public void run() {
				ClienteServices s=(ClienteServices)context.getBean("clienteServices");
				s.actualizarSaldoEnChequeDevueltos(cliente);
			}
			
		};
		TaskExecutor executor=(TaskExecutor)context.getBean("taskExecutor");
		executor.execute(task);
	}
	
	public void setApplicationContext(ApplicationContext applicationContext)throws BeansException {
		this.context=applicationContext;
		
	}
	

}
