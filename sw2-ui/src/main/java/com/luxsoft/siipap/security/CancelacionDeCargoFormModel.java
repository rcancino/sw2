package com.luxsoft.siipap.security;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;



public class CancelacionDeCargoFormModel extends DefaultFormModel{
	
	private HibernateTemplate hibernateTemplate;
	
	public CancelacionDeCargoFormModel() {
		super(Bean.proxy(CancelacionModel.class));
		getModel("password").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				resolveUser();
			}
		});
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		support.getResult().addWarning("Recuerde que esta operación es delicada y auditable," +
				"\n confirme los datos antes de proceder");
	}



	public CancelacionModel getCancelacion(){
		return (CancelacionModel)getBaseBean();
	}
	
	private void resolveUser() {
		User user=KernellSecurity.instance().findUser(
				getCancelacion().getPassword()
				, getHibernateTemplate()
				);
		getCancelacion().setUsuario(user);
		validate();
	}
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}



	public static class CancelacionModel{
		
		@NotEmpty
		private String password;
		@NotNull
		private User usuario;
		@NotEmpty
		private String comentario;
		private String nombre;
		
		
		
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public User getUsuario() {
			return usuario;
		}
		public void setUsuario(User usuario) {
			this.usuario = usuario;
			setNombre(usuario!=null?usuario.getFullName():null);
		}
		public String getComentario() {
			return comentario;
		}
		public void setComentario(String comentario) {
			this.comentario = comentario;
		}
		public String getNombre() {
			return nombre;
		}
		public void setNombre(String nombre) {
			this.nombre = nombre;
		}
		
		
		
	}

}
