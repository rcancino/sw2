package com.luxsoft.sw3.replica;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.sw3.services.Services;

/**
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class ReplicadorDeProductos extends ReplicadorTemplate{
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	
	
	/**
	 * Replica a todas las sucursales los productos pendientes
	 */
	public void replicar(){
		logger.info("Replicando productos...."+new Date());
		String sql="select  id,clave from sx_productos_log where tx_replicado is null";		
		List<Map<String,Object>> rows=Services.getInstance().getJdbcTemplate()
			.queryForList(sql);
		for(Map<String,Object> row:rows){
			String clave=(String)row.get("CLAVE");
			Number id=(Number)row.get("ID");
			boolean actualizar=true;
			for(Long sucursalId:getSucursales()){
				logger.info("Actualizando productos para la sucursal: "+sucursalId);
				try {
					
					refreshProducto(clave,sucursalId);
					patchId(clave, sucursalId);
				} catch (Exception e) {
					logger.error("Imposible replicar cambios en producto: "+clave+ "A la sucursal: "+sucursalId,e);
					actualizar=false;
				}
			}
			if(actualizar){
				try {
					String update="update sx_productos_log set tx_replicado=? where id=?";
					Object[] args=new Object[]{new Date(),id.longValue()};
					int actualizados=Services.getInstance().getJdbcTemplate().update(update, args);
					logger.info("Bitacora actualizada: "+id+ " ");
				} catch (DataAccessException e) {
					logger.error("Imposible registrar replición del producto: "+clave,e );
				}
			}
		}
	}
	/*
	public void refresh(){
		for(Long sucursalId:sucursales){
			refresh(sucursalId);
		}
	}
	
	
	public void refresh(Long sucursalId){
		logger.info("Actualizando productos para la sucursal: "+sucursalId);
		String sql="select distinct clave from sx_productos_log ";		
		List<String> claves=Services.getInstance().getJdbcTemplate().queryForList(sql,String.class);
		for(String clave:claves){
			refreshProducto(clave,sucursalId);
			patchId(clave, sucursalId);
		}
	}
	
	*/
	
	public void refreshProducto(String clave,Long sucursalId){
		Producto producto=Services.getInstance().getProductosManager().buscarPorClave(clave);
		HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		target.replicate(producto, ReplicationMode.OVERWRITE);
		logger.info("Producto actualizado : "+producto+" Suc:"+sucursalId);
	}
	
	public void patchId(String clave,Long sucursalId){		
		JdbcTemplate template=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
		Producto producto=Services.getInstance().getProductosManager().buscarPorClave(clave);
		Object[] args={producto.getId(),producto.getClave()};
		template.update("update sx_productos set producto_id=? where clave=?", args);
	}
	
	
	public ReplicadorDeProductos addSucursal(Long...sucursales){
		for(Long suc:sucursales){
			getSucursales().add(suc);
		}
		return this;
	}

	public Set<Long> getSucursales() {
		return sucursales;
	}


	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public void mandarCatalogo(Long sucursalId){
		List<Producto> productos=Services.getInstance().getProductosManager().getAll();
		HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		for(Producto p:productos){
			try {
				target.replicate(p, ReplicationMode.OVERWRITE);
				System.out.println("Producto replicado: "+p);
				patchId(p.getClave(), sucursalId);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("No se pudo replicar el producto: "+p.getClave()+ " Err: "+ExceptionUtils.getRootCauseMessage(e));
			}
		}
	}
	
	public void replicarCatalogoQueretaro(){
		List<Producto> productos=Services.getInstance().getProductosManager().getAll();
		HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(7L);
		for(Producto p:productos){
			try {
				target.replicate(p, ReplicationMode.OVERWRITE);
				System.out.println("Producto replicado: "+p);
				patchId(p.getClave(), 7L);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("No se pudo replicar el producto: "+p.getClave()+ " Err: "+ExceptionUtils.getRootCauseMessage(e));
			}
			
			
		}
	}


	public static void main(String[] args) {
		ReplicadorDeProductos replicador=new ReplicadorDeProductos();
		//replicador.addSucursal(2L,5L,6L,3L,9L)
		
		//.replicar();
		//replicador.replicarCatalogoQueretaro();
		
		replicador.mandarCatalogo(6L);
	}
	

}
