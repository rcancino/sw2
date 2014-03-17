package com.luxsoft.sw3.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Destination;

import org.apache.activemq.command.ActiveMQTopic;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.compras.dao.Compra2Dao;
import com.luxsoft.siipap.compras.dao.ListaDePreciosDao;
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.compras.model.RecepcionDeCompra;
import com.luxsoft.siipap.dao.core.FolioDao;
import com.luxsoft.siipap.inventarios.dao.ExistenciaDao;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Folio;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.sw3.replica.EntityLog;

/**
 * Implementacion del service layer para compras2
 * 
 * @author Ruben Cancino Ramos
 * 
 */
@Service("comprasManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class ComprasManagerImpl implements ComprasManager{
	
	@Autowired
	private Compra2Dao compraDao;
	
	@Autowired
	private ListaDePreciosDao listaDePreciosDao;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private ExistenciaDao existenciaDao;
	
	@Autowired
	private FolioDao folioDao;
	
	private Logger logger=Logger.getLogger(getClass());
	
		
	public List<Compra2> buscarCompras(Periodo p) {
		return compraDao.buscarCompras(p);
	}

	public Compra2 buscarInicializada(String id) {
		return compraDao.inicializarCompra(id);
	}

	public List<CompraUnitaria> buscarPartidas(final Compra2 compra) {
		return compraDao.buscarPartidas(compra);
	}

	public List<CompraUnitaria> buscarPendientesPorProveedor(Proveedor p) {
		return compraDao.buscarComprasPendientesPorProveedor(p);
	}

	public Compra2 buscarPorFolio(int sucursal, int folio) {
		return compraDao.buscarPorFolio(sucursal, folio);
	}

	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public Compra2 save(Compra2 object) {
		if(object.getId()==null){
			for(CompraUnitaria det:object.getPartidas()){
				if(det.getSucursal()==null)
					det.setSucursal(object.getSucursal());
			}
			object.setEntrega(object.estimarFechaDeEntrega());
			String tipo="COMPRAS";
			Folio folio=folioDao.buscarNextFolio(object.getSucursal(), tipo);
			object.setFolio(folio.getFolio());
			folioDao.save(folio);
		}
		object.setReplicado(null);
		object.setImportado(null);
		
		String user=KernellSecurity.instance().getCurrentUserName();
		Date time=new Date();
		if(object.getLog()==null)
			object.setLog(new UserLog());
		object.getLog().setModificado(time);
		object.getLog().setUpdateUser(user);
		if(object.getId()==null){
			object.getLog().setCreado(time);
			object.getLog().setCreateUser(user);
		}
		object.actualizar();
		this.hibernateTemplate.saveOrUpdate(object);
		Compra2 res=get(object.getId());
		//enviar(res);
		return res;
		//return compraDao.save(object);
	}
	
	private JmsTemplate jmsTemplate;
	/*
	private void enviar(Compra2 compra){
		Set<String> sucursales=new HashSet<String>();
		try {
			for(CompraUnitaria cu:compra.getPartidas()){
				sucursales.add(cu.getSucursal().getNombre());
			}
			EntityLog entity=new EntityLog(compra,compra.getId(),compra.getSucursal().getNombre(),EntityLog.Tipo.CAMBIO);
			for(String suc:sucursales){
				//Destination destino=new ActiveMQTopic();
				String destino="REPLICA.QUEUE."+suc;
				jmsTemplate.convertAndSend(destino, entity);
				logger.info("Enviando compra a: "+destino);
				System.out.println(" Enviando compra a :"+destino);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	*/
	@Transactional(propagation=Propagation.REQUIRED)
	public Compra2 saveCentralizada(Compra2 object) {
		if(object.getId()==null){
			for(CompraUnitaria det:object.getPartidas()){
				if(det.getSucursal()==null)
					det.setSucursal(object.getSucursal());
			}
			object.setEntrega(object.estimarFechaDeEntrega());
			String tipo="COMPRAS";
			Folio folio=folioDao.buscarNextFolio(object.getSucursal(), tipo);
			object.setFolio(folio.getFolio());
			folioDao.save(folio);
		}
		object.setReplicado(null);
		//object.setImportado(null);
		
		String user=KernellSecurity.instance().getCurrentUserName();
		Date time=new Date();
		if(object.getLog()==null)
			object.setLog(new UserLog());
		object.getLog().setModificado(time);
		object.getLog().setUpdateUser(user);
		if(object.getId()==null){
			object.getLog().setCreado(time);
			object.getLog().setCreateUser(user);
		}
		object.actualizar();
		
		//Compra2 res= compraDao.save(object);
		this.hibernateTemplate.saveOrUpdate(object);
		Compra2 res=get(object.getId());
		//enviar(get(res.getId()));
		return res;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Compra2 cancelar(Compra2 compra,boolean centralizada){
		compra=get(compra.getId());
		Assert.isNull(compra.getDepuracion(),"La compra esta depurada no se puede cancelar");
		//Assert.isNull(compra.getCierre(),"La compra se ha cerrado no se puede cancelar");
		for(CompraUnitaria uni:compra.getPartidas()){
			Assert.isTrue(uni.getRecibido()==0,"La compra ya tiene recepciones parciales totales o parciales no se puede cancelar : Partida: "+uni.toString());
		}
		compra.getPartidas().clear();
		compra.actualizar();
		compra.setComentario("COMPRA CANCELADA Tot Orig: "+compra.getTotalCM());
		if(centralizada)
			return saveCentralizada(compra);
		else
			return save(compra);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Compra2 depurar(final String id,final Date fecha){
		Compra2 source=get(id);
		source.setDepuracion(fecha);
		for(CompraUnitaria uni:source.getPartidas()){
			if(!uni.isDepurada()){
				uni.setDepuracion(fecha);
				uni.setDepurado(uni.getSolicitado()-uni.getRecibido());				
			}
		}
		if(source.getCierre()==null)
			source.setCierre(fecha);
		source.actualizar();
		return save(source);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Compra2 cerrar(Compra2 compra,final Date fecha){		
		if(compra.getCierre()!=null)
			return compra;
		Compra2 source=get(compra.getId());
		source.setCierre(fecha);
		return save(source);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Compra2 cancelarCierre(final Compra2 compra){
		if(compra.getCierre()==null)
			return compra;
		Compra2 source=get(compra.getId());
		source.setCierre(null);
		return save(source);
	}
	
	public Compra2 obtenerCopiaModificable(final String compraId){
		Compra2 target=buscarInicializada(compraId);
		for(CompraUnitaria uni:target.getPartidas()){
			if(uni.getRecibido()>0){
				throw new IllegalStateException("La compra ya tiene recepciones totales o parciales. Partida: "+uni);
			}
		}
		return target;
	}

	public boolean exists(String id) {
		return compraDao.exists(id);
	}

	public Compra2 get(String id) {
		return compraDao.get(id);
	}

	public List<Compra2> getAll() {
		throw new UnsupportedOperationException("No se deben requerir todas las compras");
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void remove(String id) {
		compraDao.remove(id);
	}
	
	/*public long buscarFolio(Compra2 compra) {		
		String hql="select max(v.folio) from Compra2 v " +
				"where v.sucursal.id=?";
		List<Long> res=hibernateTemplate.find(hql, compra.getSucursal().getId());
		return res.get(0)!=null?Math.abs(res.get(0).longValue())+1:1;
		
	}*/
	
	/*public long buscarFolio(RecepcionDeCompra recepcion){
		String hql="select max(v.documento) from RecepcionDeCompra v " +
		"where v.sucursal.id=?";
List<Long> res=hibernateTemplate.find(hql, recepcion.getSucursal().getId());
return res.get(0)!=null?Math.abs(res.get(0).longValue())+1:1;
	}*/

	public List<Compra2> buscarPendientes() {
		return compraDao.buscarPendientes();
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void actualizarPrecios(Compra2 compra){
		compra=obtenerCopiaModificable(compra.getId());
		for(CompraUnitaria cu:compra.getPartidas()){
			asignarPrecioDescuento(cu);
		}
		compra.actualizar();
		save(compra);
	}

	@Transactional(propagation=Propagation.SUPPORTS)
	public void asignarPrecioDescuento(CompraUnitaria det){
		Assert.notNull(det.getCompra(),"La compra no ha sido asignada");
		if(det.getCompra().isEspecial()){
			
		}
		Producto prod=det.getProducto();
		Currency mon=det.getMoneda();
		Proveedor prov=det.getCompra().getProveedor();
		Date fecha=det.getCompra().getFecha();
		ListaDePreciosDet lp=getListaDePrecios()
				.buscarPrecioVigente(prod
				,mon
				,prov
				,fecha);
		if(lp!=null){
			det.setPrecio(lp.getPrecio().amount());
			det.setDesc1(lp.getDescuento1());
			det.setDesc2(lp.getDescuento2());
			det.setDesc3(lp.getDescuento3());
			det.setDesc4(lp.getDescuento4());	
			det.setDesc5(lp.getDescuento5());
			det.setDesc6(lp.getDescuento6());
			det.setDescuentof(lp.getDescuentoFinanciero());
			det.actualizar();
		}
	}
	
	
	@Transactional(propagation=Propagation.REQUIRED,isolation=Isolation.READ_COMMITTED)
	public RecepcionDeCompra registrarRecepcion(final RecepcionDeCompra recepcion){
		String tipo="ENT_COM";
		Folio folio=folioDao.buscarNextFolio(recepcion.getSucursal(), tipo);
		recepcion.setDocumento(folio.getFolio());
		int renglon=1;
		for(EntradaPorCompra entrada:recepcion.getPartidas() ){
			Compra2 compra=recepcion.getCompra();
			entrada.setProveedor(compra.getProveedor());
			entrada.setCompra(compra.getFolio().intValue());
			entrada.setDocumento(recepcion.getDocumento());
			entrada.setFecha(new Date());
			//entrada.setFechaRemision(fechaRemision)
			entrada.setSucursal(recepcion.getSucursal());
			entrada.setSucursalCompra(compra.getSucursal().getClave());
			entrada.setRemision(recepcion.getRemision());
			entrada.setFechaCompra(compra.getFecha());
			entrada.setCosto(entrada.getCompraDet().getCosto());
			//entrada.setRenglon(renglon++);
		}
		folioDao.save(folio);
		RecepcionDeCompra target=(RecepcionDeCompra)hibernateTemplate.merge(recepcion);
		actualizarExistencias(target);
		return target;
	}
	
	@Transactional(propagation=Propagation.MANDATORY,isolation=Isolation.READ_COMMITTED)
	private void actualizarExistencias(RecepcionDeCompra recepcion){
		final Date hoy=recepcion.getFecha();
		final Long sucursal=recepcion.getSucursal().getId();
		final int year=Periodo.obtenerYear(hoy);
		final int mes=Periodo.obtenerMes(hoy)+1;
		for(EntradaPorCompra det:recepcion.getPartidas()){
			Existencia exis=existenciaDao.buscar(det.getClave(), sucursal, year, mes);			
			if(exis==null){
				exis=existenciaDao.generar(det.getProducto(), hoy, det.getSucursal().getId());
			}
			exis.setCantidad(exis.getCantidad()+det.getCantidad());
			if(det.getNacional()==true){
				exis.setPedidosPendientes(exis.getPedidosPendientes()-det.getCantidad());
			}
			exis=existenciaDao.save(exis);
			if(logger.isDebugEnabled()){
				logger.debug("Existencia actualizada: "+exis);
			}
		}
	}
	
	@Transactional(propagation=Propagation.MANDATORY)
	private void registrarPendientes(Compra2 compra){
		final Date hoy=compra.getFecha();
		final Long sucursal=compra.getSucursal().getId();
		final int year=Periodo.obtenerYear(hoy);
		final int mes=Periodo.obtenerMes(hoy)+1;
		for(CompraUnitaria det:compra.getPartidas()){
			Existencia exis=existenciaDao.buscar(det.getClave(), sucursal, year, mes);			
			if(exis==null){
				exis=existenciaDao.generar(det.getProducto(), hoy, det.getSucursal().getId());
			}
			exis.setApartados(exis.getApartados()+det.getSolicitado());
			exis=existenciaDao.save(exis);
			if(logger.isDebugEnabled()){
				logger.debug("Existencia actualizada: "+exis);
			}
		}
	}
	
	
	
	@Transactional(propagation=Propagation.REQUIRED)
	public RecepcionDeCompra getRecepcion(String id) {
		RecepcionDeCompra com=(RecepcionDeCompra)hibernateTemplate.load(RecepcionDeCompra.class, id);
		hibernateTemplate.initialize(com.getPartidas());
		return com;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Compra2 consolidarCompras(List<String> ids) {
		Assert.notNull(ids);
		Assert.notEmpty(ids,"La seleccion no es valida: "+ids.size());
		Proveedor proveedor=null;
		List<Compra2> compras=new ArrayList<Compra2>();
		List<CompraUnitaria> partidas=new ArrayList<CompraUnitaria>();
		//Validacion y consistencia
		for(String  id:ids){
			Compra2 c=buscarInicializada(id);
			Assert.isNull(c.getCierre(),"La compar: "+c.getFolio()+ "de la sucursal: "+c.getSucursal()+ " esta cerrada, no se puede consolidar");
			Assert.isNull(c.getDepuracion(),"La compar: "+c.getFolio()+ "de la sucursal: "+c.getSucursal()+ " esta depurada, no se puede consolidar");
			
			if(proveedor==null)
				proveedor=c.getProveedor();
			else{
				Assert.isTrue(proveedor.equals(c.getProveedor()),"Las compras no son del mismo proveedor");
			}
			compras.add(c);
		}
		
		for(Compra2 c:compras){
			logger.info("Partidas a consolidar de la compra: "+c.getFolio()+"   " + c.getPartidas().size());
			List<CompraUnitaria> partidasList=new ArrayList<CompraUnitaria>(c.getPartidas());
			for(CompraUnitaria det:partidasList){
				double recibido=det.getRecibido();
				Assert.isTrue(recibido==0,"La compar: "+c.getFolio()+ "de la sucursal: "+c.getSucursal()+ " esta parcialmente recibida, no se puede consolidar");
				det.setCompra(null);
				det.setFolioOrigen(c.getFolio());
				det.setComentario("CONSOLIDADA");
				partidas.add(det);
			}
			c.eliminarPartidas();
			c.setReplicado(null);
			c.setImportado(null);
			
		}
		
		
		Sucursal sucursal=(Sucursal)hibernateTemplate.get(Sucursal.class, 1L);
		Compra2 compra=new Compra2();
		compra.setSucursal(sucursal);
		compra.setProveedor(proveedor);
		compra.setConsolidada(true);
		compra.setComentario("COMPRA CONSOLIDADA ");
		compra.setFecha(new Date());
		compra.setEntrega(compra.estimarFechaDeEntrega());
		String tipo="COMPRAS_CONSOLIDADA";
		Folio folio=folioDao.buscarNextFolio(sucursal, tipo);
		compra.setFolio(folio.getFolio());
		folioDao.save(folio);
		
		for(CompraUnitaria det:partidas){
			compra.agregarPartida(det);
		}
		compra.actualizar();
		
		for(Compra2 c:compras){
			c.setReplicado(null);
			c.setImportado(null);
			c.setComentario("CONSOLIDADA EN LA: "+compra.getFolio());
		}
		
		return save(compra);
	}
	
	

	public ListaDePreciosDao getListaDePrecios() {
		return listaDePreciosDao;
	}
	
	public ListaDePreciosDao getListaDePreciosDao() {
		return listaDePreciosDao;
	}

	public void setListaDePreciosDao(ListaDePreciosDao listaDePreciosDao) {
		this.listaDePreciosDao = listaDePreciosDao;
	}
	
	public Compra2Dao getCompraDao() {
		return compraDao;
	}

	public void setCompraDao(Compra2Dao compraDao) {
		this.compraDao = compraDao;
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public ExistenciaDao getExistenciaDao() {
		return existenciaDao;
	}

	public void setExistenciaDao(ExistenciaDao existenciaDao) {
		this.existenciaDao = existenciaDao;
	}

	public FolioDao getFolioDao() {
		return folioDao;
	}

	public void setFolioDao(FolioDao folioDao) {
		this.folioDao = folioDao;
	}
	
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}
	

}
