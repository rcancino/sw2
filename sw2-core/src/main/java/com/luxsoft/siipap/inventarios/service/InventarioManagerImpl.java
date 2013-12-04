package com.luxsoft.siipap.inventarios.service;


import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.compras.dao.EntradaPorCompraDao;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.inventarios.dao.ExistenciaDao;
import com.luxsoft.siipap.inventarios.dao.InventarioDao;
import com.luxsoft.siipap.inventarios.dao.KitDao;
import com.luxsoft.siipap.inventarios.dao.MovimientoDao;
import com.luxsoft.siipap.inventarios.dao.TrasladoDao;
import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.inventarios.model.Kit;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.inventarios.model.Transformacion;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.impl.GenericManagerImpl;

public class InventarioManagerImpl extends GenericManagerImpl<Movimiento, String> implements InventarioManager{
	
	private InventarioDao inventarioDao;
	private TrasladoDao trasladoDao;
	private ExistenciaDao existenciasDao;
	private KitDao kitDao;
	private UniversalDao universalDao;
	private EntradaPorCompraDao entradaPorCompraDao;
	private CostoPromedioManager costoPromedioManager;
	
	
	public InventarioManagerImpl(MovimientoDao dao) {
		super(dao);
	}
	/**
	 * Dao origen
	 * 
	 * @return
	 */
	protected MovimientoDao getMovimientoDao(){
		return (MovimientoDao)genericDao;
	}
	
	
	
	@Override
	@Transactional (propagation=Propagation.REQUIRED)
	public Movimiento save(Movimiento object) {
		Movimiento res=super.save(object);
		for(MovimientoDet det:res.getPartidas()){
			actualizarExistencia(det);
		}
		return res;
	}
	
	/** Manejo de existencias ***/
	
	@Transactional (propagation=Propagation.REQUIRED)
	public Existencia actualizarExistencia(final Inventario mov){
		//return getExistenciasDao().actualizarExistencia(mov.getProducto(), mov.getSucursal());
		return null;
	}
	
	
	/** FIN Manejo de existencias ***/

	/** Manejo de Traslados ****/
	
	public Traslado salvarTraslado(Traslado t) {
		return getTrasladoDao().save(t);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.inventarios.service.InventarioManager#getTraslado(java.lang.Long)
	 */
	public Traslado getTraslado(Long id) {
		return getTrasladoDao().get(id);
	}
	
	/** FIN Manejo de Traslados ****/
	
	
	/** Manejo de transformaciones ***/
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.inventarios.service.InventarioManager#salvarTransformacion(com.luxsoft.siipap.inventarios.model.Transformacion)
	 */
	@Transactional (propagation=Propagation.REQUIRED)
	public Transformacion salvarTransformacion(Transformacion trs) {
		/*//Assert.isTrue(trs.validarMovimientos(),"Existe un problema con el signo de los movimientos del TRS");
		trs.generarMovimientos();
		trs=(Transformacion)universalDao.save(trs);
		actualizarExistencia(trs.getSalida());
		actualizarExistencia(trs.getEntrada());*/
		return null;
	}
	
	/** FIN Manejo de transformaciones ***/
	
	/** Manejo de movimientos por compra ***/
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.inventarios.service.InventarioManager#salvarEntradaPorCompra(com.luxsoft.siipap.compras.model.EntradaPorCompra)
	 */
	@Transactional (propagation=Propagation.REQUIRED)
	public EntradaPorCompra salvarEntradaPorCompra(EntradaPorCompra e) {
		e=getEntradaPorCompraDao().save(e);
		//actualizarExistencia(e);
		return e;
	}
	
	
	@Transactional (propagation=Propagation.REQUIRED)
	public Kit salvarKit(Kit kit) {
		return getKitDao().save(kit);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.inventarios.service.InventarioManager#buscarMovimientsKit(com.luxsoft.siipap.model.Periodo)
	 */
	public List<Kit> buscarMovimientsKit(Periodo p) {
		return getKitDao().buscarMovimientsKit(p);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.inventarios.service.InventarioManager#eliminarMovimientoKit(com.luxsoft.siipap.inventarios.model.Kit)
	 */
	@Transactional (propagation=Propagation.REQUIRED)
	public void eliminarMovimientoKit(Kit kit) {
		getKitDao().remove(kit.getId());
		
	}
	
	@Transactional (propagation=Propagation.REQUIRED)
	public Existencia generarExistenciaDeProducto(Sucursal sucursal,Producto producto) {
		final Date date=new Date();
		int year=Periodo.obtenerYear(date);
		int mes=Periodo.obtenerMes(date)+1;
		Existencia exis=existenciasDao.buscar(producto.getClave(), sucursal.getId(), year, mes);
		if(exis!=null)
			return exis;
		else{
			exis=new Existencia();
			exis.setProducto(producto);
			exis.setSucursal(sucursal);
			exis.setYear(year);
			exis.setMes(mes);
			exis.setFecha(date);
			CostoPromedio costo=costoPromedioManager.buscarCostoPromedio(year, mes, producto.getClave());
			if(costo!=null){
				exis.setCostoPromedio(costo.getCostop());
				exis.setCostoUltimo(costo.getCostoUltimo());				
			}
			return existenciasDao.save(exis);
		}
	}
	/** Colaboradores **/
	
	public InventarioDao getInventarioDao() {
		return inventarioDao;
	}
	public void setInventarioDao(InventarioDao inventarioDao) {
		this.inventarioDao = inventarioDao;
	}	

	public TrasladoDao getTrasladoDao() {
		return trasladoDao;
	}
	public void setTrasladoDao(TrasladoDao trasladoDao) {
		this.trasladoDao = trasladoDao;
	}
	public ExistenciaDao getExistenciasDao() {
		return existenciasDao;
	}
	public void setExistenciasDao(ExistenciaDao existenciasDao) {
		this.existenciasDao = existenciasDao;
	}
	public void setUniversalDao(UniversalDao universalDao) {
		this.universalDao = universalDao;
	}
	public EntradaPorCompraDao getEntradaPorCompraDao() {
		return entradaPorCompraDao;
	}
	public void setEntradaPorCompraDao(EntradaPorCompraDao entradaPorCompraDao) {
		this.entradaPorCompraDao = entradaPorCompraDao;
	}
	public KitDao getKitDao() {
		return kitDao;
	}
	public void setKitDao(KitDao kitDao) {
		this.kitDao = kitDao;
	}
	public CostoPromedioManager getCostoPromedioManager() {
		return costoPromedioManager;
	}
	public void setCostoPromedioManager(CostoPromedioManager costoPromedioManager) {
		this.costoPromedioManager = costoPromedioManager;
	}
	
	

}
