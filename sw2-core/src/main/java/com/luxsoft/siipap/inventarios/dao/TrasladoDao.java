package com.luxsoft.siipap.inventarios.dao;

import java.util.List;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.model.Periodo;

public interface TrasladoDao extends GenericDao<Traslado, Long>{
	

	public Traslado inicializarTraslado(final Long id);
	
	public List<Traslado> buscarTraslado(Periodo p);
}
