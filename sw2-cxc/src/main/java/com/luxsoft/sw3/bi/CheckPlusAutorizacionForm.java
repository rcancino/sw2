package com.luxsoft.sw3.bi;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.ClienteAuditLog;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.sw3.ventas.CheckPlusCliente;


/**
 * Forma de autorizacion  clientes tipo CheckPlus
 * 
 * @author Ruben Cancino
 *
 */
public class CheckPlusAutorizacionForm extends AbstractForm{
	
	
	
	public CheckPlusAutorizacionForm(CheckPlusAutorizacionModel model) {
		super(model);
	}
	
	public CheckPlusAutorizacionModel getBaseModel(){
		return (CheckPlusAutorizacionModel)getModel();
	}
	
	
	
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,max(p;250dlu):g"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Crédito solicitado",addReadOnly("creditoSolicitado"),true);
		builder.append("Crédito autorizado",addMandatory("lineaDeCredito"),true);
		return builder.getPanel();
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("lineaDeCredito".equals(property)){
			JComponent c=Binder.createBigDecimalForMonyBinding(getModel().getModel(property));
			c.setEnabled(!getModel().isReadOnly());
			return c;
		}else if("creditoSolicitado".equals(property)){
			JComponent c=Binder.createBigDecimalForMonyBinding(getModel().getModel(property));
			c.setEnabled(false);
			return c;
		}
		return null;
	}
	
	
	public static CheckPlusCliente autorizar(String id){
		CheckPlusCliente source=ServiceLocator2.getCheckplusManager().buscarCliente(id);
		final CheckPlusCliente target=(CheckPlusCliente)Bean.proxy(CheckPlusCliente.class);
		BeanUtils.copyProperties(source, target);
		final CheckPlusAutorizacionModel model=new CheckPlusAutorizacionModel(target);
		final CheckPlusAutorizacionForm form=new CheckPlusAutorizacionForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}
		return null;
	}
	
	public static class CheckPlusAutorizacionModel extends DefaultFormModel{
		public CheckPlusAutorizacionModel(CheckPlusCliente c) {
			super(c);
		}
		
		public CheckPlusCliente getCliente(){
			return (CheckPlusCliente)getBaseBean();
		}
		
		public CheckPlusCliente commit(){
			final CheckPlusCliente old=ServiceLocator2.getCheckplusManager().buscarCliente(getCliente().getId());
			
			final BigDecimal credito=getCliente().getLineaDeCredito();
			final String ip=KernellSecurity.getIPAdress();
			final String message=MessageFormat.format("CREDITO ANTERIOR:{0}, NUEVO:{1} "
					,old.getLineaDeCredito().toString()
					,credito.toString()
					);
			final String user=KernellSecurity.instance().getCurrentUserName();
			
			return (CheckPlusCliente)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException,SQLException {
					session.update(old);
					old.getCliente().getCredito().setLinea(CantidadMonetaria.pesos(credito));
					old.setLineaDeCredito(credito);
					ClienteAuditLog log=new ClienteAuditLog(old, old.getId(), "LINEA DE CREDITO", ip, "OFICINAS");
					log.setTableName("SX_CHECKPLUS_CLIENTE");
					log.setEntityName("CheckPlusCliente");
					log.setMessage(message);
					log.setOrigen(old.getNombre());
					log.setUsuario(user);
					log.setLastUpdated(new Date());
					old.getLog().setUpdateUser(user);
					old.getAddresLog().setUpdatedIp(ip);
					session.save(log);
					//session.flush();
					return old;
				}
			});
		}
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				//Object res=showForm();
				Object res=autorizar("8a8a8161-3f348f3c-013f-3490104e-0002");
				if(res!=null)
					showObject(res);
			}
		});
	}

}
