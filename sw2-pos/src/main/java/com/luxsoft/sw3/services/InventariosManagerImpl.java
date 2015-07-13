package com.luxsoft.sw3.services;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.compras.model.DevolucionDeCompra;
import com.luxsoft.siipap.compras.model.DevolucionDeCompraDet;
import com.luxsoft.siipap.compras.model.EntradaPorCompraTest;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion.TipoDeDevolucion;
import com.luxsoft.siipap.dao.core.FolioDao;
import com.luxsoft.siipap.inventarios.dao.ExistenciaDao;
import com.luxsoft.siipap.inventarios.dao.MovimientoDao;
import com.luxsoft.siipap.inventarios.model.Conteo;
import com.luxsoft.siipap.inventarios.model.ConteoDet;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.inventarios.model.ExistenciaConteo;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.inventarios.model.Sector;
import com.luxsoft.siipap.inventarios.model.SectorDet;
import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.inventarios.model.SolicitudDeTrasladoDet;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.inventarios.model.TrasladoDet;
import com.luxsoft.siipap.inventarios.model.Movimiento.Concepto;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Folio;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.tesoreria.Cuenta.Clasificacion;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;
import com.luxsoft.siipap.ventas.model.PreDevolucion;
import com.luxsoft.siipap.ventas.model.PreDevolucionDet;
import com.luxsoft.sw3.maquila.dao.RecepcionDeMaquilaDao;
import com.luxsoft.sw3.ventas.Pedido.ClasificacionVale;


/**
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Service("inventariosManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class InventariosManagerImpl implements InventariosManager{
	
	@SuppressWarnings("unused")
	private Logger logger=Logger.getLogger(getClass());
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private MovimientoDao movimientoDao;
	
	@Autowired
	private ExistenciaDao existenciaDao;
	
	@Autowired
	private RecepcionDeMaquilaDao recepcionDeMaquilaDao;
	
	@Autowired
	private FolioDao folioDao;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
		
	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	public Movimiento salvarMovimiento(Movimiento mov) {
		
		Date time=obtenerFechaDelSistema();
		mov.setFecha(time);
		String user=KernellSecurity.instance().getCurrentUserName();
		
		//Bitacoras
		if(mov.getId()==null){
			mov.getUserLog().setCreado(time);
			mov.getUserLog().setCreateUser(user);
			
		}
		mov.getUserLog().setModificado(time);
		mov.getUserLog().setUpdateUser(user);
		/*TODO implementar  Bitacoras Address
		
		conteo.getAddresLog().setUpdatedIp(KernellSecurity.getIPAdress());
		conteo.getAddresLog().setUpdatedMac(KernellSecurity.getMacAdress());
		conteo.getAddresLog().setCreatedIp(KernellSecurity.getIPAdress());
		conteo.getAddresLog().setCreatedMac(KernellSecurity.getMacAdress());
		*/
		return salvarMovimientoEnTransaccion(mov, time);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	private Movimiento salvarMovimientoEnTransaccion(Movimiento mov,Date time){
		if(mov.getId()==null){
			String tipo="MOV_"+mov.getConcepto().name();
			Folio folio=folioDao.buscarNextFolio(mov.getSucursal(), tipo);
			mov.setDocumento(folio.getFolio().intValue());
			int renglon=0;
			for(MovimientoDet det:mov.getPartidas()){
				det.setFecha(time);
				det.setDocumento(folio.getFolio());
				det.setCreado(time);
				det.setModificado(time);
				renglon++;
				det.setRenglon(renglon);
				if(!mov.getConcepto().equals(Movimiento.Concepto.AJU)){
					Existencia exis=existenciaDao.buscar(det.getProducto().getClave(), det.getSucursal().getId(), det.getYear(), det.getMes());
					if(exis==null){
						exis=existenciaDao.generar(det.getProducto(), det.getFecha(),det.getSucursal().getId());
					}
					exis.setCantidad(exis.getCantidad()+det.getCantidad());
					existenciaDao.save(exis);
				}	
				
			}
			folioDao.save(folio);
		}else{
			int renglon=0;
			for(MovimientoDet det:mov.getPartidas()){
				det.setModificado(time);
				renglon++;
				det.setRenglon(renglon);
				Existencia exis=existenciaDao.buscar(det.getProducto().getClave(), det.getSucursal().getId(), det.getYear(), det.getMes());
				if(exis==null){
					exis=existenciaDao.generar(det.getProducto(), det.getFecha(),det.getSucursal().getId());
				}
				exis.setCantidad(recalcularExistencia(exis));
				//this.hibernateTemplate.saveOrUpdate(exis);
				existenciaDao.save(exis);
			}
		}
		return movimientoDao.save(mov);
	}
	
	

	@Transactional(propagation=Propagation.REQUIRED)
	public void eliminarMovimiento(final Movimiento mov){
		for(MovimientoDet det:mov.getPartidas()){
			actualizarExistencia(det);
		}
		movimientoDao.remove(mov.getId());
	}
	
	public int buscarFolio(Movimiento mov) {
		String hql="select max(v.documento) from Movimiento v where v.sucursal.id=? ";
		List<Integer> res=hibernateTemplate.find(hql, mov.getSucursal().getId());
		return res.get(0)!=null?res.get(0).intValue()+1:1;
	}
	
	

	/**
	 * Actualiza la existencia en una transaccion segura quien mande llamar este metodo
	 * debe estar en una transaccion
	 * 
	 * @param exis
	 * @return
	 
	@Transactional(propagation=Propagation.MANDATORY)
	public Existencia actualizarExistencia(final Existencia exis){		
		return existenciaDao.save(exis);
	}
	*/
	
	/**
	 * Regresa las existencias de la sucursal indicada 
	 * 
	 * @param s
	 * @return
	 */
	public List<Existencia> buscarExistencias(final Sucursal s){
		String hql="from Existencia e " +
				" left join fetch e.producto p" +
				" left join fetch e.sucursal s" +
				" where s.id=?" +
				"   and p.inventariable=true" +
				"   and p.activoInventario=true"+
				" and e.year=YEAR(CURRENT_DATE()) " +
				" and e.mes=MONTH(CURRENT_DATE())"
				;
		return getHibernateTemplate().find(hql, s.getId());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.services.InventariosManager#buscarExistencias(com.luxsoft.siipap.model.core.Producto)
	 */
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public List<Existencia> buscarExistencias(final Producto producto) {
		if(!producto.isInventariable())
			return new ArrayList<Existencia>();
		final String hql="from Existencia i where i.clave=? " +
				" and i.year=YEAR(CURRENT_DATE()) " +
				" and i.mes=MONTH(CURRENT_DATE())" +
				" and i.sucursal.habilitada=true";		
		List<Existencia> res=getHibernateTemplate().find(hql, new Object[]{producto.getClave()});
		return res;		
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public List<Existencia> buscarExistencias(final Producto producto,int year,int mes){
		if(!producto.isInventariable())
			return new ArrayList<Existencia>();
		final String hql="from Existencia i where i.clave=? " +
				" and i.year=?" +
				" and i.mes=?" +
				" and i.sucursal.habilitada=true";		
		List<Existencia> res=getHibernateTemplate().find(hql, new Object[]{producto.getClave(),year,mes});
		return res;		
	}
	
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public Existencia actualizarExistencia(final Inventario inv){
		Existencia exis=existenciaDao.buscar(inv.getProducto().getClave(), inv.getSucursal().getId(), inv.getYear(), inv.getMes());
		if(exis==null){
			exis=existenciaDao.generar(inv.getProducto(), inv.getFecha(),inv.getSucursal().getId());
		}
		exis.setCantidad(exis.getCantidad()+inv.getCantidad());
		return existenciaDao.save(exis);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public double recalcularExistencia(Existencia exis) {
		String hql="select sum(i.cantidad) from Inventario i where i.producto.id=? ";
		Double res=(Double) this.hibernateTemplate.iterate(hql, exis.getProducto().getId()).next();
		return res!=null?res:res;
		
	}
	

	@Transactional(propagation=Propagation.REQUIRED)
	public Devolucion salvarDevolucion(Devolucion d) {
		Assert.isNull(d.getId(),"La devolucion ya se ha persistido, no se puede modificar");
		String tipo="DEV_FAC";
		Folio folio=folioDao.buscarNextFolio(d.getVenta().getSucursal(), tipo);
		d.setNumero(folio.getFolio());
		if(d.isTotal()){
			d.setImporte(d.getVenta().getImporte());
			d.setImpuesto(d.getVenta().getImpuesto());		
			d.setTotal(d.getVenta().getTotal());
		}else{
			CantidadMonetaria importe=CantidadMonetaria.pesos(0);
			for(DevolucionDeVenta det:d.getPartidas()){
				CantidadMonetaria precio=CantidadMonetaria.pesos(det.getVentaDet().getPrecio());
				double cantidad=det.getCantidadEnUnidad();
				double descuento=det.getVentaDet().getDescuento();
				
				CantidadMonetaria imp=precio.multiply(cantidad);
				CantidadMonetaria impDescuento=imp.multiply(Math.abs(descuento/100));
				imp=imp.subtract(impDescuento);
				
				int cortes=det.getCortes();
				CantidadMonetaria pcortes=CantidadMonetaria.pesos(det.getVentaDet().getPrecioCorte());
				CantidadMonetaria importeCortes=pcortes.multiply((double)cortes);
				imp=imp.add(importeCortes);
				det.setCortes(cortes);
				
				
				importe=importe.add(imp);
				//det.setDocumento(d.getNumero());
			}
			d.setImporte(importe.amount());
			d.setImpuesto(MonedasUtils.calcularImpuesto(importe.amount()));		
			d.setTotal(MonedasUtils.calcularTotal(importe.amount()));
		}
		
		for(DevolucionDeVenta det:d.getPartidas()){
			det.setDocumento(d.getNumero());
			
		}
		
		Devolucion res=(Devolucion)getHibernateTemplate().merge(d);
		folioDao.save(folio);
		if(res.getVenta().getOrigen().equals(OrigenDeOperacion.MOS)){
			generarNotaPorDevolucion(res);
		}
		for(DevolucionDeVenta det:res.getPartidas()){
			actualizarExistencia(det);
		}
		return res;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Devolucion generarDevolucion(PreDevolucion preDevolucion,final Date fecha) {
		preDevolucion=(PreDevolucion)this.hibernateTemplate.get(PreDevolucion.class, preDevolucion.getId());
		Devolucion devo=preDevolucion.generarRMD(fecha);
		
		devo=salvarDevolucion(devo);
		preDevolucion.setDevolucion(devo);
		this.hibernateTemplate.saveOrUpdate(preDevolucion);
		return devo;
	}

	@Transactional(propagation=Propagation.MANDATORY)
	private NotaDeCreditoDevolucion generarNotaPorDevolucion(Devolucion d){
		NotaDeCreditoDevolucion n=new NotaDeCreditoDevolucion();
		n.setAplicable(Boolean.TRUE);
		n.setDevolucion(d);
		n.setComentario(d.getComentario());
		n.setFecha(d.getFecha());
		n.setImporte(d.getImporte());
		n.setImpuesto(d.getImpuesto());
		n.setLiberado(d.getFecha());
		n.setMoneda(d.getVenta().getMoneda());
		n.setOrigen(d.getVenta().getOrigen());
		n.setSucursal(d.getVenta().getSucursal());
		TipoDeDevolucion tipo=TipoDeDevolucion.PARCIAL;
		if(d.getTotal().equals(d.getVenta().getTotal()))
			tipo=TipoDeDevolucion.TOTAL;
		n.setTipoDeDevolucion(tipo);
		n.setTotal(d.getTotal());
		n.getLog().setCreado(d.getLog().getCreado());
		n.getLog().setCreateUser(d.getLog().getCreateUser());
		n.getLog().setModificado(d.getLog().getModificado());
		n.getLog().setUpdateUser(d.getLog().getUpdateUser());
		getHibernateTemplate().save(n);
		for(DevolucionDeVenta det:d.getPartidas()){
			det.setNota(n);
		}
		return n;
		//return (NotaDeCreditoDevolucion)getHibernateTemplate().merge(n);
		
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Traslado[] generarSalidaPorTraslado(final SolicitudDeTraslado sol,final Date time,String chofer,String user ,String surtidor,String supervisor,String cortador){
		
		
		//Salida
		
		Traslado salida=preparar("TPS", time, sol,user);	
		salida.setChofer(chofer);
		salida.setSucursal(sol.getOrigen());
		salida.setComentario(sol.getComentarioTps());
		salida.setClasificacion(sol.getClasificacion());
		//Folio
		Folio folio=null;
		if (sol.getClasificacion().equals(ClasificacionVale.RECOGE_CLIENTE.toString()) || sol.getClasificacion().equals(ClasificacionVale.ENVIA_SUCURSAL.toString()) ){
			salida.setComentario("AUT-SOL: "+sol.getDocumento()+"-"+sol.getSucursal().getNombre());
			 folio=folioDao.buscarNextFolio(sol.getSucursal(), "TRASLADOS_SOL");
			
		}else{
			folio=folioDao.buscarNextFolio(salida.getSucursal(), "TRASLADOS");	
		}
		
		salida.setDocumento(folio.getFolio());
		salida.setPorInventario(sol.getPorInventario());
		salida.setSurtidor(surtidor);
		salida.setCortador(cortador);
		salida.setSuperviso(supervisor);
		
		for(SolicitudDeTrasladoDet det:sol.getPartidas()){
			if(det.getRecibido()<=0)
				continue;
			double cantidad=Math.abs(det.getRecibido());
			//Generacion de TPS
			TrasladoDet tps=new TrasladoDet();
			tps.setTipo(salida.getTipo());
			tps.setSucursal(salida.getSucursal());
			tps.setProducto(det.getProducto());
			tps.setFecha(salida.getFecha());
			tps.setDocumento(salida.getDocumento());
			tps.setCantidad(cantidad*-1);
			tps.setComentario(salida.getComentario());
			tps.setSolicitado(det.getSolicitado());
			tps.setCortes(det.getCortes());
			tps.setInstruccionesDecorte(det.getInstruccionesDecorte());
			salida.agregarPartida(tps);
			if(!salida.getSolicitud().getClasificacion().equals("ENVIA_SUCURSAL") && !salida.getSolicitud().getClasificacion().equals("RECOGE_CLIENTE")){
				System.out.println("Actualizando Existencia Para TPS Clasificacion Vale: "+salida.getSolicitud().getClasificacion());
				actualizarExistencia(tps);
			}
				
			
		}
		
		Traslado entrada=preparar("TPE", time, sol,user);
		entrada.setSucursal(sol.getSucursal());
		entrada.setDocumento(sol.getDocumento());
		entrada.setComentario(sol.getComentarioTps());
		
	if (sol.getClasificacion().equals(ClasificacionVale.RECOGE_CLIENTE.toString()) || sol.getClasificacion().equals(ClasificacionVale.ENVIA_SUCURSAL.toString()) ){
		entrada.setComentario("AUT-SOL: "+sol.getDocumento()+"-"+sol.getSucursal().getNombre());
		}
		
		entrada.setPorInventario(sol.getPorInventario());
		entrada.setClasificacion(sol.getClasificacion());
		entrada.setChofer(chofer);
		//Comentario para el documento TPS que se atiende y se utiliza en el reporte de traslado TPE
		entrada.setComentarioComision(salida.getDocumento().toString());
		entrada.setSurtidor(surtidor);
		entrada.setCortador(cortador);
		entrada.setSuperviso(supervisor);
		//Entrada
		for(TrasladoDet tps:salida.getPartidas()){
			double cantidad=Math.abs(tps.getCantidad());
			//Generacion de TPE
			TrasladoDet tpe=new TrasladoDet();
			tpe.setCantidad(cantidad);
			tpe.setTipo(entrada.getTipo());
			tpe.setSucursal(entrada.getSucursal());
			tpe.setFecha(entrada.getFecha());
			tpe.setDocumento(entrada.getDocumento());
			tpe.setComentario(entrada.getComentario());
			tpe.setProducto(tps.getProducto());
			tpe.setSolicitado(tps.getSolicitado());
			tpe.setCortes(tps.getCortes());
			tpe.setInstruccionesDecorte(tps.getInstruccionesDecorte());
			entrada.agregarPartida(tpe);
			if(entrada.getSolicitud().getClasificacion().equals("ENVIA_SUCURSAL") || entrada.getSolicitud().getClasificacion().equals("RECOGE_CLIENTE")){
				System.out.println("Actualizando Existencia Para TPE  Clasificacion Vale: "+ entrada.getSolicitud().getClasificacion());
				actualizarExistencia(tpe);
			}
			   
		}
		folioDao.save(folio);
		sol.setAtendido(salida.getDocumento());
		salida=(Traslado)hibernateTemplate.merge(salida);
		entrada=(Traslado)hibernateTemplate.merge(entrada);
		Traslado res[]={salida,entrada};
		return res;
	}
	
	private Traslado preparar(String tipo,Date time,final SolicitudDeTraslado sol,String user){
		Traslado t=new Traslado();
		t.setTipo(tipo);
		t.setFecha(time);
		t.setSolicitud(sol);
		
		//Bitacoras
		t.getLog().setModificado(time);
		t.getLog().setCreado(time);
		//String user=KernellSecurity.instance().getCurrentUserName();
		t.getLog().setCreateUser(user);
		t.getLog().setUpdateUser(user);
		t.getAddresLog().setUpdatedIp(KernellSecurity.getIPAdress());
		t.getAddresLog().setUpdatedMac(KernellSecurity.getMacAdress());
		t.getAddresLog().setCreatedIp(t.getAddresLog().getUpdatedIp());
		t.getAddresLog().setUpdatedMac(t.getAddresLog().getUpdatedMac());
		return t;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Traslado cancelar(Traslado t) {
		t.getPartidas().clear();
		t.setSolicitud(null);
		t.setComentario("CANCELADO");
		hibernateTemplate.update(t);
		return t;
	}
	


	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	public DevolucionDeCompra registrarDevolucionDeCompra(DevolucionDeCompra dec) {
		Date time=obtenerFechaDelSistema();
		
		//Bitacoras
		dec.getLog().setCreado(time);
		dec.getLog().setModificado(time);
		String user=KernellSecurity.instance().getCurrentUserName();
		dec.getLog().setCreateUser(user);
		dec.getLog().setUpdateUser(user);
		
		dec.getAddresLog().setUpdatedIp(KernellSecurity.getIPAdress());
		dec.getAddresLog().setUpdatedMac(KernellSecurity.getMacAdress());
		dec.getAddresLog().setCreatedIp(KernellSecurity.getIPAdress());
		dec.getAddresLog().setCreatedMac(KernellSecurity.getMacAdress());
		
		return salvarDevolucionDeCompra(dec);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	private DevolucionDeCompra salvarDevolucionDeCompra(final DevolucionDeCompra dec){
		Folio folio=folioDao.buscarNextFolio(dec.getSucursal(), "DEC");
		dec.setFecha(new Date());
		dec.setDocumento(folio.getFolio());
		for(DevolucionDeCompraDet det:dec.getPartidas()){
			double cantidad=Math.abs(det.getCantidad());
			det.setCantidad(cantidad*-1);
			det.setDocumento(dec.getDocumento());
			det.setFecha(dec.getFecha());
			det.setSucursal(dec.getSucursal());
			det.setCreateUser(dec.getLog().getCreateUser());
			det.setCreado(dec.getLog().getCreado());
			det.setModificado(dec.getLog().getModificado());
			actualizarExistencia(det);
		}
		DevolucionDeCompra res=(DevolucionDeCompra)this.hibernateTemplate.merge(dec);
		folioDao.save(folio);
		return res;
	}
	
	
	public DevolucionDeCompra cancelarDevolucionDeCompra(DevolucionDeCompra dec) {
		Date time=obtenerFechaDelSistema();
		String user=KernellSecurity.instance().getCurrentUserName();
		return salvarCancelacionDeDevolucionDeCompra(dec,time,user);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	private DevolucionDeCompra salvarCancelacionDeDevolucionDeCompra( DevolucionDeCompra dec,Date time,String user){
		dec=(DevolucionDeCompra)hibernateTemplate.get(DevolucionDeCompra.class, dec.getId());
		//Bitacoras
		dec.getLog().setModificado(time);
		dec.getLog().setUpdateUser(user);
		dec.getAddresLog().setUpdatedIp(KernellSecurity.getIPAdress());
		dec.getAddresLog().setUpdatedMac(KernellSecurity.getMacAdress());
		
		dec.setComentario("CANCELADO");
		for(DevolucionDeCompraDet det:dec.getPartidas()){
			det.setCantidad(0);
			actualizarExistencia(det);
		}
		dec.getPartidas().clear();
		
		DevolucionDeCompra res=(DevolucionDeCompra)this.hibernateTemplate.merge(dec);
		return res;
	}
	
	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	public Conteo registrarConteo(final Conteo conteo){
		Date time=obtenerFechaDelSistema();
		String user=KernellSecurity.instance().getCurrentUserName();
		return salvarConteoDeInventario(conteo, time,user);
	}
	
	
	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	public Sector registrarSector(final Sector sector){
		Date time=obtenerFechaDelSistema();
		String user=KernellSecurity.instance().getCurrentUserName();
		return salvarSector(sector, time,user);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	private Conteo salvarConteoDeInventario(Conteo conteo,final Date time,String user){
		
		//Bitacoras
		if(conteo.getId()==null){
			conteo.getLog().setCreado(time);
			conteo.getLog().setCreateUser(user);
			conteo.getAddresLog().setCreatedIp(KernellSecurity.getIPAdress());
			conteo.getAddresLog().setCreatedMac(KernellSecurity.getMacAdress());
		}
		
		conteo.getLog().setModificado(time);
		conteo.getLog().setUpdateUser(user);
		conteo.getAddresLog().setUpdatedIp(KernellSecurity.getIPAdress());
		conteo.getAddresLog().setUpdatedMac(KernellSecurity.getMacAdress());
		
		
		if(conteo.getId()==null){
			Folio folio=folioDao.buscarNextFolio(conteo.getSucursal(), "CONTEO_INV");
			conteo.setDocumento(folio.getFolio());			
			folioDao.save(folio);
		}
		
		for(ConteoDet det:conteo.getPartidas()){
			det.setDocumento(conteo.getDocumento());
		}
		
		Conteo res=(Conteo)this.hibernateTemplate.merge(conteo);
		return res;
		
	}
	
private Sector salvarSector(Sector sector,final Date time,String user){
		
		//Bitacoras
		if(sector.getId()==null){
			sector.getLog().setCreado(time);
			sector.getLog().setCreateUser(user);
			sector.getAddresLog().setCreatedIp(KernellSecurity.getIPAdress());
			sector.getAddresLog().setCreatedMac(KernellSecurity.getMacAdress());
		}
		
		sector.getLog().setModificado(time);
		sector.getLog().setUpdateUser(user);
		sector.getAddresLog().setUpdatedIp(KernellSecurity.getIPAdress());
		sector.getAddresLog().setUpdatedMac(KernellSecurity.getMacAdress());
		
		
		if(sector.getId()==null){
			/*Folio folio=folioDao.buscarNextFolio(conteo.getSucursal(), "CONTEO_INV");
			conteo.setDocumento(folio.getFolio());			
			folioDao.save(folio);*/
		}
		
		for(SectorDet det:sector.getPartidas()){
		//	det.setDocumento(sector.getDocumento());
		}
		
		Sector res=(Sector)this.hibernateTemplate.merge(sector);
		return res;
		
		
		
	}
	


@Transactional(propagation=Propagation.SUPPORTS)
public void generarExistenciasParaConteoFisico(final Sucursal sucursal,final Date fecha,final String user){
	List<Existencia> exis=buscarExistencias(sucursal);
	for(Existencia e:exis){
		ExistenciaConteo ec=new ExistenciaConteo();
		ec.setFecha(fecha);
		ec.setId(e.getId());
		ec.setSucursal(e.getSucursal());
		ec.setProducto(e.getProducto());
		ec.setExistencia(e.getCantidad());
		
		ec.getLog().setCreado(fecha);
		ec.getLog().setModificado(fecha);
		ec.getLog().setCreateUser(user);
		ec.getLog().setUpdateUser(user);
		
		ec=(ExistenciaConteo)hibernateTemplate.merge(ec);
		//System.out.println(ec);
	}
}



	@Transactional(propagation=Propagation.SUPPORTS)
	public void generarExistenciasParaConteo(final Long sucursalId,final Date fecha,final String user){
		List<Existencia> exis=existenciaDao.buscarExistencias(sucursalId, fecha);
		for(Existencia e:exis){
			ExistenciaConteo ec=new ExistenciaConteo();
			ec.setFecha(fecha);
			ec.setId(e.getId());
			ec.setSucursal(e.getSucursal());
			ec.setProducto(e.getProducto());
			ec.setExistencia(e.getCantidad());
			
			ec.getLog().setCreado(fecha);
			ec.getLog().setModificado(fecha);
			ec.getLog().setCreateUser(user);
			ec.getLog().setUpdateUser(user);
			
			ec=(ExistenciaConteo)hibernateTemplate.merge(ec);
			//System.out.println(ec);
		}
	}
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public void generarExistenciasParaConteo(List<Existencia> exis,final boolean parcial,Date fecha,String user){
		for(Existencia e:exis){
			try {
				
				List<ExistenciaConteo> found=hibernateTemplate
						.find("from ExistenciaConteo e where e.existenciaOrigen=? and date(e.log.creado)=?",new Object[]{e.getId(),new Date()});
				if(found.isEmpty()){
					ExistenciaConteo ec=new ExistenciaConteo();
					ec.setFecha(fecha);
					ec.setExistenciaOrigen(e.getId());
					ec.setSucursal(e.getSucursal());
					ec.setProducto(e.getProducto());
					ec.setExistencia(e.getCantidad());
					ec.setConteoParcial(parcial);
					ec.getLog().setCreado(fecha);
					ec.getLog().setModificado(fecha);
					ec.getLog().setCreateUser(user);
					ec.getLog().setUpdateUser(user);
					hibernateTemplate.save(ec);
				}
				
			} catch (Exception e2) {
				System.out.println("Error persistiendo :"+exis+ "  Msg:"+ExceptionUtils.getRootCauseMessage(e2));
			}
						
		}
		if(parcial){
			//Generar conteo y conteoDet
			Integer current=(Integer)hibernateTemplate.find("select max(c.sector) from Conteo c where c.fecha=?", new Object[]{fecha}).get(0);
			if(current==null)
				current=0;
			int sector=current+1;
			Conteo conteo=new Conteo();
			conteo.setFecha(fecha);
			conteo.setSector(sector);
			conteo.setSucursal(exis.get(0).getSucursal());
			
			for(Existencia e:exis){
				ConteoDet det=new ConteoDet();
				det.setCantidad(0);
				det.setProducto(e.getProducto());
				conteo.agregarPartida(det);
			}
			registrarConteo(conteo);
		}
	}
	
	private DateFormat df=new SimpleDateFormat("dd/MM/yyyy");

	@Transactional(propagation=Propagation.REQUIRED)
	public void generarAjusteDeInventario(Sucursal sucursal, Date fecha) {
		
		String hql="from ExistenciaConteo e where e.sucursal.id=? and date(e.log.creado)=? and fijado is not null and e.ajuste!=0";
		List<ExistenciaConteo> exis=getHibernateTemplate().find(hql,new Object[]{sucursal.getId(),fecha});
		for(ExistenciaConteo ex:exis){
			if(ex.getConteoParcial()){
				System.out.println("Conteo parcial no genero ajuste de inventario");
				return ;
			}
		}
		if(exis.isEmpty())
			return;
		Movimiento mov=new Movimiento();
		mov.setSucursal(sucursal);
		
		
		mov.setConcepto(Concepto.AJU);
		Date f=null;
		mov.setFecha(fecha);
		
		for(ExistenciaConteo ex:exis){
			
			if(ex.getAjuste()==0.0d)
				continue;
			if(f==null){
				f=ex.getFecha();
				mov.setFecha(f);
				String msg=MessageFormat.format("Inventario Físico:{0}", df.format(f));
				mov.setComentario(msg);
				
			}
			MovimientoDet det=new MovimientoDet();
			det.setProducto(ex.getProducto());
			det.setCantidad(ex.getAjuste());
			det.setComentario(mov.getComentario());
			det.setFecha(f);
			mov.agregarPartida(det);
			
		}
		salvarMovimiento(mov);
	}

	
	@Transactional(propagation=Propagation.REQUIRED)
	public PreDevolucion salvarPreDevolucion(PreDevolucion preDevo) {
		String tipo="PREDEV_FAC";
		Folio folio=folioDao.buscarNextFolio(preDevo.getSucursal(), tipo);
		preDevo.setDocumento(folio.getFolio());
		CantidadMonetaria importe=CantidadMonetaria.pesos(0);
		for(PreDevolucionDet det:preDevo.getPartidas()){
			CantidadMonetaria precio=CantidadMonetaria.pesos(det.getVentaDet().getPrecio());
			double cantidad=det.getCantidadEnUnidad();
			double descuento=det.getVentaDet().getDescuento();
			
			CantidadMonetaria imp=precio.multiply(cantidad);
			CantidadMonetaria impDescuento=imp.multiply(Math.abs(descuento/100));
			imp=imp.subtract(impDescuento);
			
			int cortes=det.getCortes();
			CantidadMonetaria pcortes=CantidadMonetaria.pesos(det.getVentaDet().getPrecioCorte());
			CantidadMonetaria importeCortes=pcortes.multiply((double)cortes);
			imp=imp.add(importeCortes);
			det.setCortes(cortes);
			
			
			importe=importe.add(imp);
			//det.setDocumento(preDevo.getNumero());
		}
		preDevo.setImporte(importe.amount());
		preDevo.setImpuesto(MonedasUtils.calcularImpuesto(importe.amount()));		
		preDevo.setTotal(MonedasUtils.calcularTotal(importe.amount()));
		
		PreDevolucion res=(PreDevolucion)getHibernateTemplate().merge(preDevo);
		folioDao.save(folio);		
		return res;
	}
	
	public RecepcionDeMaquila getRecepcion(String id){
		return this.recepcionDeMaquilaDao.get(id);
	}
	
	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	public RecepcionDeMaquila salvarRecepcion(final RecepcionDeMaquila recepcion){
		Date time=obtenerFechaDelSistema();
		
		//Bitacoras
		String user=KernellSecurity.instance().getCurrentUserName();
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();
		if(recepcion.getId()==null){
			recepcion.getLog().setCreateUser(user);
			recepcion.getLog().setCreado(time);
			recepcion.getAddresLog().setCreatedIp(ip);
			recepcion.getAddresLog().setCreatedMac(mac);
		}
		recepcion.getLog().setModificado(time);		
		recepcion.getLog().setUpdateUser(user);
		recepcion.getAddresLog().setUpdatedIp(ip);
		recepcion.getAddresLog().setUpdatedMac(mac);
		
		return doSalvarRecepcion(recepcion, time);
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public RecepcionDeMaquila doSalvarRecepcion(final RecepcionDeMaquila recepcion,Date time){
		if(recepcion.getId()==null){
			Folio folio=folioDao.buscarNextFolio(recepcion.getSucursal(), "MAQ");
			recepcion.setDocumento(folio.getFolio());
			for(EntradaDeMaquila det:recepcion.getPartidas()){
				det.setDocumento(folio.getFolio());
				det.setRemision(recepcion.getRemision());
				det.setCreado(time);
				det.setModificado(time);
				det.setEspecial(det.getProducto().isMedidaEspecial());
				Existencia exis=existenciaDao.buscar(det.getProducto().getClave(), det.getSucursal().getId(), det.getYear(), det.getMes());
				if(exis==null){
					//exis=existenciaDao.generar(det.getProducto(), det.getFecha(),det.getSucursal().getId());
					exis=new Existencia();
					exis.setSucursal(det.getSucursal());
					exis.setProducto(det.getProducto());
					exis.setCreateUser("ADMIN");
					exis.setFecha(det.getFecha());
					exis.setYear(Periodo.obtenerYear(det.getFecha()));
					exis.setMes(Periodo.obtenerMes(det.getFecha())+1);
					
				}
				exis.setCantidad(exis.getCantidad()+det.getCantidad());
				existenciaDao.save(exis);
			}
			folioDao.save(folio);
		}else{			
			for(EntradaDeMaquila det:recepcion.getPartidas()){
				det.setModificado(time);
				//La modificacion requiere de un Trigger para actualizar la existencia
			}
		}
		return this.recepcionDeMaquilaDao.save(recepcion);
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public void eliminarRecepcion(final RecepcionDeMaquila recepcion){
		for(EntradaDeMaquila e:recepcion.getPartidas()){
			e.setCantidad(0);
			e.setKilos(0);
			e.setComentario("CANCELADO");
			actualizarExistencia(e);
		}
		recepcion.setComentario("CANCELADO");
		this.recepcionDeMaquilaDao.save(recepcion);
	}

	private synchronized Date obtenerFechaDelSistema(){
		return (Date)jdbcTemplate.queryForObject("select now()", Date.class);
	}

	public Existencia buscarExistencia(final Sucursal suc,Producto producto,int year,int mes){
		return existenciaDao.buscar(producto.getClave(),suc.getId(), year, mes);
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public MovimientoDao getMovimientoDao() {
		return movimientoDao;
	}
	
	public void setMovimientoDao(MovimientoDao movimientoDao) {
		this.movimientoDao = movimientoDao;
	}
	
	public static void main(String[] args) {
		/*
		Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
		Services.getInstance().getInventariosManager()
		//.generarExistenciasParaConteo(5L, new Date());
		.generarAjusteDeInventario(suc, new Date());
		*/
		Services.getInstance().getHibernateTemplate()
			.iterate("select sum(i.cantidad) from ", 5079L).next();
	}
	
}
