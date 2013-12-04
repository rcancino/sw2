package com.luxsoft.siipap.swing;

import java.awt.KeyboardFocusManager;

import javax.swing.SwingUtilities;

import org.acegisecurity.context.SecurityContextHolder;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

import com.jgoodies.uif.util.UIFFocusTraversalPolicy;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.login.LoginWindow;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;




/**
 * Inicializa la aplicacion, El objeto principal de esta aplicacion es inicializar el Spring ApplicationContext
 * 
 * 
 * @author Ruben Cancino
 *
 */
public abstract class AbstractApplicationStarter {
	
	
	protected Logger logger=Logger.getLogger(getClass());
	
	
	
	
	protected AbstractApplicationStarter start(){
		
		
		AbstractApplicationContext ctx=null;
		
		try {
			
			configureActions();
			
			ctx=(AbstractApplicationContext)createApplicationContext();
			SwingUtilities.invokeAndWait(new Runnable(){
				public void run() {
					configureUI();					
					login();
				}
			});
//			Assert.notNull(SecurityContextHolder.getContext().getAuthentication(),"No esta firmado al sistema");
			
			return this;
			
		} catch (Exception e) {
			e.printStackTrace();			
			logger.error("Error al inicializar la aplicacion ",e);
			if(ctx!=null)
				ctx.close();
			exit();
			return null;
		}
		
	}
	
	protected void login(){
		boolean login=KernellSecurity.instance().isSecurityEnabled();
		if(login){
			LoginWindow w=new LoginWindow(){
				@Override
				public void loggedIn() {				
					startUI();
				}			
			};
			w.login();
		}else
			startUI();
	}
	
	protected void startUI(){		
		checkSetup();
		configureHelp();
		//Application app=(Application)ctx.getBean("application");
		Application.instance().open();
		//app.open();
	}
	
	protected void configureActions(){
	}
	
	protected  ApplicationContext createApplicationContext(){
		return new ClassPathXmlApplicationContext(getContextPaths());
	}
	
	protected abstract String[] getContextPaths();
	
	
	protected void configureUI() {		
	   // System.setProperty("apple.laf.useScreenMenuBar", "true");	
	    System.setProperty( "com.sun.java.swing.plaf.gtk.GTKLookAndFeel", "false");	
	   
	    SWExtUIManager.setup();	    
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
            setDefaultFocusTraversalPolicy(UIFFocusTraversalPolicy.DEFAULT);
	}
	
	 
	

	/**
	 * Checks whether a setup is necessary. For example, one can check
	 * whether the user has accepted the license agreement.
	 */
	protected void checkSetup() {
		/*
		if (!SWSetupManager.checkLicense()) 
			exit();
			*/
	}
	
	
	protected void configureHelp(){
		
	}
	
	
	protected void exit(){
		System.exit(1);
	}	 

}
