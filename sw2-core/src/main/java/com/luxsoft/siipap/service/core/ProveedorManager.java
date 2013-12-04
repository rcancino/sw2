package com.luxsoft.siipap.service.core;



import java.util.List;

import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.GenericManager;

public interface ProveedorManager extends GenericManager<Proveedor, Long>{
	
	public Proveedor buscarPorClave(final String clave);
	
	public Proveedor buscarPorNombre(final String nombre);
	
	public Proveedor buscarInicializado(final String clave);

	public Proveedor buscarPorRfc(final String rfc);
	
	public List<Proveedor> buscarActivos();
	
	public List<Proveedor> buscarImportadores();
	

}
