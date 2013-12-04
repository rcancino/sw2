package com.luxsoft.siipap.security;

import java.awt.Dialog;

import javax.swing.JComponent;

import org.springframework.orm.hibernate3.HibernateTemplate;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.FormatUtils;

/**
 * Dialogo para cancelar un Cargo a un cliente
 * 
 * @author Ruben Cancino
 *
 */
public class CancelacionDeCargoForm extends AbstractForm{
	
	

	public CancelacionDeCargoForm(DefaultFormModel model) {
		super(model);
		
	}
	
	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("Cancelación de Venta/Cargo"
					,"Registre su password y la razón de la cancelación");
		}
		return header.getHeader();
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				"p,3dlu,70dlu, 3dlu, p,3dlu,p:g"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Password",getControl("password"));
		builder.append("Usuario",addReadOnly("nombre"));
		builder.append("Motivo",getControl("comentario"),5);
		return builder.getPanel();
	}
	
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("password".equals(property)){
			JComponent c=BasicComponentFactory.createPasswordField(model.getModel(property),true);
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("comentario".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
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
				CancelacionDeCargoFormModel model=new CancelacionDeCargoFormModel();
				model.setHibernateTemplate(ServiceLocator2.getHibernateTemplate());
				CancelacionDeCargoForm form=new CancelacionDeCargoForm(model);
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println(model.getCancelacion().getUsuario().getFullName());
					System.out.println(model.getCancelacion().getComentario());
					
				}
				System.exit(0);
			}

		});
	}

	

}
