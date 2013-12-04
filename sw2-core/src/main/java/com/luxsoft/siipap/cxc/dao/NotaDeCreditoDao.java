package com.luxsoft.siipap.cxc.dao;

import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.dao.GenericDao;

public interface NotaDeCreditoDao extends GenericDao<NotaDeCredito, String>{
	
	/**
	 * Busca una nota de credito por el Id de siipapwin
	 * 
	 * @param id
	 * @return
	 */
	public NotaDeCredito buscarPorSiipapId(final Long id);
	
	
	

}
