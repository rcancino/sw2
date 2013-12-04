package com.luxsoft.siipap.kernell;

import javax.swing.JComponent;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;



/**
 * Forma para el mantenimiento de instancias de {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class NewUserForm extends GenericAbstractForm<User>{
	
	

	public NewUserForm(IFormModel model) {
		super(model);
		setTitle("Alta de usuarios");
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Usuario nuevo","Alta de un nuevo usuario");
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.form2.AbstractForm#buildFormPanel()
	 */
	@Override
	protected JComponent buildFormPanel() {
		return buildGeneralForm();
	}

	private JComponent buildGeneralForm(){
		final FormLayout layout=new FormLayout(
				"p,2dlu,f:max(p;120dlu):g"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.append("Usuario",getControl("username"));
		builder.append("Password",getControl("password"));
		builder.append("Nombre",getControl("firstName"));
		builder.append("Apellido",getControl("lastName"));
		return builder.getPanel();
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("password".equals(property)){
			return BasicComponentFactory.createPasswordField(model.getModel(property));
		}else if("firstName".equals(property) || "lastName".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
		}
		return null;
	}
	
	
	
	
	public static User showForm(){
		DefaultFormModel model=new DefaultFormModel(new User());
		final NewUserForm form=new NewUserForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (User)model.getBaseBean();
		}
		return null;
	}
	
		
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		User user=NewUserForm.showForm();
		if(user!=null){
			NewUserForm.showObject(user);
		}
		System.exit(0);
	}

}
