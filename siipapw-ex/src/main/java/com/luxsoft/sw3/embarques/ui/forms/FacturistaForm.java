package com.luxsoft.sw3.embarques.ui.forms;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.sw3.embarque.ChoferFacturista;



/**
 * Fomra para el mantenimiento de facturistas para embarques
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class FacturistaForm extends AbstractForm{
	
	

	public FacturistaForm(final IFormModel model) {
		super(model);
		setTitle("Facturista para embarques      ");
		
	}
	
	

	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("Facturista","");
			
		}
		return header.getHeader();
	}
	
	

	@Override
	protected JComponent buildFormPanel() {
		
		
		FormLayout layout=new FormLayout(
				"p,2dlu,p:g(.5),3dlu," +				
				"p,2dlu,p:g(.5)"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		if(model.getValue("id")!=null)
			builder.append("Id",addReadOnly("id"),true);
		builder.append("Nombre",addMandatory("nombre"),5);
		builder.nextLine();	
		builder.append("RFC",getControl("rfc"));
		builder.nextLine();	
		builder.append("Teléfono 1",getControl("telefono1"));
		builder.append("Teléfono 2",getControl("telefono2"));
		builder.nextLine();	
		builder.append("Fax",getControl("fax"));
		builder.append("email",getControl("email1"));		
		builder.nextLine();		
		builder.append("Dirección",getControl("direccion"),5);
		
		ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
		
		return builder.getPanel();
	}
	
	
	
	
	
	@Override
	protected void onWindowOpened() {
		super.onWindowOpened();
		
	}

	protected JComponent createNewComponent(final String property){
		JComponent c=super.createNewComponent(property);
		c.setEnabled(!model.isReadOnly());
		return c;
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("nombre".equals(property)){
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),false);
			return tf;
		}else if("rfc".equals(property)){
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),false);
			return tf;
		}
		return null;
	}
	
	
	
	
	
	
	
	
	

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				DefaultFormModel controller=new DefaultFormModel(new ChoferFacturista());
				FacturistaForm form=new FacturistaForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println(ToStringBuilder.reflectionToString(controller.getBaseBean()));
					
				}
				System.exit(0);
			}

		});
	}

	
	

}
