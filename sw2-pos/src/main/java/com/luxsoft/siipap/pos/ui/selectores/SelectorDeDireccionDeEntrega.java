package com.luxsoft.siipap.pos.ui.selectores;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;

/**
 * Permite definir una instruccion de entrega
 * 
 * Si el cliente tiene asociadas instrucciones de entrega
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SelectorDeDireccionDeEntrega extends AbstractForm{
	
	private SelectorDeDireccionDeEntrega(IFormModel model){
		super(model);
		setTitle("Instrucción de Entrega");
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
		builder.append("Entidad",getControl("estado"));
		builder.append("C.P.",getControl("cp"));
		builder.append("País",getControl("pais"));
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
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				
				System.exit(0);
			}

		});
	}

}
