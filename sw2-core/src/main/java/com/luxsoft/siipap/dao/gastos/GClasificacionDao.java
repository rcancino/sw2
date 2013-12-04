package com.luxsoft.siipap.dao.gastos;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;


public interface GClasificacionDao extends GenericDao<ConceptoDeGasto, Long>{
	
	public ConceptoDeGasto buscarPorClave(final String clave);

}
