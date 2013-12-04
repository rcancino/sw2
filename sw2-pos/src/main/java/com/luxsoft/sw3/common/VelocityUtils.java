package com.luxsoft.sw3.common;

import java.io.StringWriter;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
//import org.apache.velocity.app.VelocityEngine;
//import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * Auxiliar en el uso de Velocity engine
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class VelocityUtils {
	
	private static Logger logger=Logger.getLogger(VelocityUtils.class);
	
	static {
		try {
			//Velocity.setProperty("resource.loader", "class");
			//Velocity.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
			//Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			//Velocity.
			Velocity.init();
			System.out.println(Velocity.getProperty("resource.loader"));
		} catch (Exception e) {
			logger.error(ExceptionUtils.getRootCauseMessage(e),e);
		}
	}
	
	/**
	 * Procesa un velocity template directamente del classpath
	 * 
	 * @param resourcePath
	 * @return
	 */
	public static String processResource(String resourcePath,VelocityContext context){
		
		StringWriter writer=new StringWriter();
		try {
			Velocity.mergeTemplate(resourcePath,"ISO-8859-1", context, writer);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getRootCauseMessage(e),e);
		}
		return writer.toString();
	}

}
