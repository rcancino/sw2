package com.luxsoft.sw3.replica;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.SessionFactory;
import org.hibernate.type.Type;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.inventarios.model.TrasladoDet;
import com.luxsoft.utils.LoggerHelper;

/**
 * Hibernate interceptor para registrar la bitacora de cambios en las entidades
 * replicables
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("rawtypes")
public class AuditLogInterceptor extends EmptyInterceptor implements InitializingBean{
	
	
	private HibernateTemplate hibernateTemplate;
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6663894443784802729L;
	
	private Set<AuditLog> inserts=new HashSet<AuditLog>();
	private Set<AuditLog> deletes=new HashSet<AuditLog>();
	private Set<AuditLog> updates=new HashSet<AuditLog>();
	private Logger logger=LoggerHelper.getLogger();
	
	
	private List<Class> clases=new ArrayList<Class>();
	
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		if(id!=null && replicar(entity.getClass())){
			for(String destino:resolverDestinos(entity)){
				if(entity instanceof Traslado){
					Traslado t =(Traslado) entity;
					AuditLog audit1=new AuditLog(entity,id,"INSERT",getIPAdress(),getSucursalOrigen(),t.getSolicitud().getSucursal().getNombre());
					inserts.add(audit1);
					AuditLog audit2=new AuditLog(entity,id,"INSERT",getIPAdress(),getSucursalOrigen(),t.getSolicitud().getOrigen().getNombre());
					inserts.add(audit2);
					
				}else if(entity instanceof TrasladoDet){
					
					TrasladoDet t =(TrasladoDet) entity;
					AuditLog audit1=new AuditLog(entity,id,"INSERT",getIPAdress(),getSucursalOrigen(),t.getTraslado().getSolicitud().getSucursal().getNombre());
					inserts.add(audit1);
					AuditLog audit2=new AuditLog(entity,id,"INSERT",getIPAdress(),getSucursalOrigen(),t.getTraslado().getSolicitud().getOrigen().getNombre());
					inserts.add(audit2);
					
				}else
				inserts.add(new AuditLog(entity,id,"INSERT",getIPAdress(),getSucursalOrigen(),destino));
			}
		}
		return false;
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {		
		
		if(id!=null && replicar(entity.getClass())){
			for(String destino:resolverDestinos(entity)){
				if(entity instanceof Traslado){
					Traslado t =(Traslado) entity;
					AuditLog audit1=new AuditLog(entity,id,"UPDATE",getIPAdress(),getSucursalOrigen(),t.getSolicitud().getSucursal().getNombre());
					inserts.add(audit1);
					AuditLog audit2=new AuditLog(entity,id,"UPDATE",getIPAdress(),getSucursalOrigen(),t.getSolicitud().getOrigen().getNombre());
					inserts.add(audit2);
					
				}else if(entity instanceof TrasladoDet){
					
					TrasladoDet t =(TrasladoDet) entity;
					AuditLog audit1=new AuditLog(entity,id,"UPDATE",getIPAdress(),getSucursalOrigen(),t.getTraslado().getSolicitud().getSucursal().getNombre());
					inserts.add(audit1);
					AuditLog audit2=new AuditLog(entity,id,"UPDATE",getIPAdress(),getSucursalOrigen(),t.getTraslado().getSolicitud().getOrigen().getNombre());
					inserts.add(audit2);
					
				}else
				updates.add(new AuditLog(entity,id,"UPDATE",getIPAdress(),getSucursalOrigen(),destino));
			}
		}
		return false;
	}
	
	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		if(id!=null && replicar(entity.getClass())){
			for(String destino:resolverDestinos(entity)){
				if(entity instanceof Traslado){
					Traslado t =(Traslado) entity;
					AuditLog audit1=new AuditLog(entity,id,"DELETE",getIPAdress(),getSucursalOrigen(),t.getSolicitud().getSucursal().getNombre());
					inserts.add(audit1);
					AuditLog audit2=new AuditLog(entity,id,"DELETE",getIPAdress(),getSucursalOrigen(),t.getSolicitud().getOrigen().getNombre());
					inserts.add(audit2);
					
				}else if(entity instanceof TrasladoDet){
					
					TrasladoDet t =(TrasladoDet) entity;
					AuditLog audit1=new AuditLog(entity,id,"DELETE",getIPAdress(),getSucursalOrigen(),t.getTraslado().getSolicitud().getSucursal().getNombre());
					inserts.add(audit1);
					AuditLog audit2=new AuditLog(entity,id,"DELETE",getIPAdress(),getSucursalOrigen(),t.getTraslado().getSolicitud().getOrigen().getNombre());
					inserts.add(audit2);
					
				}else
				deletes.add(new AuditLog(entity,id,"DELETE",getIPAdress(),getSucursalOrigen(),destino));
			}
		}	
	}
	
	private String[] resolverDestinos(Object entity){
		if(entity instanceof SolicitudDeTraslado){
			SolicitudDeTraslado sol=(SolicitudDeTraslado)entity;
			return new String[]{sol.getOrigen().getNombre()};
		}/*else if(entity instanceof Traslado){
			Traslado t=(Traslado)entity;
			if(t.getTipo().equals("TPE")){
				ServiceLocator2.getConfiguracion().getSucursal();
				return new String[]{t.getSolicitud().getSucursal().getNombre()};
				
			}else{
				return new String[]{"OFICINAS"};
			}
		}else if(entity instanceof TrasladoDet){
			TrasladoDet t=(TrasladoDet)entity;
			if(t.getTipo().equals("TPE")){
				return new String[]{t.getTraslado().getSolicitud().getSucursal().getNombre()};
			}else{
				return new String[]{"OFICINAS"};
			}
		}*/else{
			return new String[]{"OFICINAS"};
		}
	}
	
	public boolean replicar(Class clazz){
		/*
		for(Class c:clases){
			if (c.isAssignableFrom(clazz))
				return true;
		}
		return false;
		*/
		return true;
	}
	
	@Override
	public void postFlush(Iterator entities) {
		try{
			audit(inserts);
			audit(updates);
			audit(deletes);
		}catch (Exception e) {
			logger.error("Error en interceptor central de replica",e);
		}finally{
			inserts.clear();
			deletes.clear();
			updates.clear();
		}
	}
	

	public List<Class> getClases() {
		return clases;
	}


	public void setClases(List<Class> clases) {
		this.clases = clases;
	}

	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate=new HibernateTemplate(sessionFactory);
	}
	
	private HibernateTemplate getHibernateTemplate(){
		return hibernateTemplate;
	}

	private  String ip;
	
	public String getIPAdress(){
		return ip;
	}
	

	protected void audit(Set<AuditLog> logs){
		try {
			for(AuditLog log:logs){
				if(getExclusiones().contains(log.getEntityName()))
					return;
				getHibernateTemplate().save(log);
				getHibernateTemplate().flush();
				//Object res=getHibernateTemplate().merge(log);
				//System.out.println("Log registrado: "+res);
				logger.debug("Log registrado:"+log);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	private String sucursalOrigen;
	private String sucursalDestino;

	private List<String> exclusiones;


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

	public List<String> getExclusiones() {
		if(exclusiones==null){
			exclusiones=new ArrayList<String>();
			//sexclusiones.add("Folio");
		}
		return exclusiones;
	}

	public void setExclusiones(List<String> exclusiones) {
		this.exclusiones = exclusiones;
	}

	

	public void afterPropertiesSet() throws Exception {
		Assert.hasLength(sucursalOrigen,"Se requiere registrar la sucursal origen");
		Assert.notNull(hibernateTemplate,"Se requiere hibernateTemplate");
		
		try {
			ip=InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {ip="NO DISPONIBLE";}
		
	}
	

}
