package com.luxsoft.siipap.support.hibernate;

import java.util.Date;

public class ModificationLog {
	
	private Date creado;
	private Date modificado;
	
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
	
	

}
