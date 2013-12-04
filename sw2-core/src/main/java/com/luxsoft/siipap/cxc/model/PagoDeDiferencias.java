package com.luxsoft.siipap.cxc.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@DiscriminatorValue("PAGO_DIF")
public class PagoDeDiferencias extends Pago{
	
	@Column(name="DIF_CAMBIARIA",nullable=true)
	private Boolean cambiaria=false;

	@Override
	public String getInfo() {
		if(getCambiaria())
			return "DIF CAMBIARIA";
		return "AJUSTE SALDO";
	}

	public Boolean getCambiaria() {
		if(cambiaria==null)
			cambiaria=Boolean.FALSE;
		return cambiaria;
	}

	public void setCambiaria(Boolean cambiaria) {
		this.cambiaria = cambiaria;
	}
	
	public boolean equals(Object obj){
		if(obj==null) return false;
		if(this==obj) return true;
		if(getClass()!=obj.getClass()) return false;
		PagoDeDiferencias other=(PagoDeDiferencias)obj;
		return new EqualsBuilder()
		.appendSuper(super.equals(other))
		.append(this.cambiaria, other.getCambiaria())
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder(19,47)
		.appendSuper(super.hashCode())
		.append(this.cambiaria)
		.toHashCode();
	}
	

}
