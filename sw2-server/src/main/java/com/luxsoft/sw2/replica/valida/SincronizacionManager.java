package com.luxsoft.sw2.replica.valida;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.utils.LoggerHelper;

public class SincronizacionManager {
	
	Logger logger=LoggerHelper.getLogger();
	
	public void star(){
		ApplicationContext ctx=new ClassPathXmlApplicationContext("tasks.xml",getClass());
	}
	
	public static void main(String[] args) {
		new SincronizacionManager().star();
	}

}
