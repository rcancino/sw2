package com.luxsoft.sw3.services;

import com.luxsoft.siipap.cxc.model.AutorizacionDeAbono;
import com.luxsoft.siipap.service.GenericManager;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

/**
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface SolicitudDeDepositosManager extends GenericManager<SolicitudDeDeposito, String>{
	
	/**
	 * Autorizacion de una solicitud de deposito
	 * 
	 * @param sol
	 * @return
	 */
	public SolicitudDeDeposito autorizar(SolicitudDeDeposito sol);
	
	public SolicitudDeDeposito autorizar(SolicitudDeDeposito sol,AutorizacionDeAbono aut);
	
	public SolicitudDeDeposito buscarDuplicada(SolicitudDeDeposito sol);

}
