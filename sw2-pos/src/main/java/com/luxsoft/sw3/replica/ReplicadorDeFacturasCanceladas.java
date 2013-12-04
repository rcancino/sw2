package com.luxsoft.sw3.replica;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;


import com.luxsoft.siipap.cxc.model.CancelacionDeCargo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;


/**
 * Replicador para importar las facturas canceladas en las sucursales
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeFacturasCanceladas {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	
	
	/**
	 * Importa todas las solicitudes pendientes
	 * 
	 */
	public void importar(){
		//logger.info("\nImportando facturas canceladas..."+new Date());
		for(Long sucursalId:getSucursales()){
			importar(sucursalId);
		}
	}
	
	/**
	 * Importa todas las facturas  <b>PENDIENTES</b> 
	 *  
	 * 
	 * @param fecha
	 * @param sucursalId
	 * @param origen
	 * @param sourceTemplate
	 * @param targetTemplate
	 */
	public void importar(Long sucursalId){
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		List<String> pendientes=source
					.find("select s.id from CancelacionDeCargo s " +
							" where s.importado is null " +
							" and s.fecha>=?" +
							" order by s.fecha desc",DateUtil.toDate("06/06/2012"));
		for(String id:pendientes){
			importar(id, sucursalId);
		}
	}
	
	/**
	 * Importa la factura cancelada indicada de una sucursal a la base de datos central
	 * 
	 * @param id
	 * @param sucursalId
	 * @param source
	 * @param target
	 */
	public void importar(final String id,final Long sucursalId){
		
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		CancelacionDeCargo cancelacion=(CancelacionDeCargo)source.get(CancelacionDeCargo.class, id);
		
		try {
			CancelacionDeCargo found=(CancelacionDeCargo)target.get(CancelacionDeCargo.class, id);
			if(found!=null){
				cancelacion.setImportado(found.getImportado()!=null?found.getImportado():new Date());
				cancelacion.setReplicado(found.getImportado()!=null?found.getImportado():new Date());
				source.update(cancelacion);
				logger.info("Cancelacion importada con la venta... Cancelacion ID: "+cancelacion.getId()+" Sucursal: "+sucursalId);
				return;
			}
			//Eliminamos la factura en produccion
			eliminarPosiblesAplicaciones(cancelacion.getCargo().getId());
			Services.getInstance().getUniversalDao().remove(Venta.class, cancelacion.getCargo().getId());			
			cancelacion.setImportado(new Date());
			cancelacion.setReplicado(new Date());
			Venta v=(Venta)cancelacion.getCargo();
			v.setPedido(null);
			target.replicate(cancelacion, ReplicationMode.IGNORE);
			source.update(cancelacion);
			logger.info("Cancelacion de cargo importada: "+cancelacion.getId()+" Sucursal: "+sucursalId);
		} catch (Exception e) {				
			logger.error("Error importando cancelacion de cargo: "+cancelacion.getId()
					+"  "+" de la sucursal: "+sucursalId+"  \n"+ExceptionUtils.getRootCauseMessage(e));				
		}	
	}
	
	private void eliminarPosiblesAplicaciones(String id){
		String DELETE="DELETE FROM SX_CXC_APLICACIONES WHERE CARGO_ID=?";
		Services.getInstance().getJdbcTemplate().update(DELETE,new Object[]{id});
	}
	
	
	public ReplicadorDeFacturasCanceladas addSucursal(Long... sucursales){
		for (Long sucursalId:sucursales){
			getSucursales().add(sucursalId);
		}
		return this;
	}
	
	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public static void main(String[] args) {
		
		ReplicadorDeFacturasCanceladas replicador=new ReplicadorDeFacturasCanceladas();
		replicador.addSucursal(3L)
		//replicador.addSucursal(6L)
		//.importar();
		//.importar(5L)
		//.refresh();
		//.importar("8a8a828b-295ad80e-0129-5b4dce27-0025", 6L)
		//replicador.addSucursal(3L)
		.importar();
		;
		
	}

}
