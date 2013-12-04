package com.luxsoft.sw3.crm.catalogos;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;

public class CRM_DireccionForm extends AbstractForm {
	
	

	public CRM_DireccionForm(IFormModel model) {
		super(model);
	}
	
	
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"l:p,2dlu,f:90dlu ,2dlu," +
				"l:p,2dlu,f:90dlu"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendSeparator("Dirección");
		builder.append("Calle",addMandatory("calle"),5);
		builder.append("Numero Ext",addMandatory("numero"));
		builder.append("Numero Int",getControl("interior"));
		builder.append("Colonia",getControl("colonia"),5);
		builder.append("Del/Mpio",getControl("municipio"),5);
		builder.append("Entidad",getControl("estado"));
		builder.append("C.P.",getControl("cp"));
		builder.append("Comentario  ",getControl("comentario"),5);
		return builder.getPanel();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JComponent createCustomComponent(String property) {
		if("estado".equalsIgnoreCase(property)){
			return Bindings.createEstadosBinding(model.getComponentModel(property));
		}else if("ciudad".equalsIgnoreCase(property)){
			return Bindings.createCiudadesBinding(model.getComponentModel(property));
		}else if("municipio".equalsIgnoreCase(property)){
			return Bindings.createMunicipiosBinding(model.getComponentModel(property));
		}else if("calle".equals(property) || "colonia".equals(property) ){
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),true);
			return tf;
		}
		return super.createCustomComponent(property);
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
				
			}

		});
	}

	
}
