package com.luxsoft.sw3.impap.ui;

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.security.AutorizacionWindow;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.MessageUtils;


public class KernellUtils {
	
	public static User buscarUsuarioPorPassword(String password){
		return KernellSecurity.instance().findUser(password, ServiceLocator2.getHibernateTemplate());
	}
	
	public static User buscarUsuario(){
		AutorizacionWindow window=new AutorizacionWindow("Autorizacion en línea");
		window.setConComentario(false);
		window.setManager(ServiceLocator2.getAutorizacionesManager());
		window.open();
		if(!window.hasBeenCanceled()){
			User user=window.getUser().getUser();
			System.out.println(user);
			return user;
		}
		return null;
	}
	
	public static boolean validarAcceso(String role){
		User user=buscarUsuario();
		if(user!=null){
			if(user.hasRole(role)){
				return true;
			}else{
				MessageUtils.showMessage("No tiene el Rol: "+role, "Acceso denegado");
			}
		}
		return false;
	}
	
	
	/**
	 * Forma sencilla para localizar un usuario
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	protected static class PasswordDialog extends SXAbstractDialog{		

		public PasswordDialog() {
			super("Autentificando usuario");
		}
		
		private JTextField userField;
		private JPasswordField passwordField;

		@Override
		protected JComponent buildContent() {
			
			JPanel panel=new JPanel(new BorderLayout());
			
			FormLayout layout=new FormLayout("p,2dlu,100dlu","");
			DefaultFormBuilder builder =new DefaultFormBuilder(layout);
			//builder.setLeadingColumnOffset(1);
			builder.setDefaultDialogBorder();
			userField=new JTextField(20);
			passwordField=new JPasswordField(20);
			passwordField.addActionListener(getOKAction());
			builder.append("Usuario",userField);
			builder.append("Password",passwordField);
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			getCancelAction().putValue(Action.NAME, "Cancelar");
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		public String getPaswword(){
			return new String(passwordField.getPassword());
		}
		
		public String getUserName(){
			return userField.getText();
		}
		
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				buscarUsuario();
				System.exit(0);
			}

		});
	}

}
