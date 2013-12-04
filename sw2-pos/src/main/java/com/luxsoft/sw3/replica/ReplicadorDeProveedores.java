package com.luxsoft.sw3.replica;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;

import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.sw3.services.Services;

public class ReplicadorDeProveedores {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	public void replicar(){
		String sql="select clave from sx_proveedores where tx_replicado is null";
		List<String> claves=Services.getInstance().getJdbcTemplate().queryForList(sql,String.class);
		
		for(String clave:claves){
			replicar(clave);
		}
	}
	
	/**
	 * Replica todos los proveedores a la sucursal indicada
	 * 
	 * @param sucursalId
	 */
	public void replicarTodos(Long sucursalId){
		String sql="select clave from sx_proveedores ";
		List<String> claves=Services.getInstance().getJdbcTemplate().queryForList(sql,String.class);
		
		for(String clave:claves){
			Proveedor p=Services.getInstance().getProveedorManager().buscarInicializado(clave);
			logger.info("Sucursal: "+sucursalId);
			try {
				ReplicaServices.getInstance().getHibernateTemplate(sucursalId).replicate(p, ReplicationMode.OVERWRITE);
			} catch (Exception e) {
				logger.error("Error replicando :"+clave,e);
			}
			
		}
	}
	
	public void replicar(String clave){
		Proveedor p=Services.getInstance().getProveedorManager().buscarInicializado(clave);
		logger.info("Relicando proveedor: "+p.getClave()+ " Productos: "+p.getProductos().size() );
		for(Long sucursalId:getSucursales()){
			try {		
				logger.info("Replicando proveedor a Sucursal: "+sucursalId);
				ReplicaServices.getInstance().getHibernateTemplate(sucursalId).replicate(p, ReplicationMode.OVERWRITE);				
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
		p.setReplicado(new Date());
		Services.getInstance().getUniversalDao().save(p);
	}
	
	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public ReplicadorDeProveedores addSucursales(Long...longs){
		for(Long suc:longs){
			getSucursales().add(suc);
		}
		return this;
	}
	
	
	public static void main(String[] args) {
		ReplicadorDeProveedores replicador=new ReplicadorDeProveedores();
		//replicador.addSucursales(2L)
		replicador.addSucursales(2L,3L,5L,6L,9L)
		//replicador.addSucursales(2L)
		.replicar("I024") 
		//.replicar()
		//.replicarTodos(7L)
		;
	
	}

}
