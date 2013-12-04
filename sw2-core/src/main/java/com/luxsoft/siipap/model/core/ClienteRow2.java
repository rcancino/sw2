package com.luxsoft.siipap.model.core;

import java.util.Date;

public class ClienteRow2 {
	
	private Long cliente_id;
	private String clave;
	private String nombre;
	private Date creado;
	private Date modificado;
	private Date importado;
	private Long credito;
	private Date replicado;
	
	 
	
	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}
	public Long getCliente_id() {
		return cliente_id;
	}
	public void setCliente_id(Long cliente_id) {
		this.cliente_id = cliente_id;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
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
	
	
	public Date getImportado() {
		return importado;
	}
	public void setImportado(Date importado) {
		this.importado = importado;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cliente_id == null) ? 0 : cliente_id.hashCode());
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
		ClienteRow2 other = (ClienteRow2) obj;
		if (cliente_id == null) {
			if (other.cliente_id != null)
				return false;
		} else if (!cliente_id.equals(other.cliente_id))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "ClienteRow2 [clave=" + clave + ", nombre=" + nombre + "]";
	}
	public Long getCredito() {
		return credito;
	}
	public void setCredito(Long credito) {
		this.credito = credito;
	}
	public Date getReplicado() {
		return replicado;
	}
	public void setReplicado(Date replicado) {
		this.replicado = replicado;
	}

	
	

}
