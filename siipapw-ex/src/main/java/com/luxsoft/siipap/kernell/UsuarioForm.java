package com.luxsoft.siipap.kernell;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.component.UIFButton;

public class UsuarioForm extends JPanel{
	
	private final PresentationModel model;
	
	private JComponent idField;
	
	public UsuarioForm(ValueHolder beanChannel){
		model=new PresentationModel(beanChannel);
		model.addPropertyChangeListener(PresentationModel.PROPERTYNAME_BUFFERING,new BuffeingListener());
		init();
		
	}
	
	public PresentationModel getModel(){
		return model;
	}
	
	JTextField username;                    // required
    JPasswordField password;                    // required
    JPasswordField confirmPassword;
    JTextField  passwordHint;
    JTextField  firstName;                   // required
    JTextField  lastName;                    // required
    JTextField  email;                       // required; unique
    JTextField  phoneNumber;    
    JCheckBox enabled;
    JCheckBox accountExpired;
    JCheckBox accountLocked;
    JCheckBox credentialsExpired;
	
	
	private void initComponents(){
		idField=BasicComponentFactory.createLongField(model.getBufferedComponentModel("id"));
		username=BasicComponentFactory.createTextField(model.getBufferedComponentModel("username"));
	    password=BasicComponentFactory.createPasswordField(model.getBufferedComponentModel("password"));
	    confirmPassword=BasicComponentFactory.createPasswordField(model.getBufferedComponentModel("confirmPassword"));
	    passwordHint=BasicComponentFactory.createTextField(model.getBufferedComponentModel("passwordHint"));
	    firstName=BasicComponentFactory.createTextField(model.getBufferedComponentModel("firstName"));
	    lastName=BasicComponentFactory.createTextField(model.getBufferedComponentModel("lastName"));
	    email=BasicComponentFactory.createTextField(model.getBufferedComponentModel("email"));
	    phoneNumber=BasicComponentFactory.createTextField(model.getBufferedComponentModel("phoneNumber"));
	    
	    
	    enabled=BasicComponentFactory.createCheckBox(model.getBufferedComponentModel("enabled"), "");
	    accountExpired=BasicComponentFactory.createCheckBox(model.getBufferedComponentModel("accountExpired"), "");
	    accountLocked=BasicComponentFactory.createCheckBox(model.getBufferedComponentModel("accountLocked"), "");
	    credentialsExpired=BasicComponentFactory.createCheckBox(model.getBufferedComponentModel("credentialsExpired"), "");
	}
	
	private void init(){
		initComponents();
		setLayout(new BorderLayout());
		add(buildForm(),BorderLayout.CENTER);
		add(ButtonBarFactory.buildRightAlignedBar(createAceptarButton(), createCancelButton()),BorderLayout.SOUTH);
	}
	
	public void eneableForm(boolean b){
		model.getBufferedComponentModel("id").setEditable(b);
		model.getBufferedComponentModel("username").setEditable(b);
	    model.getBufferedComponentModel("password").setEditable(b);
	    model.getBufferedComponentModel("confirmPassword").setEditable(b);
	    model.getBufferedComponentModel("passwordHint").setEditable(b);
	    model.getBufferedComponentModel("firstName").setEditable(b);
	    model.getBufferedComponentModel("lastName").setEditable(b);
	    model.getBufferedComponentModel("email").setEditable(b);
	    model.getBufferedComponentModel("phoneNumber").setEditable(b);
	    model.getBufferedComponentModel("enabled").setEditable(b);
	    model.getBufferedComponentModel("accountExpired").setEditable(b);
	    model.getBufferedComponentModel("accountLocked").setEditable(b);
	    model.getBufferedComponentModel("credentialsExpired").setEditable(b);
	}
	
	private JPanel buildForm(){
		final FormLayout layout=new FormLayout(
				"p,2dlu,f:p:g(.5), 2dlu ,p,2dlu,f:p:g(.5)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Datos generales");
		builder.append("LoginName",username);
		builder.append("id",idField);		
		
		builder.append("Password",password);
		builder.append("Confirmar",confirmPassword);
		
		builder.append("Nombre",firstName);
		builder.append("Apellido P",lastName);
		
		builder.append("Email",email);
		builder.append("Teléfono",phoneNumber);
		
		builder.appendSeparator("Cuenta");
		builder.append("Habilitado",enabled);
		builder.append("Cuenta expirada",accountExpired);
		builder.append("Bloqueado",accountLocked);
		builder.append("Cred. Expiraron",credentialsExpired);
		
		builder.getPanel().setOpaque(false);
		return builder.getPanel();
	}
	
	
	UIFButton button = new UIFButton("Cancelar");
	
	protected UIFButton createCancelButton() {
        
        button.addActionListener(EventHandler.create(ActionListener.class, this,"cancel"));
        button.setEnabled(false);
        button.setVerifyInputWhenFocusTarget(false);
        return button;
    }
	
	public void cancel(){
		model.triggerFlush();
	}
	
	
	
	private Action aceptarAction;
	
	private Action getAceptarAction(){
		if(aceptarAction==null){
			aceptarAction=new AbstractAction("Aceptar"){
				public void actionPerformed(ActionEvent e) {
					model.triggerCommit();
				}
			};
			aceptarAction.setEnabled(false);
		}
		return aceptarAction;
	}
	
	protected UIFButton createAceptarButton() {
        UIFButton button = new UIFButton(getAceptarAction());
        return button;
    }
	
	private class BuffeingListener implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			aceptarAction.setEnabled(model.isBuffering());
			button.setEnabled(model.isBuffering());
		}		
	}
	

}
