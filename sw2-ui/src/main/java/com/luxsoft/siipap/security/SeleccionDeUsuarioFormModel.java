package com.luxsoft.siipap.security;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;



public class SeleccionDeUsuarioFormModel extends DefaultFormModel{
	
	private HibernateTemplate hibernateTemplate;
	
	public SeleccionDeUsuarioFormModel() {
		super(Bean.proxy(UsuarioModel.class));
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



	public UsuarioModel getUsuarioModel(){
		return (UsuarioModel)getBaseBean();
	}
	
	private void resolveUser() {
		User user=KernellSecurity.instance().findUser(
				getUsuarioModel().getPassword()
				, getHibernateTemplate()
				);
		getUsuarioModel().setUsuario(user);
		validate();
	}
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}



	public static class UsuarioModel{
		
		@NotEmpty
		private String password;
		@NotNull
		private User usuario;
		
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
		
		public String getNombre() {
			return nombre;
		}
		public void setNombre(String nombre) {
			this.nombre = nombre;
		}
		
		
		
	}

}
