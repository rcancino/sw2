package com.luxsoft.siipap.cxc.ui.clientes;

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
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.FormatUtils;
import com.luxsoft.sw3.cfdi.model.CFDIClienteMails;


public class CFDIMailsClienteForm extends AbstractForm{
	
	public CFDIMailsClienteForm(IFormModel model) {
		super(model);
		setTitle("Mantenimiento Correo CFDI");
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
		ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("cliente".equals(property)){
			JComponent c=BasicComponentFactory.createFormattedTextField(model.getModel(property), FormatUtils.getToStringFormat());
			c.setEnabled(false);
			return c;
		}
		return null;
	}
	
	@Override
	protected void afterContentCreated(JComponent content) {
		Header header=new Header("Mantenimiento correo envío de CFDI"
				," Sugerir al cliente tener un CORREO ESPECIAL para la recepción " +
						"\nde comprobantes fiscales digitales");
		header.setDescRows(5);
		content.add(header.getHeader(),BorderLayout.NORTH);
		
		super.afterContentCreated(content);
	} 
	
	public static CFDIClienteMails showForm(String clave){
		Cliente cliente=ServiceLocator2.getClienteManager().buscarPorClave(clave);
		
		List<CFDIClienteMails> data=ServiceLocator2.getHibernateTemplate()
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
		final CFDIMailsClienteForm form=new CFDIMailsClienteForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			CFDIClienteMails target=salvar(model.getTarget());
			
			return target;
			
		}
		return null;
	}
	
	public static CFDIClienteMails salvar(CFDIClienteMails bean){
		Date time=ServiceLocator2.obtenerFechaDelSistema();
		
		if(bean.getId()==null){
			bean.getLog().setCreado(time);
		
		}
		bean.getLog().setModificado(time);
		CFDIClienteMails res= (CFDIClienteMails)ServiceLocator2.getHibernateTemplate().merge(bean);
		res.setUsuario(bean.getUsuario());
		return res;
		
	}
	
	public static class CFDIClienteMailsModel extends DefaultFormModel{
		
		public CFDIClienteMailsModel(CFDIClienteMails mails) {
			super(mails);
		
			
		}
		
		
	
		
		public CFDIClienteMails getTarget(){
			return (CFDIClienteMails)getBaseBean();
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
