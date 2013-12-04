package com.luxsoft.siipap.ventas.dao;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;

public interface ListaDePreciosClienteDao extends GenericDao<ListaDePreciosCliente, Long>{

	
	/**
	 * Regresa una lista de las listas de precios vigentes
	 * 
	 * @return
	 */
	public List<ListaDePreciosCliente> buscarListasVigentes();
	
	/**
	 * Busca la lista de precios vigente
	 * 
	 * @param p
	 * @return
	 */
	public ListaDePreciosCliente buscarListaVigente(Cliente p);
	
	/**
	 * Localiza un precio para el producto/cliente indicado
	 * 
	 * @param c
	 * @param p
	 * @param moneda
	 * @return El precio si existe o null si no existe
	 */
	public BigDecimal buscarPrecio(final Cliente c,final Producto p,final Currency moneda);
	
	public double buscarDescuentoPorProducto(final Cliente c,final Producto p,final Currency moneda);
	
	/**
	 * Regresa la lista de precios para el periodo indicado
	 * 
	 * @param p
	 * @return
	 */
	//public List<ListaDePreciosCliente> buscarListas(final Periodo p);
	
	
	/**
	 * Hace una copia de la lista de precios 
	 * 
	 * @param id
	 * @return
	 */
	public ListaDePreciosCliente copiar(Long id);
}
