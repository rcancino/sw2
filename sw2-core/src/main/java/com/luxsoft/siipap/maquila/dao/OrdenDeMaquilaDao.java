package com.luxsoft.siipap.maquila.dao;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;

public interface OrdenDeMaquilaDao extends GenericDao<RecepcionDeMaquila, Long>{
	
	
	/**
	 * Localiza ordenes migradas de oracle
	 * 
	 * @param sucursal
	 * @param folio
	 * @return
	 */
	public RecepcionDeMaquila buscarPorFolioSucursal(final int sucursal,final int folio);
	
	/**
	 * Commoditi para buscar movimientos migrados de Oracle
	 * 
	 * @param comId
	 * @return
	 */
	public EntradaDeMaquila buscarPorComId(final Long comId,final int renglon);

}
