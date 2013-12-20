package com.luxsoft.cfdi;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.FormatUtils;
import com.luxsoft.sw3.cfdi.model.CFDIClienteMails;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.services.KernellUtils;

public class CFDIMandarFacturarForm extends AbstractForm{
	
	public CFDIMandarFacturarForm(IFormModel model) {
		super(model);
		setTitle("Mandar facturar");
	}
	
	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,max(p;200dlu)" 
			,	"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Cliente ",getControl("cliente"),true);
		builder.append("Correo para CFDI (1)",getControl("email1"),true);
		builder.append("Correo para CFDI (2)",getControl("email2"));
		builder.append("Contraseña",getControl("password"));
		builder.append("Usuario",addReadOnly("usuario"));
		ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("cliente".equals(property) || "usuario".equals(property) ){
			JComponent c=BasicComponentFactory.createFormattedTextField(model.getModel(property), FormatUtils.getToStringFormat());
			c.setEnabled(false);
			return c;
		}else if("password".equals(property)){
			JComponent c=BasicComponentFactory.createPasswordField(model.getModel(property),true);
			c.setEnabled(!model.isReadOnly());
			return c;
		}
		return null;
	}
	
	@Override
	protected void afterContentCreated(JComponent content) {
		Header header=new Header("Verificar Email del cliente par recepción de CFDIs"
				," Sugerir al cliente tener un CORREO ESPECIAL para la recepción " +
						"\nde comporbantes fiscales digitales");
		header.setDescRows(5);
		content.add(header.getHeader(),BorderLayout.NORTH);
		
		super.afterContentCreated(content);
	} 
	
	public static CFDIClienteMails showForm(String clave){
		Cliente cliente=Services.getInstance().getClientesManager().buscarPorClave(clave);
		
		List<CFDIClienteMails> data=Services.getInstance().getHibernateTemplate()
				.find("from CFDIClienteMails c where c.cliente.clave=?", clave);
		
		if(!data.isEmpty()){			
			return showForm(data.get(0));
		}else{
			CFDIClienteMails mails=new CFDIClienteMails(cliente);
			return showForm(mails);
		}
	}
	
	public static CFDIClienteMails showForm(CFDIClienteMails mails){
		final CFDIClienteMailsModel  model=new CFDIClienteMailsModel (mails);
		final CFDIMandarFacturarForm form=new CFDIMandarFacturarForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			CFDIClienteMails target=salvar(model.getTarget());
			
			return target;
			
		}
		return null;
	}
	
	public static CFDIClienteMails salvar(CFDIClienteMails bean){
		Date time=Services.getInstance().obtenerFechaDelSistema();
		User user=bean.getUsuario();
		if(bean.getId()==null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user.getFullName());
		}
		bean.getLog().setModificado(time);
		bean.getLog().setUpdateUser(user.getFullName());
		CFDIClienteMails res= (CFDIClienteMails)Services.getInstance().getHibernateTemplate().merge(bean);
		res.setUsuario(bean.getUsuario());
		return res;
		
	}
	
	public static class CFDIClienteMailsModel extends DefaultFormModel{
		
		public CFDIClienteMailsModel(CFDIClienteMails mails) {
			super(mails);
			getModel("password").addValueChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					resolveUser();
				}
			});
			
		}
		
		
		@Override
		protected void addValidation(PropertyValidationSupport support) {
			super.addValidation(support);
			if(getValue("password")==null){
				support.addError("password", "Digite su clave de usuario" );
			}
		}
		
		public CFDIClienteMails getTarget(){
			return (CFDIClienteMails)getBaseBean();
		}
		
		private void resolveUser() {
			User user=KernellSecurity.instance().findUser(
					getTarget().getPassword()
					, Services.getInstance().getHibernateTemplate()
					);
			getTarget().setUsuario(user);
			validate();
		}
		
	}
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				showForm("U050008");
				
			}
		});
	}

}
