package com.luxsoft.siipap.security;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;


import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jdesktop.swingx.JXBusyLabel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.Model;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.service.AutorizacionManager;
import com.luxsoft.siipap.service.AutorizacionesManager;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.ShortBeanValidationImpl;

import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class AutorizacionWindow extends AbstractForm{
	
	private String headerTitle="Esta operación requiere autorización";
	private String headerDesc="";
	private boolean conComentario=true;
	
	private AutorizacionesManager manager;

	public AutorizacionWindow(String title) {
		super(new DefaultFormModel(UserModel.class));
		setTitle(title);
		DefaultFormModel dfm=(DefaultFormModel)model;
		dfm.setBeanValidator(new ShortBeanValidationImpl());
		
	}
	
	
	public boolean isConComentario() {
		return conComentario;
	}

	public void setConComentario(boolean conComentario) {
		this.conComentario = conComentario;
	}


	private JXBusyLabel bussyLabel=new JXBusyLabel();
	
	private HeaderPanel header;
	
	public HeaderPanel getHeader(){
		if(header==null){
			header=new HeaderPanel(getHeaderTitle(),getHeaderDesc());
		}
		return header;
	}
	
	public String getHeaderTitle() {
		return headerTitle;
	}



	public void setHeaderTitle(String headerTitle) {
		this.headerTitle = headerTitle;
	}



	public String getHeaderDesc() {
		return headerDesc;
	}



	public void setHeaderDesc(String headerDesc) {
		this.headerDesc = headerDesc;
	}



	protected JComponent buildHeader(){
		return getHeader();
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("50dlu,3dlu,150dlu,3dlu,30dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
	    builder.setRowGroupingEnabled(true);

		builder.append("Usuario",getControl("userName"),bussyLabel);
		
		builder.append("Password",getControl("password"));
		if(isConComentario()){
			CellConstraints cc=new CellConstraints();
			 
		     builder.appendRow(builder.getLineGapSpec());
		     builder.appendRow(new RowSpec("top:31dlu")); // Assumes line is 14, gap is 3
		     builder.nextLine(2);
		     builder.append("Comment");
		     builder.add(new JScrollPane(getControl("comentario")),cc.xy(builder.getColumn(), builder.getRow(), "fill, fill"));
		     
		     builder.nextLine();
		}
		
	     
		return builder.getPanel();
	}

	public void clear(){
		getUser().setComentario("");
		getUser().setPassword(null);
		getUser().setUserName(null);
	}
	
	
	private int intentos=3;
	private int intento=1;
	
	public void loggedIn(){
		super.doAccept();
	}
	@Override
	public void doAccept() {
		if(intento<=intentos){
			LoginWorker worker=new LoginWorker();
			worker.execute();
		}
		else
			doCancel();

	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if("comentario".equals(property)){
			JTextArea ta=BasicComponentFactory.createTextArea(model.getModel(property),false);
			return ta;
		}else if("password".equals(property)){
			JPasswordField pw=BasicComponentFactory.createPasswordField(model.getModel(property));
			return pw;
		}
		return null;
	}

	public UserModel getUser(){
		return (UserModel)model.getBaseBean();
	}
	
	public String getUserName(){
		return getUser().getUserName();
	}
	public String getPassword(){
		return getUser().getPassword();
	}
	
	

	public AutorizacionesManager getManager() {
		return manager;
	}

	public void setManager(AutorizacionesManager manager) {
		this.manager = manager;
	}



	public static class UserModel extends Model{
		
		@NotNull(message="Se requiere un usuario autorizado")
		private String userName;
		
		@NotNull(message="Digite su password")
		private String password;
		
		@NotNull(message="Digite un comentario ")
		@Length(max=250)
		private String comentario="";
		
		private User user;

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			Object old=this.userName;
			this.userName = userName;
			firePropertyChange("userName", old, userName);
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			Object old=this.password;
			this.password = password;
			firePropertyChange("password", old, password);
		}

		public String getComentario() {
			return comentario;
		}

		public void setComentario(String comentario) {
			Object old=this.comentario;
			this.comentario = comentario;
			firePropertyChange("comentario", old, comentario);
		}

		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}
		
		
		
	}
	
	public void setUserBean(User u){
		getUser().setUser(u);
	}
	
	public class LoginWorker extends SwingWorker<User, String>{

		@Override
		protected User doInBackground() throws Exception {
			bussyLabel.setBusy(true);
			getCancelAction().setEnabled(false);
			getOKAction().setEnabled(false);
			if(getManager()!=null){
				return getManager().authenticate(getUserName(), getPassword());
			}
			User res=ServiceLocator2.getAutorizacionesManager().authenticate(getUserName(), getPassword());
			return res;
		}
		
		@Override
		protected void done() {
			try {
				User res=get();
				getUser().setUser(res);
				loggedIn();
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getContentPane(), "Credenciales invalidas","Autorizaciones",JOptionPane.WARNING_MESSAGE);
				intento++;
			}finally{
				bussyLabel.setBusy(false);
				getCancelAction().setEnabled(true);
				getOKAction().setEnabled(true);
			}
			
		}
	}
	
	

	public static void main(String[] args) {
		SWExtUIManager.setup();
		AutorizacionWindow window=new AutorizacionWindow("Autorizaciones CxC");
		window.open();
		if(!window.hasBeenCanceled()){
			System.out.println("Localizando usuario: "+window.getUser().getUser());
		}
	}

}
