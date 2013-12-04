package com.luxsoft.sw3.services;

import java.util.List;

import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.ProductoRow;
import com.luxsoft.siipap.service.GenericManager;
import com.luxsoft.sw3.model.ProductoInfo;

/**
 * Manager espeicalizado en el manejo de productos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface ProductosManager2 extends GenericManager<Producto, Long>{
	
	public List<Producto> getActivos();
	
	public List<ProductoRow> getActivosAsRows();
	
	public List<ProductoRow> getActivosDolaresAsRows();
	
	public List<Producto> getProductosParaComprasNacionales();
	
	public List<Producto> buscarInventariablesActivos();
	
	public ProductoInfo getProductoInfo(final String clave);
	
	public Producto buscarPorClave(final String clave); 
	
	public List<Producto> getMedidasEspeciales();

}
