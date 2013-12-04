package com.luxsoft.sw3.services;

import java.util.Date;
import java.util.List;



import com.luxsoft.siipap.compras.dao.ListaDePreciosDao;
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.compras.model.RecepcionDeCompra;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.GenericManager;

/**
 * Service Manager para el control de Ordenes de Compra y entidades
 * relacionadas
 * 
 * @author Ruben Cancino
 *
 */
public interface ComprasManager extends GenericManager<Compra2, String>{
	
	/**
	 * Regresa una lista de compras para le periodo indicado
	 * 
	 * @param p
	 * @return
	 */
	public List<Compra2> buscarCompras(Periodo p);
	
	/**
	 * Lista de partidas pendientes de ser surtidas por proveedor
	 * 
	 * @param p
	 * @return
	 */
	public List<CompraUnitaria> buscarPendientesPorProveedor(final Proveedor p);
	
	/**
	 *  Busca la compra por sucursal y folio
	 *  
	 * @param sucursal La sucursal requerida
	 * @param folio    El folio de la sucursal
	 * @return
	 */
	public Compra2 buscarPorFolio(final int sucursal,final int folio);
	
	/**
	 * Regresa la lista de partidas correspondiente a las compra
	 * 
	 * @param compra
	 * @return
	 */
	public List<CompraUnitaria> buscarPartidas(final Compra2 compra);
	
	/**
	 * Regresa la compra completamente inicializada
	 * 
	 * @param id
	 * @return
	 */
	public Compra2 buscarInicializada(final String id);
	
	public List<Compra2> buscarPendientes();
	
	public ListaDePreciosDao getListaDePrecios();

	public void asignarPrecioDescuento(CompraUnitaria det);
	
	public void actualizarPrecios(Compra2 compra);
	
	public Compra2 saveCentralizada(Compra2 object); 
	
	public Compra2 cancelar(Compra2 compra,boolean centralizada);
	
	public Compra2 depurar(String compraId,final Date fecha);
	
	public Compra2 cerrar(Compra2 compra,final Date fecha);
	
	public Compra2 cancelarCierre(final Compra2 compra);
	
	public Compra2 obtenerCopiaModificable(final String compraId);
	
	public RecepcionDeCompra registrarRecepcion(final RecepcionDeCompra recepcion);
	
	/**
	 * Regresa una recepcion de compra correctamente inicializada
	 * 
	 * @param id
	 * @return
	 */
	public RecepcionDeCompra getRecepcion(final String id);
	
	/**
	 * Consolida un grupo de compras
	 * 
	 * @param compras
	 * @return
	 */
	public Compra2 consolidarCompras(final List<String> ids);
	
	
	

}
