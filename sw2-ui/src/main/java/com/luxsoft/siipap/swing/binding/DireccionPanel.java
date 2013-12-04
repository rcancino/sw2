package com.luxsoft.siipap.swing.binding;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;

/**
 * Panel para el mantenimiento de una Direccion
 * 
 * @author Ruben Cancino
 *
 */
public class DireccionPanel extends AbstractForm{
	

	public DireccionPanel(IFormModel model) {
		super(model);
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"l:p,2dlu,f:70dlu ,2dlu," +
				"l:p,2dlu,f:70dlu"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		//builder.setDefaultDialogBorder();
		builder.append("Calle",addMandatory("calle"),5);
		builder.append("Numero Ext",addMandatory("numero"));
		builder.append("Numero Int",getControl("numeroInterior"));
		builder.append("Colonia",getControl("colonia"),5);
		builder.append("Del/Mpio",getControl("municipio"),5);
		builder.append("Ciudad",getControl("ciudad"));
		builder.append("Estado",getControl("estado"));
		builder.append("C.P.",getControl("cp"));
		//builder.append("País",getControl("pais"));
		return builder.getPanel();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JComponent createCustomComponent(String property) {
		if("estado".equalsIgnoreCase(property)){
			//return Bindings.createEstadosBinding(buffer(model.getComponentModel(property)));
			return Bindings.createEstadosBinding(model.getComponentModel(property));
		}else if("ciudad".equalsIgnoreCase(property)){
			//return Bindings.createCiudadesBinding(buffer(model.getComponentModel(property)));
			return Bindings.createCiudadesBinding(model.getComponentModel(property));
		}else if("municipio".equalsIgnoreCase(property)){
			//return Bindings.createMunicipiosBinding(buffer(model.getComponentModel(property)));
			return Bindings.createMunicipiosBinding(model.getComponentModel(property));
		}else if("calle".equals(property) || "colonia".equals(property) ){
			//JTextField tf=Binder.createMayusculasTextField(buffer(model.getComponentModel(property)),true);
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),false);
			return tf;
		}else if(property.startsWith("num")){
			//JTextField tf=Binder.createMayusculasTextField(buffer(model.getComponentModel(property)),true);
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),false);
			return tf;
		}else {
			//JTextField tf=Binder.createMayusculasTextField(buffer(model.getComponentModel(property)),true);
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),false);
			return tf;
		}		
	}

	public static Direccion showForm(){
		return showForm(new Direccion());
	}
	
	public static Direccion showForm(Direccion bean){
		return showForm(bean,false);
	}
	
	public static Direccion showForm(Direccion bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final DireccionPanel form=new DireccionPanel(model);
		//form.setUndecorated(true);
		form.setResizable(true);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Direccion)model.getBaseBean();
		}
		return null;
	}
	
	public static void main(String[] args) {
		showForm();
	}

	
}
