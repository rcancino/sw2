package com.luxsoft.sw3.services;

import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.compras.model.DevolucionDeCompra;
import com.luxsoft.siipap.compras.model.RecepcionDeCompra;
import com.luxsoft.siipap.inventarios.dao.MovimientoDao;
import com.luxsoft.siipap.inventarios.model.Conteo;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.inventarios.model.ExistenciaConteo;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.PreDevolucion;

/**
 * Manager para el mantenimiento de inventarios
 * 
 * @author Ruben Cancino
 *
 */
public interface InventariosManager {
	
	/**
	 * Acceso al DAO de Movimientos
	 * 
	 * @return
	 */
	public MovimientoDao getMovimientoDao();
	
	/**
	 * Persiste un movimiento de inventario
	 * 
	 * @param mov
	 * @return
	 */
	public Movimiento salvarMovimiento(final Movimiento mov);
	
	public void eliminarMovimiento(final Movimiento mov);
	
	public int buscarFolio(final Movimiento mov);
	
	
	/**
	 * Actualiza la existencia en una transaccion segura
	 * 
	 * @param exis
	 * @return
	 */
	//public Existencia actualizarExistencia(final Existencia exis);
	
	/**
	 * Regresa las existencias de la sucursal indicada 
	 * 
	 * @param s
	 * @return
	 */
	public List<Existencia> buscarExistencias(final Sucursal s);
	

	/**
	 * Actualiza la existencia. Genera el registro de existencia si este no
	 * se ha generado
	 * 
	 * @param inv
	 * @return
	 */
	public Existencia actualizarExistencia(final Inventario inv);
	
	/**
	 * Regresa las existencias actuales del producto
	 * 
	 * @param producto
	 * @return
	 */
	public List<Existencia> buscarExistencias(final Producto producto);
	
	public List<Existencia> buscarExistencias(final Producto producto,int year,int mes);	
	
	public Existencia buscarExistencia(final Sucursal suc,Producto producto,int year,int mes);
	
	
	public Devolucion salvarDevolucion(final Devolucion d);
	
	public Devolucion generarDevolucion(final PreDevolucion preDevolucion,final Date fecha);
	
	public Traslado[] generarSalidaPorTraslado(final SolicitudDeTraslado sol,final Date time,String chofer,String usuario,String surtidor,String supervisor,String cortador);
	
	public Traslado cancelar(final Traslado t);
	
	public DevolucionDeCompra registrarDevolucionDeCompra(final DevolucionDeCompra dec);
	
	public DevolucionDeCompra cancelarDevolucionDeCompra(final DevolucionDeCompra dec);
	
	public Conteo registrarConteo(final Conteo conteo);
	
	public void generarExistenciasParaConteo(final Long sucursalId,Date fecha,String user);
	
	public void generarExistenciasParaConteo(List<Existencia> exis,boolean parcial,Date fecha,String user);
	
	public void generarAjusteDeInventario(Sucursal sucursal,Date fecha);
	
	public PreDevolucion salvarPreDevolucion(final PreDevolucion preDevo);
	
	
	/** Maquila **/
	
	public RecepcionDeMaquila getRecepcion(String id);
	
	public RecepcionDeMaquila salvarRecepcion(final RecepcionDeMaquila recepcion);
	
	public void eliminarRecepcion(final RecepcionDeMaquila recepcion);

}
