package com.luxsoft.siipap.dao.core;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Folio;
import com.luxsoft.siipap.model.core.FolioId;


/**
 * Presite folios del sistema
 * 
 * @author Ruben Cancino
 *
 */
public interface FolioDao extends GenericDao<Folio, FolioId>{
	
	/**
	 * Busca el folio para el tipo y sucursal indicado, incrementando
	 * su valor
	 * 
	 * @param s
	 * @param tipo
	 * @return
	 */
	public Folio buscarNextFolio(final Sucursal s,final String tipo);

}
