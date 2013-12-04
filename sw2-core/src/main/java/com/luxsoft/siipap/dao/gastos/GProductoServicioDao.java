package com.luxsoft.siipap.dao.gastos;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.gastos.GProductoServicio;

public interface GProductoServicioDao extends GenericDao<GProductoServicio, Long>{
	
	public GProductoServicio buscarPorClave(final String clave);

}
