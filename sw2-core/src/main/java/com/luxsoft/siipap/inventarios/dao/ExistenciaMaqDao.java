package com.luxsoft.siipap.inventarios.dao;

import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.inventarios.model.ExistenciaMaq;
import com.luxsoft.sw3.maquila.model.Almacen;
import com.luxsoft.siipap.model.core.Producto;

/**
 * Dao para persistir las existencias
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface ExistenciaMaqDao extends GenericDao<ExistenciaMaq, Long>{
	
	/**
	 * Localiza el inventario inicial para un producto, sucursal  año y mes 
	 * 
	 * @param producto La clave del producto
	 * @param sucursal La sucursal
	 * @param year El año
	 * @param mes  El mes (valido 1 al 12)
	 * @return
	 */
	@Transactional(propagation=Propagation.SUPPORTS)
	public ExistenciaMaq buscar(String producto,long almacen,int year,int mes);
	
	
	public ExistenciaMaq buscarPorClaveSiipap(String producto,int clave,int year,int mes);
	
	
	/**
	 * Actualiza las existencias para el articulo indicado en el año y  al fin del mes solicitado
	 * 
	 * @param clave La clave del articulo
	 * @param year  El año o periodo fiscal
	 * @param mes   El mes (1 - 12)
	 * 
	 */
	public void actualizarExistencias(String clave,int year, int mes);
	
	public void actualizarExistencias(Long almacenlId,String clave,int year,int mes);
	
	/**
	 * Actualiza las existencias de todas las sucursales y para todos los articulos al final del mes
	 * 
	 * @param year
	 * @param mes
	 */
	public void actualizarExistencias(int year, int mes);
	
	public void actualizarExistencias();
	
	public void actualizarExistencias(Long almacenId,int year,int mes);
	
	/**
	 * Calcula y regresa la existencia 
	 * 
	 * @param producto El producto
	 * @param sucursal La sucursal 
	 * @param fecha    A la fecha indicada (corte)
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public double calcularExistencia(Producto producto,Almacen almacen,final Date fecha);	
	
	
	/**
	 * Regresa las existencias del producto en el mes en curso
	 * en la fecha/periodo indicado
	 * 
	 * @param producto
	 * @param fecha
	 * @return
	 */
	public List<ExistenciaMaq> buscarExistencias(final Producto producto,final Date fecha);
	
	/**
	 * Regresa todas las existencias para la sucursal indicada
	 * 
	 * @param sucursalId
	 * @param fecha
	 * @return
	 */
	public List<ExistenciaMaq> buscarExistencias(final Long almacenId,final Date fecha);
	
	/**
	 * Genera un registro de existencia si este ya existe lo regresa
	 * 
	 * @param producto
	 * @param fecha
	 * @return
	 */
	public ExistenciaMaq generar(final Producto producto,final Date fecha, final Long almacenId);
		
	/**
	 * Genera un registro de existencia si este ya existe lo regresa
	 * 
	 * @param clave
	 * @param fecha
	 * @param sucursalId
	 * @return
	 */
	public ExistenciaMaq generar(final String  clave,final Date fecha, final Long almacenId);
	
	/**
	 * Genera un registro de existencia si este ya existe lo regresa
	 * 
	 * @param clave
	 * @param sucursalId
	 * @param year
	 * @param mes El mes 1 based en 1= Enero 12= Diciembre
	 * @return
	 */
	public ExistenciaMaq generar(final String  clave,final Long almacenId,int year,int mes);
 
}
