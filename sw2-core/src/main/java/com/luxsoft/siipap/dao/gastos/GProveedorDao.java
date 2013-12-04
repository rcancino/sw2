package com.luxsoft.siipap.dao.gastos;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.gastos.GProveedor;

public interface GProveedorDao extends GenericDao<GProveedor, Long>{
	
	public GProveedor buscarPorNombre(final String clave);
	
	public GProveedor buscarPorRfc(final String rfc);

}
