/**
 * 
 */
package com.luxsoft.sw3.cfd.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class FolioFiscalId implements Serializable{
	
	@Column(name="SUCURSAL_ID",nullable=false)
	private Long sucursal;
	
	@Column(name="SERIE",length=15,nullable=false)
	private String serie;
	

	public FolioFiscalId() {
		
	}

	public FolioFiscalId(Long sucursal, String serie) {
		
		this.sucursal = sucursal;
		this.serie = serie;
	}

	public Long getSucursal() {
		return sucursal;
	}

	public void setSucursal(Long sucursal) {
		this.sucursal = sucursal;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	
	
	
}