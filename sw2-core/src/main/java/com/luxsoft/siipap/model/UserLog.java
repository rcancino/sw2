package com.luxsoft.siipap.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

/**
 * Componente para el registro de bitacora
 * de modificaciones a una entidad hechas por los usuarios
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Embeddable
public class UserLog implements Serializable{
	
	
	@Column(name="CREADO_USERID")
	private String createUser;
	
    @Column(name="MODIFICADO_USERID")
    private String updateUser;
	
	@Type (type="timestamp")
	@Column(name="CREADO",updatable=false)
	@AccessType( value="field")
	private Date creado;
	
	@Type (type="timestamp")
	@Column(name="MODIFICADO")
	private Date modificado;
	
	
	public UserLog() {	}
	
	public UserLog(User createUser) {
		this(createUser,createUser);
	}

	public UserLog(User createUser, User updateUser) {		
		this.createUser = createUser.getUsername();
		this.updateUser = updateUser.getUsername();		
	}
	
	
	
	/**
	 * @param createUser the createUser to set
	 */
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	/**
	 * @param updateUser the updateUser to set
	 */
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getCreateUser() {
		return createUser;
	}
	public String getUpdateUser() {
		return updateUser;
	}
	
	public Date getCreado() {
		return creado;
	}
	public void setCreado(Date creado) {
		this.creado = creado;
	}

	public Date getModificado() {
		return modificado;
	}
	public void setModificado(Date modificado) {
		this.modificado = modificado;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((creado == null) ? 0 : creado.hashCode());
		result = PRIME * result + ((createUser == null) ? 0 : createUser.hashCode());
		result = PRIME * result + ((modificado == null) ? 0 : modificado.hashCode());
		result = PRIME * result + ((updateUser == null) ? 0 : updateUser.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final UserLog other = (UserLog) obj;
		if (creado == null) {
			if (other.creado != null)
				return false;
		} else if (!creado.equals(other.creado))
			return false;
		if (createUser == null) {
			if (other.createUser != null)
				return false;
		} else if (!createUser.equals(other.createUser))
			return false;
		if (modificado == null) {
			if (other.modificado != null)
				return false;
		} else if (!modificado.equals(other.modificado))
			return false;
		if (updateUser == null) {
			if (other.updateUser != null)
				return false;
		} else if (!updateUser.equals(other.updateUser))
			return false;
		return true;
	}
	
	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}	

}
