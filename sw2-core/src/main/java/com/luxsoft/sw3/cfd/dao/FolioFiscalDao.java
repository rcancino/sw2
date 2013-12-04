package com.luxsoft.sw3.cfd.dao;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.sw3.cfd.model.FolioFiscal;
import com.luxsoft.sw3.cfd.model.FolioFiscalId;
import com.luxsoft.sw3.cfd.model.SerieNoExistenteException;


/**
 * DAO para la persistencia de los folios fiscales
 * 
 * @author Ruben Cancino
 *
 */
public interface FolioFiscalDao extends GenericDao<FolioFiscal, FolioFiscalId>{
	
	/**
	 * Regresa el folio fiscal para la sucursal y serie indicado
	 * 
	 * @param s
	 * @param serie
	 * @return
	 * @throws SerieNoExistenteException Si la serie no esta registrada
	 */
	public FolioFiscal buscarFolio(final Sucursal s,final String serie)throws SerieNoExistenteException;

}
