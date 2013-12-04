package com.luxsoft.siipap.compras.dao;

import java.util.List;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.core.Proveedor;

public interface ProveedorDao extends GenericDao<Proveedor, Long>{
	
	public Proveedor buscarPorClave(final String clave);
	
	public Proveedor buscarPorNombre(final String nombre);
	
	public Proveedor buscarPorRfc(final String rfc);
	
	public List<Proveedor> buscarActivos();
	
	public List<Proveedor> buscarImportadores();

}
