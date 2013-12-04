package com.luxsoft.sw3.replica;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador Statefull para importar ordenes de compra
 * y sus entradas correspondientes 
 * 
 * @author Ruben Cancino
 *
 */
public class ReplicadorDeCompras extends ReplicadorTemplate{
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	public void sincronizar(){
		importar();
		replicar();
	}
	
	public void replicar(){
		//String hql="select c.id from Compra2 c where c.replicado is null and  c.fecha>=?";
		//List<String> ids=Services.getInstance().getHibernateTemplate().find(hql,DateUtil.toDate("11/05/2010"));
		String hql="select c.id from Compra2 c where c.replicado is null ";
		List<String> ids=Services.getInstance().getHibernateTemplate().find(hql);
		if(ids.size()>0)
			logger.info("Compras por replciar: "+ids.size());
		
		for(String id:ids){
			try {
				Compra2 compra=Services.getInstance().getComprasManager().buscarInicializada(id);
				if(compra.isImportacion()){
					replicarCompraImportacion(compra);
				}else if(compra.getConsolidada() ){
					replicarConsolidada(compra);
				}else{
					HibernateTemplate target=ReplicaServices.getInstance()
					.getHibernateTemplate(compra.getSucursal().getId());
					eliminarPartidasIfNecesary(compra, target);
					target.replicate(compra, ReplicationMode.OVERWRITE);
					
				}				
				compra.setReplicado(new Date());
				Services.getInstance().getUniversalDao().save(compra);				
				logger.info("Compra replicada: "+compra.getId());
			} catch (Exception e) {
				logger.error("Imposible replicar compra: "+id+ "  "+ExceptionUtils.getRootCauseMessage(e),e);
			}
		}
	}
	
	public void replicarConsolidada(final Compra2 compra){		
		Set<Long> sucs=new HashSet<Long>();
		for(CompraUnitaria cu:compra.getPartidas()){
			sucs.add(cu.getSucursal().getId());
		}
		for(Long sucursalId:sucs){
			try {
				HibernateTemplate target=ReplicaServices.getInstance()
				.getHibernateTemplate(sucursalId);
				eliminarPartidasIfNecesary(compra, target);
				target.replicate(compra, ReplicationMode.OVERWRITE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void replicarCompraImportacion(final Compra2 compra){
		
		for(Long sucursalId:getSucursales()){
			try {
				HibernateTemplate target=ReplicaServices.getInstance()
				.getHibernateTemplate(sucursalId);
				eliminarPartidasIfNecesary(compra, target);
				target.replicate(compra, ReplicationMode.OVERWRITE);				
			} catch (Exception e) {
				String msg="Error replicando compra: "+compra.getFolio()+" Err:\n"+ExceptionUtils.getRootCauseMessage(e);
				logger.error(msg,e);
				//e.printStackTrace();
			}
		}
	}
	
	/**
	 * Parche para eliminar partidas BUG de la replicacion
	 * 
	 * @param compra
	 * @param target
	 */
	private void eliminarPartidasIfNecesary(final Compra2 compra,HibernateTemplate target){
		//if(compra.getPartidas().size()==0){
		try {
			for(CompraUnitaria cu:compra.getPartidas()){
				if(cu.getRecibido()>0)
					continue;
			}
			String UPDATE="delete from CompraUnitaria where compra.id=?";
			int res=target.bulkUpdate(UPDATE, compra.getId());
			logger.info("Partidas eliminadas: "+res);
		} catch (Exception e) {
			logger.info("Compra inmutable: "+compra.getId()+ " sucursal: "+compra.getSucursal()+ " Al eliminar partidas: "+ExceptionUtils.getRootCauseMessage(e));
		}
			
		//}
	}
	
	private Compra2 inicializarCompra(String id,HibernateTemplate template) {
		String hql="from Compra2 c left join  fetch c.sucursal s" +
				" left join fetch c.proveedor p " +
				" left join fetch c.partidas l " +
				" where c.id=? "; 
		List<Compra2> data=template.find(hql, id);
		return data.isEmpty()?null:data.get(0);
	}
	
	public void replicar(String id){
		Compra2 compra=Services.getInstance().getComprasManager().buscarInicializada(id);
		if(compra.getConsolidada()){
			replicarConsolidada(compra);
		}else if(compra.isImportacion()){
			replicarCompraImportacion(compra);
		}else{
			HibernateTemplate target=ReplicaServices.getInstance()
			.getHibernateTemplate(compra.getSucursal().getId());
			eliminarPartidasIfNecesary(compra, target);
			target.replicate(compra, ReplicationMode.OVERWRITE);
		}
		compra.setReplicado(new Date());
		Services.getInstance().getUniversalDao().save(compra);				
		logger.info("Compra replicada: "+compra.getId());
	}
	
	/**
	 * Importa todas las compras pendientes de importacion
	 * 
	 */
	public void importar(){
		for(Long sucursalId:getSucursales()){
			try {
				importar(sucursalId);
			} catch (Exception e) {
				logger.error("Imposible importar compras de la sucursal: "+sucursalId+ " "+ExceptionUtils.getRootCauseMessage(e),e);
			}
		}
	}
	
	public void importar(Long sucursalId){
		//logger.info("Importando compras pendientes de la sucursal: "+sucursalId);
		String hql="select c.id from Compra2 c " +
				" where c.importado is null and c.fecha>=?";
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		List<String> ids=source.find(hql, DateUtil.toDate("6/05/2010"));
		if(!ids.isEmpty()){
			logger.info("Compras pendientes por importar: "+ids.size()+ " de la sucursal: "+sucursalId);
		}
		for(String id:ids){
			try {
				List<Compra2> data=source.find(
						"from Compra2 c left join fetch c.partidas det where c.id=?"
						,id);
				if(!data.isEmpty()){
					Compra2 compra=data.get(0);
					compra.setImportado(new Date());
					compra.setReplicado(new Date());
					eliminarPartidasIfNecesary(compra, Services.getInstance().getHibernateTemplate());
					Services.getInstance().getHibernateTemplate().replicate(compra, ReplicationMode.OVERWRITE);
					source.update(compra);
				}
			} catch (Exception e) {
				logger.error("Error importando compra: "+id+ ExceptionUtils.getRootCauseMessage(e),e);
			}
		}
	}
	
	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public ReplicadorDeCompras addSucursales(Long...longs){
		for(Long suc:longs){
			getSucursales().add(suc);
		}
		return this;
	}

	public static void main(String[] args) {
		ReplicadorDeCompras replicador=new ReplicadorDeCompras();	
		//replicador.addSucursales(2L,3L,5L,6L)
		//replicador.addSucursales(3L)
		//.replicar("8a8a8584-2821f2a6-0128-229491f6-003c");
		//.sincronizar();
		//.importar();
		//.replicar();
		replicador.importar(7L);
		//.replicar();
	
	}
}
