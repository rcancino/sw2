package com.luxsoft.sw3.cfd.model;

import com.luxsoft.siipap.cxc.model.Cargo;

/**
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ComprobanteFiscalRow {
	
	private Cargo cargo;
	
	private ComprobanteFiscal cfd;

	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	public ComprobanteFiscal getCfd() {
		return cfd;
	}

	public void setCfd(ComprobanteFiscal cfd) {
		this.cfd = cfd;
	}
	
	

}
