package com.luxsoft.sw2.replica.valida;

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
public class ConnectionServices {
	
	Logger logger=Logger.getLogger(getClass());
	
	private ApplicationContext context;
	
	private static ConnectionServices INSTANCE;
	
	private ConnectionServices(){
		
	}
	
	/** Singleton implementation ***/
	
	
	
	public static ConnectionServices getInstance(){
		if(INSTANCE==null)
			INSTANCE=new ConnectionServices();
		return INSTANCE;
	}
	
	public static synchronized void close(){
		if(INSTANCE!=null){
			getInstance().closeContext();
		}
	}
	
	
	
	public ApplicationContext getContext(){		
		if(context==null){
			context=new ClassPathXmlApplicationContext(
					"jdbc.xml",getClass()
					);
		}
		return context;
	}
	/*
	public synchronized HibernateTemplate getHibernateTemplate(Long sucursalId){
		
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
	}*/
	
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
		case 11:
			name="vertiz176"+name;
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
	
	


}
