package com.luxsoft.siipap.cxc.dao;

import com.luxsoft.siipap.cxc.model.AutorizacionParaCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.dao.GenericDao;

public interface NotaDeCargoDao extends GenericDao<NotaDeCargo, String>{
	
	/**
	 * Cancela una nota de cargo
	 * 
	 * @param id
	 */
	//public void cancelar(final String id,final AutorizacionParaCargo aut);

}
