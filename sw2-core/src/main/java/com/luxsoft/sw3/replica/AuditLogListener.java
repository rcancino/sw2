package com.luxsoft.sw3.replica;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.AplicacionDeNota;
import com.luxsoft.siipap.cxc.model.AplicacionDePago;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.CargoPorTesoreria;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.ventas.model.AsignacionVentaCE;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.embarque.EntregaDet;
import com.luxsoft.sw3.solicitudes.SolicitudDeModificacion;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;
import com.luxsoft.utils.LoggerHelper;


/**
 * HibernateListener para generar registros de AuditLog en ciertas entidades
 * Tiene la misma finalidad que {@link AuditLogInterceptor} pero mediante Listeners de  hibernate
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("rawtypes")
public class AuditLogListener implements PostInsertEventListener
						,PostUpdateEventListener
						,PostDeleteEventListener,InitializingBean{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2569675975784239559L;
	
	private HibernateTemplate hibernateTemplate;
	
	private Set entidades=new HashSet();
	private Set catalogos=new HashSet();
	private Set<String> exclusiones=new HashSet<String>();
	private Set<String> destinos;
	private String sucursalOrigen;
	private String sucursalDestino;
	private  String ip;
	
	private Logger logger=LoggerHelper.getLogger();
	
	public void onPostInsert(PostInsertEvent event) {
		audit(event.getEntity(),event.getId(),"INSERT");
	}
	
	public void onPostDelete(PostDeleteEvent event) {
		audit(event.getEntity(),event.getId(),"DELETE");
	}

	public void onPostUpdate(PostUpdateEvent event) {
		audit(event.getEntity(),event.getId(),"UPDATE");
	}

	private void audit(Object entity,Serializable id,String tipo){
		AuditLog log=new AuditLog(entity,id,tipo,getIPAdress(),getSucursalOrigen(),getSucursalDestino());
		if(getExclusiones().contains(log.getEntityName()))
			return;
		if(getCatalogos().contains(log.getEntityName())){
			for(String destino:getDestinos()){
				AuditLog target=new AuditLog();
				BeanUtils.copyProperties(log, target);
				target.setSucursalDestino(destino);
				audit(target);
			}
		}else if(getEntidades().contains(log.getEntityName())){
			for(String destino:resolverDestino(entity)){
				AuditLog target=new AuditLog();
				BeanUtils.copyProperties(log, target);
				target.setSucursalDestino(destino);
				audit(target);
			}
			if(log.getEntityName().equals("Abono")){
				Abono a=(Abono)entity;
				if(a.getAutorizacion()!=null){
					AuditLog target=new AuditLog();
					BeanUtils.copyProperties(log, target);
					target.setAction("INSERT");
					target.setSucursalDestino(a.getSucursal().getNombre());
					audit(target);
				}
			}
			return;
		}
	}
	
	private void audit(AuditLog log){
		
		try {
			getHibernateTemplate().save(log);
			getHibernateTemplate().flush();
			//AuditLog res=(AuditLog)getHibernateTemplate().merge(log);
			logger.debug("Log registrado:"+log);
		} catch (Exception e) {
			logger.error(" Error registrando AuditLog: "+log+"  Causa:"
					+ ExceptionUtils.getRootCauseMessage(e),ExceptionUtils.getCause(e));
		}
	}
	
	private boolean replicar(Object clazz){
		/*
		for(Class c:clases){
			if (c.isAssignableFrom(clazz))
				return true;
		}
		return false;
		*/
		return true;
	}
	
	private String[] resolverDestino(Object bean){
		//System.out.println("Resolviendo destinos para : "+bean);
		if(Abono.class.isAssignableFrom(bean.getClass()) ){
			Abono abono=(Abono)bean;
			if(abono.getSucursal().getId().equals(new Long(1))){
				return new String[]{"OFICINAS"};
			}
			return new String[]{abono.getSucursal().getNombre()};
		}else if(bean instanceof AplicacionDeNota ){
			Aplicacion a=(Aplicacion)bean;
			if(a.getCargo().getOrigen().equals(OrigenDeOperacion.CAM)){
				String suc=a.getCargo().getSucursal().getNombre();
				return new String[]{suc};
			}else{
				return new String[0];
			}
			
		}else if(bean instanceof AplicacionDePago ){
			Aplicacion a=(Aplicacion)bean;
			if(a.getCargo().getOrigen().equals(OrigenDeOperacion.CAM)){
				String suc=a.getCargo().getSucursal().getNombre();
				return new String[]{suc};
			}else{
				return new String[0];
			}
			
		}else if(bean instanceof Embarque){
			Embarque e=(Embarque)bean;
			return new String[]{e.getSucursal()};
		}else if(bean instanceof Entrega){
			Entrega e=(Entrega)bean;
			return new String[]{e.getEmbarque().getSucursal()};
		}else if(bean instanceof EntregaDet){
			EntregaDet e=(EntregaDet)bean;
			return new String[]{e.getEntrega().getEmbarque().getSucursal()};
		}else if(Cargo.class.isAssignableFrom(bean.getClass()) ){
			Cargo cargo=(Cargo)bean;
			if(cargo.getSucursal().getId().equals(new Long(1))){
				return new String[]{"ND"};
			}
			return new String[]{cargo.getSucursal().getNombre()};
		}else if (bean instanceof SolicitudDeDeposito){
			SolicitudDeDeposito sol=(SolicitudDeDeposito)bean;
			return new String[]{sol.getSucursal().getNombre()};
		}else if(bean instanceof Compra2){
			Compra2 compra=(Compra2)bean;
			if(compra.isImportacion()){
				return getDestinos().toArray(new String[0]);
			}else if(compra.getConsolidada()){
				Set<String> sucs=new HashSet<String>();
				for(CompraUnitaria cu:compra.getPartidas()){
					sucs.add(cu.getSucursal().getNombre());
				}
				return sucs.toArray(new String[0]);
			}
			return new String[]{compra.getSucursal().getNombre()};
		}else if(bean instanceof CompraUnitaria){
			CompraUnitaria com=(CompraUnitaria)bean;
			//System.out.println("Resolviendo destino para compra unitaria: "+com);
			if( (com.getCompra()!=null) && com.getCompra().isImportacion()){
				return getDestinos().toArray(new String[0]);
			}else
				return new String[]{com.getSucursal().getNombre()};
		}else if(bean instanceof NotaDeCredito){
			NotaDeCredito nota=(NotaDeCredito)bean;
			if(nota.getOrigenAplicacion().equals("CAM")){
				Aplicacion a=nota.getAplicaciones().iterator().next();
				String suc=a.getCargo().getSucursal().getNombre();
				return new String[]{suc};
			}else{
				return new String[0];
			}
		}else if(bean instanceof CargoPorTesoreria){
			CargoPorTesoreria cargo=(CargoPorTesoreria)bean;
			if(cargo.getOrigen().equals(OrigenDeOperacion.CAM)){
				return new String[]{cargo.getSucursal().getNombre()};
			}else{
				return new String[0];
			}
		}else if(bean instanceof AsignacionVentaCE){
			AsignacionVentaCE a=(AsignacionVentaCE)bean;
			return new String[]{a.getVenta().getSucursal().getNombre()};
		}else if(bean instanceof SolicitudDeModificacion){
			SolicitudDeModificacion a=(SolicitudDeModificacion)bean;
			return new String[]{a.getSucursal().getNombre()};
		}
		else
			return new String[]{"ND"};
	}
	
	public void afterPropertiesSet() throws Exception {
		Assert.hasLength(sucursalOrigen,"Se requiere registrar la sucursal origen");
		Assert.notNull(hibernateTemplate,"Se requiere hibernateTemplate");
		Assert.notEmpty(destinos);
		try {
			ip=InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {ip="NO DISPONIBLE";}
	}
	
	public Set getEntidades() {
		return entidades;
	}

	public void setEntidades(Set entidades) {
		this.entidades = entidades;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		setHibernateTemplate(new HibernateTemplate(sessionFactory));
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
	public String getIPAdress(){
		return ip;
	}	
	
	public String getSucursalOrigen() {
		return sucursalOrigen;
	}

	public void setSucursalOrigen(String sucursalOrigen) {
		this.sucursalOrigen = sucursalOrigen;
	}

	public String getSucursalDestino() {
		return sucursalDestino;
	}

	public void setSucursalDestino(String sucursalDestino) {
		this.sucursalDestino = sucursalDestino;
	}

	public Set getCatalogos() {
		return catalogos;
	}

	public void setCatalogos(Set catalogos) {
		this.catalogos = catalogos;
	}
	
	public Set<String> getExclusiones() {
		return exclusiones;
	}

	public void setExclusiones(Set<String> exclusiones) {
		this.exclusiones = exclusiones;
	}

	public Set<String> getDestinos() {
		return destinos;
	}

	public void setDestinos(Set<String> destinos) {
		this.destinos = destinos;
	}
	

}
