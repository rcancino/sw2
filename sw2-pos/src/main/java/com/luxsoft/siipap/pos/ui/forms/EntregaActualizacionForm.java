package com.luxsoft.siipap.pos.ui.forms;

import java.text.SimpleDateFormat;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.text.DateFormatter;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.embarque.Entrega;

/**
 * Forma para actualizar los siguientes datos de una entrega
 * 	
 * arribo
 * recepcion
 * recibio 
 * comentario
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class EntregaActualizacionForm extends AbstractForm{

	public EntregaActualizacionForm(IFormModel model) {
		super(model);
		setTitle("Actualización de entrega");
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,2dlu,70dlu,3dlu" +
				",p,2dlu,70dlu, 3dlu" +
				",p,2dlu,100dlu:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		//builder.append("Llegada con cliente",getControl("arribo"));
		builder.append("Recepción del cliente",getControl("recepcion"),true);
		builder.append("Recibió",getControl("recibio"),7);
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),9);
		
		return builder.getPanel();
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("recepcion".equals(property) || "arribo".equals(property)){
			JSpinner s=com.luxsoft.siipap.swing.binding.Bindings
					.createDateSpinnerBinding(model.getModel(property));
			
			s.setEnabled(!model.isReadOnly());
			return s;
		}else if("comentario".equals(property)|| "recibio".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
		}
		return super.createCustomComponent(property);
	}

	public static Entrega editar(final Entrega target){
		
		DefaultFormModel model=new DefaultFormModel(target);
		EntregaActualizacionForm form=new EntregaActualizacionForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return target;
		}
		return null;
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
				showObject(editar(new Entrega()));
				
				
				System.exit(0);
			}

		});
	}

}
