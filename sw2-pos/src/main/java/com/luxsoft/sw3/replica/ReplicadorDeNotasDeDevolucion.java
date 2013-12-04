package com.luxsoft.sw3.replica;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.services.Services;

/**
 *  Importa y exporta Notas de cretido por devolución generadas
 *  en el punto de venta y/o oficinas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeNotasDeDevolucion {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	public void sincronizar(){
		logger.debug("Replicando notas de devolucion "+new Date());
		importar();
		replicar();
		//replicarBonificacion();
	}
	
	/**
	 * Importa las notas de devolucion generadas en la sucursal.
	 * Solo las de MOSTRADOR  
	 * 
	 */
	public ReplicadorDeNotasDeDevolucion importar(){
		for(Long sucursalId:getSucursales()){
			try {
				importar(sucursalId);
			} catch (Exception e) {
				logger.error("Imposible importar notas de devolucion pendientes a la sucursal: "+sucursalId,e);
			}
		}
		return this;
	}
	
	/**
	 *	Importa las notas de devolucion pendientes generadas en la sucursal.
	 * 	Solo las de MOSTRADOR
	 *  
	 * @param sucursalId
	 */
	public void importar(Long sucursalId){
		String hql="select n.id from NotaDeCreditoDevolucion n " +
				" where n.origen=\'MOS\'" +
				" and n.importado is null" +
				" and n.fecha>=?";
		List<String> ids=Services.getInstance().getHibernateTemplate().find(hql,DateUtil.toDate("05/04/2010"));
		logger.debug("Notas pendientes por importar : "+ids.size()+ "Sucursal: "+sucursalId);		
		for(String id:ids){
			try {
				importar(sucursalId,id);
			} catch (Exception e) {
				logger.debug("Imposible importar la Nota de devolucion:"+id+ " De la sucursal: "
						+sucursalId);
			}
			
		}
	} 
	
	
	
	public void importar(Long sucursalId,String id){
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		String hql="from NotaDeCreditoDevolucion n left join fetch n.aplicaciones a where n.id=?";
		List<NotaDeCreditoDevolucion> notas=source.find(hql,new Object[]{id}); 
		if(!notas.isEmpty()){
			NotaDeCreditoDevolucion nota=notas.get(0);
			ComprobanteFiscal cf=Services.getInstance().getComprobantesDigitalManager().cargarComprobante(nota);
			if(cf==null){				
				try {
					cf=Services.getInstance().getComprobantesDigitalManager().generarComprobante(nota);
					nota.setImportado(new Date());
					nota.setFolio(Integer.valueOf(cf.getFolio()));
					nota.setReplicado(null);
					Services.getInstance().getHibernateTemplate().replicate(nota, ReplicationMode.OVERWRITE);
					nota.setImportado(new Date());
					source.update(nota);
					logger.info("Nota de devolucion importada "+nota.getId()+ " CFD generado: "+cf.getId());
					
				} catch (Exception e) {
					logger.info("Imposible actualizar el campo de importado en la nota: "+nota.getId()+ "de la sucursal: "+nota.getSucursal().getId());
				}
				
			}else{
				logger.info("CFD ya generado para la nota: "+nota.getId());
			}
			
			
		}
	}

	
	
	/**
	 * Replica las notas de credito pendientes de replicar a sus sucursal
	 * 
	 */
	public void replicar(){
		for(Long sucursalId:getSucursales()){
			try {
				replicar(sucursalId);
			} catch (Exception e) {
				logger.error("Imposible replciar notas de devolucion pendientes a la sucursal: "+sucursalId,e);
			}
			
		}
	}
	
	public void replicar(Long sucursalId){
		String hql="select n.id from NotaDeCreditoDevolucion n " +
		" where n.sucursal.id=?" +
		"  and n.fecha>=?" +
		"  and n.replicado is null" +
		"  and n.devolucion.venta.origen!=\'CRE\'";
		Object[] params={sucursalId,DateUtil.toDate("01/03/2010")};
		List<String> ids=Services.getInstance()
			.getHibernateTemplate().find(hql,params);
		if(ids.size()==0)
			return;
		logger.info("Notas pendientes por replicar: "+ids.size());
		HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);		
		for(String id:ids){
			try {
				NotaDeCreditoDevolucion nota=buscarNota(id);
				target.replicate(nota, ReplicationMode.OVERWRITE);
				logger.info("Nota replicada: "+id+ " Sucursal: "+sucursalId);
				updateReplicacion(nota);
			} catch (Exception e) {
				logger.error("Imposible replciar abono :"+id+ " De la sucursal: "
						+sucursalId);
			}
		}
	}
	
	public void replicar(String id){
		NotaDeCreditoDevolucion nota=buscarNota(id);
		HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(nota.getSucursal().getId());
		target.replicate(nota, ReplicationMode.OVERWRITE);
		logger.info("Nota replicada: "+id+ " A suursal: "+nota.getSucursal().getId());
		updateReplicacion(nota);
	}
	
	private NotaDeCreditoDevolucion buscarNota(String id){
		String hql="from NotaDeCreditoDevolucion n " +
				" left join fetch n.aplicaciones a" +
				" left join fetch a.autorizacion aut" +
				" where n.id=?";
		List<NotaDeCreditoDevolucion> notas=Services.getInstance().getHibernateTemplate().find(hql,id);
		if(notas.isEmpty())
			return null;
		return notas.get(0);
	}
	
	private void updateReplicacion(NotaDeCreditoDevolucion nota){
		try {
			nota.setReplicado(new Date());
			Services.getInstance().getHibernateTemplate().update(nota);
		} catch (Exception e) {
			logger.info("Imposible actualizar el campo de replicado en la nota: "+nota.getId()+ "de la sucursal: "+nota.getSucursal().getId());
		}
	}
	
	
	
	public ReplicadorDeNotasDeDevolucion addSucursal(Long... sucursales){
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
	
	
	public void replicarBonificacion(){
		System.out.println("Si corre pero no entra");
		for(Long sucursalId:getSucursales()){
			try {
				System.out.println(sucursalId);
				replicarBonificacion(sucursalId);
			} catch (Exception e) {
				logger.error("Imposible replciar notas de bonificacion pendientes a la sucursal: "+sucursalId,e);
			}
			
		}
	}
	
	public void replicarBonificacion (Long sucursalId){
		String hql="select n.id from NotaDeCreditoBonificacion n " +
		" where n.sucursal.id=?" +
		"  and n.fecha>=?" +
		"  and n.replicado is null" +
		"  and n.origen!=\'CRE\'";
		Object[] params={sucursalId,DateUtil.toDate("28/03/2010")};
		List<String> ids=Services.getInstance()
			.getHibernateTemplate().find(hql,params);
		if(ids.size()==0)
			return;
		logger.info("Notas pendientes por replicar: "+ids.size());
		HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);		
		for(String id:ids){
			try {
				NotaDeCreditoBonificacion nota=buscarNotaBonificacion(id);
				target.replicate(nota, ReplicationMode.OVERWRITE);
				logger.info("Nota replicada: "+id+ " Sucursal: "+sucursalId);
				updateReplicacionBonificacion(nota);
			} catch (Exception e) {
				logger.error("Imposible replciar abono :"+id+ " De la sucursal: "
						+sucursalId);
			}
		}
	}
	
	
	public void replicarBonificacion(String id){
		NotaDeCreditoBonificacion nota=buscarNotaBonificacion(id);
		System.out.println(nota.getSucursal().getId());
		System.out.println(nota.getId());
		HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(nota.getSucursal().getId());
		target.replicate(nota, ReplicationMode.OVERWRITE);
		logger.info("Nota replicada: "+id+ " A suursal: "+nota.getSucursal().getId());
		updateReplicacionBonificacion(nota);
	}
	
	private void updateReplicacionBonificacion(NotaDeCreditoBonificacion nota){
		try {
			nota.setReplicado(new Date());
			Services.getInstance().getHibernateTemplate().update(nota);
		} catch (Exception e) {
			logger.info("Imposible actualizar el campo de replicado en la nota: "+nota.getId()+ "de la sucursal: "+nota.getSucursal().getId());
		}
	}
	
	
	private NotaDeCreditoBonificacion buscarNotaBonificacion(String id){
		String hql="from NotaDeCreditoBonificacion n " +
				" left join fetch n.aplicaciones a" +
				" left join fetch a.autorizacion aut" +
				" where n.id=?";
		List<NotaDeCreditoBonificacion> notas=Services.getInstance().getHibernateTemplate().find(hql,id);
		if(notas.isEmpty())
			return null;
		return notas.get(0);
	}
	
	
	public static void main(String[] args) {
		
		ReplicadorDeNotasDeDevolucion replicador=new ReplicadorDeNotasDeDevolucion();
		/*replicador.addSucursal(6L)
		.importar()
		.replicar();
		.replicar("8a8a819d-283bdd32-0128-3be9f2d5-0007");*/
		replicador.addSucursal(2L,3L,5L,6L);
		replicador.replicarBonificacion();
		
	}

}
