package com.luxsoft.sw3.cfdi.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Cliente;


@Entity
@Table(name="SX_CLIENTES_CFDI_MAILS")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class CFDIClienteMails extends BaseBean{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="CFD_ID")
	protected String id;
	
	@Version
	private int version;
	
	@ManyToOne(optional = false)			
	@JoinColumn(name = "CLIENTE_ID",nullable=false,unique=true)
	private Cliente cliente;
	
	@Column(name="EMAIL1")
	@Length (max=100)
	@NotNull
	private String email1;
	    
	@Column(name="EMAIL2")
	@Length (max=100)
	private String email2;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
	
	public CFDIClienteMails() {}
	
	

	public CFDIClienteMails(Cliente cliente) {
		super();
		this.cliente = cliente;
		setEmail1(cliente.getEmai3());
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getEmail1() {
		return email1;
	}

	public void setEmail1(String email1) {
		Object old=this.email1;
		this.email1 = email1;
		firePropertyChange("email1", old, email1);
	}

	public String getEmail2() {
		return email2;
	}

	public void setEmail2(String email2) {
		Object old=this.email2;
		this.email2 = email2;
		firePropertyChange("email2", old, email2);
	}

	public UserLog getLog() {
		if(log==null){
			log=new UserLog();
		}
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cliente == null) ? 0 : cliente.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		CFDIClienteMails other = (CFDIClienteMails) obj;
		if (cliente == null) {
			if (other.cliente != null)
				return false;
		} else if (!cliente.equals(other.cliente))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append("Cliente:  "+getCliente())
		.append("Email 1: "+getEmail1())
		.append("Email 2: "+getEmail2())
		.toString();
	}
	
	@Transient
	private User usuario;
	
	@Transient
	private String password;

	public User getUsuario() {
		return usuario;
	}

	public void setUsuario(User usuario) {
		Object old=this.usuario;
		this.usuario = usuario;
		firePropertyChange("usuario", old, usuario);
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		Object old=this.password;
		this.password = password;
		firePropertyChange("password", old, password);
	}
	
	

}
