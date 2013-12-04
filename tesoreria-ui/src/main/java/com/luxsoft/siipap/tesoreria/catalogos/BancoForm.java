package com.luxsoft.siipap.tesoreria.catalogos;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.validation.view.ValidationComponentUtils;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;

/**
 * Forma para el mantenimiento de Bancos
 * 
 * @author Ruben Cancino
 *
 */
public class BancoForm extends GenericAbstractForm<Banco>{

	public BancoForm(IFormModel model) {
		super(model);
		setTitle("Bancos");
	}

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Catálogo de Bancos","Mantenimiento al catálogo de instituciones bancarias");
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,70dlu, 2dlu," +
				"50dlu,2dlu,70dlu:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Clave",getControl("clave"),true);
		builder.append("Nombre",getControl("nombre"),5);
		builder.append("Empresa",getControl("empresa"),5);
		builder.append("RFC",getControl("rfc"),true);
		builder.append("Contacto 1",getControl("contacto1"),5);		
		builder.append("E-mail",getControl("email1"),5);		
		builder.append("Contacto 2",getControl("contacto2"),5);		
		builder.append("E-mail",getControl("email2"),5);		
		builder.append("Nacional",getControl("nacional"),true);
		ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
		return builder.getPanel();
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("empresa".equals(property)){
			return Bindings.createEmpresaBinding(model.getModel(property));
		}else if("clave".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getComponentModel(property));
			ValidationComponentUtils.setMandatory(control,true);
			return control;
		}			
		return null;
	}
	
	
	public static Banco showForm(Banco bean){
		return showForm(bean,false);
	}
	
	public static Banco showForm(Banco bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final BancoForm form=new BancoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Banco)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {		
		Object bean=showForm(new Banco());
		BancoForm.showObject(bean);
		System.exit(0);
	}

}
