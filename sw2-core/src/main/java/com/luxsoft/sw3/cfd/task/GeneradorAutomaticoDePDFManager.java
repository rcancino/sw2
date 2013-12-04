package com.luxsoft.sw3.cfd.task;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.utils.LoggerHelper;

public class GeneradorAutomaticoDePDFManager {
	
Logger logger=LoggerHelper.getLogger();
	
	public void start(){
		ApplicationContext ctx=new ClassPathXmlApplicationContext("tasks.xml",getClass());
		logger.info("Contenxto incializado..."+ctx.getStartupDate());
	}
	

	public static void main(String[] args) {
		new GeneradorAutomaticoDePDFManager().start();
	}
}
