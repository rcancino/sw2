package com.luxsoft.sw3.embarque;

import java.text.MessageFormat;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Cliente designado para una tarifa especial por tonelada
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_EMBARQUES_CLIENTES_TON")
public class ClientePorTonelada extends BaseBean implements Replicable{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ID")
	private Long id;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)			
	@JoinColumn(name = "CLIENTE_ID", nullable = false, updatable = false)
	@NotNull(message="El cliente es mandatorio")	
	private Cliente cliente;
	
	@Column(name = "CLAVE", length = 7)
	@NotNull
	private String clave;
	
	@Column(name="PRECIO")
	private double precio=0;
	
	@Column(name="COMENTARIO")
	@Length (max=255)
	private String comentario;
	
	@Column(name="SUSPENDER_FLETE")
	private boolean suspenderFlete;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
	public ClientePorTonelada() {}
	

	public ClientePorTonelada(Cliente cliente) {
		super();
		this.cliente = cliente;		
		setClave(cliente.getClave());
		
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public double getPrecio() {
		return precio;
	}

	public void setPrecio(double precio) {
		double old =this.precio;
		this.precio = precio;
		firePropertyChange("precio", old, precio);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}


	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public Long getId() {
		return id;
	}
	
	

	public Date getImportado() {
		return importado;
	}


	public void setImportado(Date importado) {
		this.importado = importado;
	}


	public Date getReplicado() {
		return replicado;
	}


	public void setReplicado(Date replicado) {
		this.replicado = replicado;
	}
	

	public boolean isSuspenderFlete() {
		return suspenderFlete;
	}

	public void setSuspenderFlete(boolean suspenderFlete) {
		boolean old=this.suspenderFlete;
		this.suspenderFlete = suspenderFlete;
		firePropertyChange("suspenderFlete", old, suspenderFlete);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cliente == null) ? 0 : cliente.hashCode());
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
		ClientePorTonelada other = (ClientePorTonelada) obj;
		if (cliente == null) {
			if (other.cliente != null)
				return false;
		} else if (!cliente.equals(other.cliente))
			return false;
		return true;
	}
	
	public String toString(){
		return MessageFormat.format("{0} Precio: {1}", this.clave,this.precio);
	}
	
	@AssertTrue(message="Precio por tonelada invalido")
	public boolean validarPrecio(){
		return getPrecio()>=0;
	}

}
