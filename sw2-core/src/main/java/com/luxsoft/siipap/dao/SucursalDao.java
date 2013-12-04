package com.luxsoft.siipap.dao;

import com.luxsoft.siipap.model.Sucursal;

public interface SucursalDao extends GenericDao<Sucursal, Long>{
	
	public Sucursal buscarPorClave(Integer clave);

}
