package com.luxsoft.siipap.kernell;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;

public class RoleForm extends AbstractForm{

	public RoleForm(IFormModel model) {
		super(model);
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				"p,2dlu,f:max(p;90dlu):g"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.getValue("id")!=null	){
			builder.append("Id",getControl("id"));
		}
		builder.append("Nombre",getControl("name"));
		builder.append("Descripción",getControl("description"));
		return builder.getPanel();
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("name".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
		}
		return super.createCustomComponent(property);
	}

	public static Role showForm(){
		return showForm(new Role());
	}
	
	public static Role showForm(final Role role){
		return showForm(role,false);
	}
	
	public static Role showForm(final Role role,boolean readOnly){
		final DefaultFormModel model=new DefaultFormModel(role);
		model.setReadOnly(readOnly);
		final RoleForm form=new RoleForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Role)model.getBaseBean();
		}
		return null;
	}
	
	public static void main(String[] args) {
		Role r=showForm();
		if(r!=null){
			showObject(r);
		}
	}

}
