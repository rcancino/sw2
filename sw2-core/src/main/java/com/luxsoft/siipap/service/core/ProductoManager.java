package com.luxsoft.siipap.service.core;



import java.util.List;

import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.GenericManager;

public interface ProductoManager extends GenericManager<Producto, Long>{
	
	
	
	public Producto buscarPorClave(final String clave); 
	
	public List<Producto> buscarProductosActivos();
	
	
	public List<Producto> buscarProductosActivosYDeLinea();

}
