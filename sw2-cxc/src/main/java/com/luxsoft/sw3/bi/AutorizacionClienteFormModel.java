package com.luxsoft.sw3.bi;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.ClienteAuditLog;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;

public class AutorizacionClienteFormModel extends DefaultFormModel{
	
	private final String property;
	
	public AutorizacionClienteFormModel(String property) {
		super(Bean.proxy(ModificacionModel.class));
		this.property=property;
	}
	
	public ModificacionModel getModificacionModel(){
		return (ModificacionModel)getBaseBean();
	}
	public Cliente getCliente(){
		return getModificacionModel().getCliente();
	}
	
	public String getProperty() {
		return property;
	}

	public Cliente commit(){
		final Cliente old=ServiceLocator2.getClienteManager().buscarPorClave(getCliente().getClave());
		final BeanWrapperImpl w1=new BeanWrapperImpl(old);
		Object newVal=getValue(getProperty());
		final String ip=KernellSecurity.getIPAdress();
		final String message=MessageFormat.format("ANTERIOR:{0}, NUEVO:{1} COMENTARIO:{2} "
				,w1.getPropertyValue(property)
				,newVal
				,getModificacionModel().getComentario()
				);
		w1.setPropertyValue(getProperty(), newVal);
		final String user=KernellSecurity.instance().getCurrentUserName();
		return (Cliente)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				session.update(old);
				ClienteAuditLog log=new ClienteAuditLog(old, old.getId(), getProperty().toUpperCase(), ip, "OFICINAS");
				if(getModificacionModel().isSuspendido())
					old.agregarComentario("AVISO", getModificacionModel().getComentario());
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
	
	
	public static class ModificacionModel{
		private Cliente cliente;
		private String comentario;
		private boolean permitirCheque;
		private boolean suspendido;
		private BigDecimal chequesDevueltos;
		public Cliente getCliente() {
			return cliente;
		}
		public void setCliente(Cliente cliente) {
			this.cliente = cliente;
		}
		public String getComentario() {
			return comentario;
		}
		public void setComentario(String comentario) {
			this.comentario = comentario;
		}
		public boolean isPermitirCheque() {
			return permitirCheque;
		}
		public void setPermitirCheque(boolean permitirCheque) {
			this.permitirCheque = permitirCheque;
		}
		public boolean isSuspendido() {
			return suspendido;
		}
		public void setSuspendido(boolean suspendido) {
			this.suspendido = suspendido;
		}
		public BigDecimal getChequesDevueltos() {
			return chequesDevueltos;
		}
		public void setChequesDevueltos(BigDecimal chequesDevueltos) {
			this.chequesDevueltos = chequesDevueltos;
		}
		
		
	}

}
