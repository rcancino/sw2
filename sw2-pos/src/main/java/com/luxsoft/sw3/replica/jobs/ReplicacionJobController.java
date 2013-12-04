package com.luxsoft.sw3.replica.jobs;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.sw3.services.POSDBUtils;

public class ReplicacionJobController {
	
	private static Logger logger=Logger.getLogger(DepositosReplicaJob.class);
	
	private static ReplicacionJobController INSTANCE;
	
	private ApplicationContext context;
	
	private ReplicacionJobController(){}
	
	public static ReplicacionJobController getInstace(){
		if(INSTANCE==null){
			logger.info("Inicializando controlador de replicación");
			INSTANCE=new ReplicacionJobController();
		}
		return INSTANCE;
	}
	
	protected String[] getConfigLocations() {
        return new String[] {                
        		"sw3-replica-jobs.xml"
        	//	,"sw3-replica-jobs2.xml"
        		//,"sw3-replica-jobs3.xml"
            };
    }
	
	public ApplicationContext getContext(){		
		if(context==null){
			context=new ClassPathXmlApplicationContext(
					getConfigLocations(),getClass()
					);
		}
		return context;
	}
	

	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		ReplicacionJobController.getInstace().getContext();
	}
}
 