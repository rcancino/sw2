package com.luxsoft.sw2.server.services;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.sw2.replica.ReplicaManager;
import com.luxsoft.utils.LoggerHelper;

/**
 * 
 * @author Ruben Cancino 
 *
 */
public class LocalServerManager {
	
	private static  LocalServerManager INSTANCE;
	
	static Logger logger=LoggerHelper.getLogger();
	
	ApplicationContext context;
	
	private LocalServerManager(){
		initContext();
	}
	
	public ApplicationContext getContext(){
		/*if(context==null){
			initContext();
		}*/
		return context;
	}
	
	private void initContext(){
		//logger.info("Inicializando ServiceLocator2 ....");
		ServiceLocator2.instance().getContext();
		//logger.info("Inicializando Server context.... conectado a: "+DBUtils.getAplicationDB_URL());
		context=new ClassPathXmlApplicationContext(
				new String[]{
						"classpath:/spring/server-local-jms-context.xml"
						}
				,ServiceLocator2.instance().getContext()
		);
	}
	
	public static void close(){
		if(INSTANCE!=null){
			logger.info("Cerrando server manager context...");			
			//((ClassPathXmlApplicationContext)INSTANCE.getContext().getParent()).close();
			((ClassPathXmlApplicationContext)INSTANCE.getContext()).close();
		}
	}
	
	public static LocalServerManager getInstance(){
		if(INSTANCE==null){
			INSTANCE=new LocalServerManager();
			
		}
		return INSTANCE;
	}
	
	public static synchronized JmsTemplate getLocalJmsTemplate(){
		return (JmsTemplate)getInstance().getContext().getBean("localJmsTemplate");
	}
	
	public static synchronized JmsTemplate getCentralJmsTemplate(){
		return (JmsTemplate)getInstance().getContext().getBean("centralJmsTemplate");
	}
	
	public static synchronized ReplicaManager getReplicaManager(){
		return (ReplicaManager)getInstance().getContext().getBean("replicaManager");
	}
	
	public static void main(String[] args) throws Exception{
		getInstance();
		//manager.getInstance().context;
		//manager.getBrokers();
	}

}
