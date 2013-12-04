package com.luxsoft.siipap.security;

import javax.swing.JComponent;

import org.springframework.orm.hibernate3.HibernateTemplate;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;

/**
 * Dialogo seleccionar un usuario usando unicamente el password
 * 
 * @author Ruben Cancino
 *
 */
public class SeleccionDeUsuario extends AbstractForm{
	
	

	public SeleccionDeUsuario(DefaultFormModel model) {
		super(model);
		
	}
	
	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("Registro de usuario"
					,"Digite su password");
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
		return builder.getPanel();
	}
	
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("password".equals(property)){
			JComponent c=BasicComponentFactory.createPasswordField(model.getModel(property),true);
			c.setEnabled(!model.isReadOnly());
			return c;
		}
		return null;
	}
	
	public static User findUser(HibernateTemplate template){
		SeleccionDeUsuarioFormModel model=new SeleccionDeUsuarioFormModel();
		model.setHibernateTemplate(template);
		SeleccionDeUsuario form=new SeleccionDeUsuario(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getUsuarioModel().getUsuario();
		}
		return null;
	}
	
	public static User findUser(HibernateTemplate template,String titulo){
		SeleccionDeUsuarioFormModel model=new SeleccionDeUsuarioFormModel();
		model.setHibernateTemplate(template);
		SeleccionDeUsuario form=new SeleccionDeUsuario(model);
		form.setTitle(titulo);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getUsuarioModel().getUsuario();
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
				System.out.println(findUser(com.luxsoft.siipap.service.ServiceLocator2.getHibernateTemplate()));	
				System.exit(0);
			}

		});
	}

	

}
