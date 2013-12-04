package com.luxsoft.sw3.replica;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;



/**
 * Services Facade para la replicacion MySQL - MySQL
 * 
 * @author Ruben Cancino
 *
 */
public class ReplicaServices {
	
	Logger logger=Logger.getLogger(getClass());
	
	private ApplicationContext context;
	
	private static ReplicaServices INSTANCE;
	
	private ReplicaServices(){
		
	}
	
	/** Singleton implementation ***/
	
	
	
	public static ReplicaServices getInstance(){
		if(INSTANCE==null)
			INSTANCE=new ReplicaServices();
		return INSTANCE;
	}
	
	public static synchronized void close(){
		if(INSTANCE!=null){
			getInstance().closeContext();
		}
	}
	
	protected String[] getConfigLocations() {
        return new String[] {                
        		"sw3-replicationContext.xml" 
                //,"classpath*:spring/sw3-applicationContext.xml" // for modular projects
                //,"classpath*:spring/sw3-serviceContext.xml" // for service layers
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
	
	public synchronized HibernateTemplate getHibernateTemplate(Long sucursalId){
		//if(sucursalId==3L)
			  //return Services.getInstance().getHibernateTemplate();
		String name="_hibernateTemplate";
		switch (sucursalId.intValue()) {
		case 2:
			name="calle4"+name;
			break;
		case 3:
			name="tacuba"+name;
			break;
		case 5:
			name="bolivar"+name;
			break;
		case 6:
			name="andrade"+name;
			break;
		case 7:
			name="queretaro"+name;
			break;
		case 9:
			name="cincoFebrero"+name;
			break;
		default:
			break;
		}
		return (HibernateTemplate)getContext().getBean(name);
	}
	
	public synchronized JdbcTemplate getJdbcTemplate(Long sucursalId){
		String name="_jdbcTemplate";
		switch (sucursalId.intValue()) {
		case 2:
			name="calle4"+name;
			break;
		case 3:
			name="tacuba"+name;
			break;
		case 5:
			name="bolivar"+name;
			break;
		case 6:
			name="andrade"+name;
			break;
		case 7:
			name="queretaro"+name;
			break;
		case 9:
			name="cincoFebrero"+name;
			break;
		default:
			break;
		}
		return (JdbcTemplate)getContext().getBean(name);
	}
	
	protected synchronized void closeContext(){
		((ClassPathXmlApplicationContext)INSTANCE.getContext()).close();
		context=null;
	}
	
	

	public static void main(String[] args) {
		ReplicaServices.getInstance().getHibernateTemplate(7L);
	}

}
