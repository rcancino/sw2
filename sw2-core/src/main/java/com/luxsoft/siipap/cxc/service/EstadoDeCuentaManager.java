package com.luxsoft.siipap.cxc.service;

import java.util.List;

import com.luxsoft.siipap.cxc.model.MovimientoDeCuenta;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;

public interface EstadoDeCuentaManager {
	
	
	/**
	 * Busca los movimientos de cuenta para  un cliente para un periodo
	 * 
	 * @param c
	 * @param p
	 * @return
	 */
	public List<MovimientoDeCuenta> buscarMovimientos(final Cliente c,final Periodo p);

	/**
	 * Busca los movimientos de cuenta para todo un periodo
	 * 
	 * @param p
	 * @return
	 */
	//public List<MovimientoDeCuenta> buscarMovimientos(final Periodo p);
	
}
