package com.luxsoft.siipap.security;

import javax.swing.UIManager;

import org.acegisecurity.BadCredentialsException;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXLoginDialog;
import org.jdesktop.swingx.JXLoginPanel;
import org.jdesktop.swingx.auth.LoginEvent;
import org.jdesktop.swingx.auth.LoginListener;
import org.jdesktop.swingx.auth.LoginService;

import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.service.LoginManager;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class AutorizacionDialog implements LoginListener{
	
	
	private Logger logger=Logger.getLogger(getClass());
	
	private final LoginService service;
	private final JXLoginDialog dialog;
	
	public AutorizacionDialog(final String role){
		
		service=new LoginService(){

			@Override
			public boolean authenticate(String name, char[] password,String server) throws Exception {
				User user=ServiceLocator2.getAutorizacionesManager().autorizarOperacionPorRole(name, new String(password), role);
				return user!=null;
				
			}
			
		};
		service.addLoginListener(this);
		dialog=new JXLoginDialog(service,null,null);
	}
	
	public  void login(){
		dialog.getPanel().setBannerText("SiipapWin EX");		
		dialog.setVisible(true);
	}
	

	public void loginCanceled(LoginEvent source) {
		
		
	}

	public void loginFailed(LoginEvent source) {
		logger.info("Login failed: "+source.getCause().getMessage());
		String msg="Error de acceso";
		if(source.getCause()!=null && source.getCause() instanceof BadCredentialsException){			
			msg+="\n Verifique sus credenciales";
		}else{
			msg+="\n"+source.getCause().getMessage();
		}
		dialog.getPanel().setErrorMessage(msg);
			
		
	}

	public void loginStarted(LoginEvent source) {
		logger.info("Login started, validando ROL");
	}

	public void loginSucceeded(LoginEvent source) {
		System.out.println("Usuario registrado exitosamente: "+LoginManager.getCurrentUser());
		loggedIn();
	}
	
	public void loggedIn(){
		
	}
	
	public boolean opreacionAutorizada(){
		return false;
	}
	
	public static void main(String[] args) {
		UIManager.put(JXLoginPanel.class.getCanonicalName() + ".loginString","Acceso");
		UIManager.put(JXLoginPanel.class.getCanonicalName() + ".cancelLogin","NO");
		
		SWExtUIManager.setup();
		AutorizacionDialog w=new AutorizacionDialog("CXC_AUTORIZACIONES");
		w.login();
	}

}
