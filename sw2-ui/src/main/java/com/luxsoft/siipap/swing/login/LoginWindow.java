package com.luxsoft.siipap.swing.login;

import javax.swing.UIManager;

import org.acegisecurity.BadCredentialsException;
import org.jdesktop.swingx.JXLoginDialog;
import org.jdesktop.swingx.JXLoginPanel;
import org.jdesktop.swingx.auth.LoginEvent;
import org.jdesktop.swingx.auth.LoginListener;
import org.jdesktop.swingx.auth.LoginService;

import com.luxsoft.siipap.service.LoginManager;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class LoginWindow implements LoginListener{
	
	private final LoginService service;
	private final JXLoginDialog dialog;
	
	public LoginWindow(){
		service=new SWXLoginService();
		service.addLoginListener(this);
		dialog=new JXLoginDialog(service,null,null);
	}
	
	public  void login(){
		dialog.getPanel().setBannerText("SiipapWin EX");		
		dialog.setVisible(true);
	}
	
	
	public static void main(String[] args) {
		UIManager.put(JXLoginPanel.class.getCanonicalName() + ".loginString","Acceso");
		UIManager.put(JXLoginPanel.class.getCanonicalName() + ".cancelLogin","NO");
		
		SWExtUIManager.setup();
		LoginWindow w=new LoginWindow();
		w.login();
	}

	public void loginCanceled(LoginEvent source) {
		// TODO Auto-generated method stub
		
	}

	public void loginFailed(LoginEvent source) {
		System.out.println("Login failed: "+source.getCause().getMessage());		
		String msg="Error de acceso";
		if(source.getCause()!=null && source.getCause() instanceof BadCredentialsException){			
			msg+="\n Verifique sus credenciales";
		}else{
			msg+="\n"+source.getCause().getMessage();
		}
		dialog.getPanel().setErrorMessage(msg);
			
		
	}

	public void loginStarted(LoginEvent source) {
		System.out.println("Login started....");
		
	}

	public void loginSucceeded(LoginEvent source) {
		System.out.println("Usuario registrado exitosamente: "+LoginManager.getCurrentUser());
		loggedIn();
	}
	
	public void loggedIn(){
		
	}

}
