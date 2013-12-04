package com.luxsoft.siipap.dao.legacy;

import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.model.legacy.Deposito;

public interface DepositoDao {
	
	/**
	 * Busca los depositos generados para la fecha indicada
	 * 
	 * @param fechaIni
	 * @param fechaFin
	 * @return
	 */
	public List<Deposito> buscarDepositos(Date fechaIni,Date fechaFin) ;
	
	public Deposito save(Deposito d);
	
}