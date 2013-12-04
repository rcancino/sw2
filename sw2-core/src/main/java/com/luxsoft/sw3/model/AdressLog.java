package com.luxsoft.sw3.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Componente para almacenar informacion de la direccion IP y Mac  
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Embeddable
public class AdressLog implements Serializable{
	
	@Column(name="IP_CREATED",nullable=true ,length=50)
	private String createdIp;
	
	@Column(name="IP_UPDATED",nullable=true ,length=50)
	private String updatedIp;
	
	@Column(name="MAC_CREATED",nullable=true,length=50)
	private String createdMac;
	
	@Column(name="MAC_UPDATED",nullable=true,length=50)
	private String updatedMac;

	

	public String getCreatedIp() {
		return createdIp;
	}

	public void setCreatedIp(String createdIp) {
		this.createdIp = createdIp;
	}

	public String getUpdatedIp() {
		return updatedIp;
	}

	public void setUpdatedIp(String updatedIp) {
		this.updatedIp = updatedIp;
	}

	public String getCreatedMac() {
		return createdMac;
	}

	public void setCreatedMac(String createdMac) {
		this.createdMac = createdMac;
	}

	public String getUpdatedMac() {
		return updatedMac;
	}

	public void setUpdatedMac(String updatedMac) {
		this.updatedMac = updatedMac;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((createdIp == null) ? 0 : createdIp.hashCode());
		result = prime
				* result
				+ ((createdMac == null) ? 0 : createdMac.hashCode());
		result = prime
				* result
				+ ((updatedIp == null) ? 0 : updatedIp.hashCode());
		result = prime * result
				+ ((updatedMac == null) ? 0 : updatedMac.hashCode());
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
		AdressLog other = (AdressLog) obj;
		if (createdIp == null) {
			if (other.createdIp != null)
				return false;
		} else if (!createdIp.equals(other.createdIp))
			return false;
		if (createdMac == null) {
			if (other.createdMac != null)
				return false;
		} else if (!createdMac.equals(other.createdMac))
			return false;
		if (updatedIp == null) {
			if (other.updatedIp != null)
				return false;
		} else if (!updatedIp.equals(other.updatedIp))
			return false;
		if (updatedMac == null) {
			if (other.updatedMac != null)
				return false;
		} else if (!updatedMac.equals(other.updatedMac))
			return false;
		return true;
	}
	
	

}
