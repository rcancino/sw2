package com.luxsoft.siipap.cxc.service;

import com.luxsoft.siipap.cxc.model.Ficha;



public interface DepositosManager {
	
	public Ficha cancelarDeposito(final String fichaId);
	
	public Ficha save(final Ficha ficha);

}

