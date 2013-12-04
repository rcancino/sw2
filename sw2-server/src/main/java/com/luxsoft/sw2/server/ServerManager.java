package com.luxsoft.sw2.server;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import com.luxsoft.sw2.replica.ReplicaManager;
import com.luxsoft.sw2.replica.remote.RequestService;
import com.luxsoft.utils.LoggerHelper;

/**
 * Repositorio central (Facade) para los servicios centralizados 
 * 
 * @author Ruben Cancino 
 *
 */
public class ServerManager {
	
	private static  ServerManager INSTANCE;
	
	static Logger logger=LoggerHelper.getLogger();
	
	ApplicationContext context;
	
	private ServerManager(){
		initContext();
	}
	
	public ApplicationContext getContext(){
		if(context==null){
			initContext();
		}
		return context;
	}
	
	private void initContext(){
		logger.info("Inicializando ServerManager context ....");		
		context=new ClassPathXmlApplicationContext(
				new String[]{
						"classpath:/spring/server-jms-replica.xml"
						//"classpath:/spring/server-jms-context.xml"
						//, "classpath*:/spring/importacion-jms-*.xml"
						}
				);		
	}
	
	
	
	public static void close(){
		if(INSTANCE!=null){
			logger.info("Cerrando server manager context...");
			((ClassPathXmlApplicationContext)INSTANCE.getContext()).close();
		}
	}
	
	public static ServerManager getInstance(){
		if(INSTANCE==null){
			INSTANCE=new ServerManager();
			
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
	
	public static synchronized RequestService getRequestService(){
		return (RequestService)getInstance().getContext().getBean("requestService");
	}
	
	public static void main(String[] args) throws Exception{
		RequestService service=getRequestService();
		service.requestSqlData("select PRODUCTO_ID,CLAVE,from SX_PRODUCTOS");
	}

}
