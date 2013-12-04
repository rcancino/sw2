package com.luxsoft.siipap.dao.tesoreria;

import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.tesoreria.Transferencia;

/**
 * Persistencia para Traspasos
 * 
 * @author Ruben Cancino
 *
 */
public interface TransferenciaDao extends GenericDao<Transferencia, Long>{
	
	/**
	 * Persiste la transferencia bajo el concepto indicado
	 * 
	 * @param t La transferencia en cuestrion
	 * @param tipo El concepto apropiado
	 * @param suc  La sucursal normalmente oficionas
	 */
	//public void save(Transferencia t,Concepto tipo,Sucursal suc);
	
	@Transactional 
	public Transferencia save(Transferencia t);
	
	@Transactional
	public void remove(Long id);

}
