package com.luxsoft.siipap.inventarios.service;

import java.util.List;

import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.inventarios.model.Kit;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.Transformacion;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.GenericManager;


public interface InventarioManager extends GenericManager<Movimiento, String>{
		
	
	
	public Traslado getTraslado(final Long id);
	
	/**
	 * Persiste un traslado de material entre sucursales
	 * 
	 * @param t
	 * @return
	 */
	public Traslado salvarTraslado(final Traslado t);
	
	/**
	 * Salva de manera consistente una transformacion
	 * 
	 * @param trs
	 * @return
	 */
	public Transformacion salvarTransformacion(final Transformacion trs);
	
	/**
	 * Salva la entrada por compra. Como regla de negocios adicionalmente
	 * a la persistencia del movimiento de entrada la implementacion esta
	 * obligada a actualizar la existencia como resultado de la entrada
	 * 
	 * @param e
	 * @return
	 */
	public EntradaPorCompra salvarEntradaPorCompra(final EntradaPorCompra e);
	
	
	/**
	 * Actualiza la existencia del inventario en funcion
	 * del movimiento registrado
	 * 
	 * @param mov
	 * @return
	 */
	public Existencia actualizarExistencia(final Inventario mov);
	
	/**
	 * Persiste un movimiento kit y sus partes
	 * 
	 * @param kit
	 * @return
	 */
	public Kit salvarKit(final Kit kit);
	
	/**
	 * Busca todos los movimientos de entrada y salida generados
	 * por creacion de productos kit
	 * 
	 * @param p
	 * @return
	 */
	public List<Kit> buscarMovimientsKit(final Periodo p);
	
	/**
	 * Elimina un movimiento kit del inventario, regresando 
	 * las existencias originales
	 * Existen siertas restricciones en este proceso
	 * 
	 * @param kit
	 */
	public void eliminarMovimientoKit(final Kit kit);
	
	/**
	 * Genera la existencia del producto indicado
	 * 
	 * @param sucursal
	 * @param producto
	 * @return
	 */
	public Existencia generarExistenciaDeProducto(final Sucursal sucursal,final Producto producto);
	
	
}
