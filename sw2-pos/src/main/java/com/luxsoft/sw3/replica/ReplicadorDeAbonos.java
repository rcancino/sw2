package com.luxsoft.sw3.replica;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.CargoPorTesoreria;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador para abonos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeAbonos {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 * Importa los abonos pendientes del dia
	 */
	public void importar(){
		importar(Periodo.hoy());
	} 
	
	public void importar(Periodo periodo){
		for(Long sucursalId:getSucursales()){
			try {
				importar(sucursalId,periodo);
			} catch (Exception e) {
				logger.error("Error replicando abonos a la sucursal: "+sucursalId
						+"  "+ExceptionUtils.getRootCauseMessage(e),e);	
			}
			
		}
	}
	
	public void importar(Long sucursalId,Periodo periodo){
		importar(sucursalId, periodo.getListaDeDias().toArray(new Date[0]));
	}
	
	
	
	
	/**
	 * Importa el abono indicado de una sucursal a la base de datos central
	 * 
	 * @param id
	 * @param sucursalId
	 * @param source
	 * @param target
	 */
	public void importar(final String id
			,final Long sucursalId
			,final HibernateTemplate source
			,final HibernateTemplate target){
			String hql="from Abono a " +
			" left join fetch a.aplicaciones ap" +
			//" left join fetch a.cliente c" +
			" where a.id=? "
		;
		List<Abono> abonos=source.find(hql, id);
		if(!abonos.isEmpty()){
			Abono a=abonos.get(0);
			if(a instanceof NotaDeCreditoDevolucion){				
				return;				
			}
			try {
				a.setImportado(new Date());
				target.replicate(a, ReplicationMode.OVERWRITE);
				source.update(a);
				logger.debug("Abono replicado: "+a.getId());
			} catch (Exception e) {				
				logger.error("Error replicando abono: "+a.getId()
						+"  "+ExceptionUtils.getRootCauseMessage(e));				
			}			
		}
	}
	
	/**
	 * Importa los abonos de la sucursal para las fechas indicadas
	 * 
	 * @param sucursalId
	 */
	public void importar(final Long sucursalId,Date...dias){
		
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		for(Date dia:dias){
			String msg=MessageFormat.format("Importando abonos pendientes del {0} para la sucursal: {1}", dia,sucursalId);
			logger.info(msg);
			//Abonos del dia 
			String hql="select v.id from Abono v where v.sucursal.id=? and v.fecha=? and v.importado is null";
			HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			Set<String> ids=new HashSet(source.find(hql, new Object[]{sucursalId,dia}));
			if(ids.size()>0)
				logger.info("Abonos generados en el dia por  importar: "+ids.size());
			
			//Abonos de aplicaciones generadas en el dia
			String hql2="select a.abono.id from Aplicacion a where a.cargo.sucursal.id=? and a.fecha=? and a.fecha>a.abono.fecha";
			Set<String> aplicIds=new HashSet<String>(source.find(hql2, new Object[]{sucursalId,dia}));
			if(aplicIds.size()>0)
				logger.info("Abonos con aplicaciones en el dia: "+aplicIds.size());
			ids.addAll(aplicIds);
			if(ids.size()>0)
				logger.info("Total abonos por importar: "+ids.size());
			importar(sucursalId, source, target, ids.toArray(new String[0]));
		}
	}
	
	public void importar(final Long sucursalId
			,final HibernateTemplate source
			,final HibernateTemplate target,String...ids){
		int row=0;
		for(String id:ids){
			importar(id,sucursalId,source,target);
			logger.info("Abono: "+row+" de: "+ids.length);
			row++;
		}
	}
	
	public void actualizarAbonosEliminados(){
		
		for(Long sucursalId:getSucursales()){			
			String SQL="SELECT ABONO_ID from sx_cxc_abonos_borrados where TX_ACTUALIZADO is null";
			JdbcTemplate source=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
			List<String> ids=source.queryForList(SQL, String.class);
			if(ids.size()>0)
				logger.info("\tEliminando abonos desde la sucursal: "+sucursalId+ "Abonos por eliminar: "+ids.size());
			for(String id:ids){
				
				try {
					
					
					//Borramos las aplicaciones
					String DELETE2="DELETE FROM SX_CXC_APLICACIONES WHERE ABONO_ID=?";
					int deleted=Services.getInstance().getJdbcTemplate().update(DELETE2,new Object[]{id});
					
					//Borramos el abono
					String DELETE="DELETE FROM SX_CXC_ABONOS WHERE ABONO_ID=?";
					deleted=Services.getInstance().getJdbcTemplate().update(DELETE,new Object[]{id});
					
					//Actualizamos el origen
					if(deleted>0){
						logger.info("Abono eliminado: "+id);
					}
					String UPDATE="UPDATE sx_cxc_abonos_borrados set TX_ACTUALIZADO=now() where ABONO_ID=?";
					source.update(UPDATE,new Object[]{id});
				} catch (DataAccessException e) {
					logger.error("Error eliminando abono: "+id
							+"  "+ExceptionUtils.getRootCauseMessage(e));	
				}
				
			}
		}
		
	}
	
	/**
	 * Envia a las sucursales los abonos requeridos
	 * 
	 * Actualmente solo los que se aplicaran a un CargoPorTesoreria
	 * 
	 * 
	 *//*
	public void replicarAplicacionDePagos(Date fecha){
		String hql="from Aplicacion a where a.fecha=? and a.abono.replicado is null";
		List<Aplicacion> aplicaciones=Services.getInstance().getHibernateTemplate().find(hql, fecha);
		for(Aplicacion a:aplicaciones){
			if(a.getCargo() instanceof CargoPorTesoreria){
				Long sucursalId=a.getCargo().getSucursal().getId();
				HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
				//CargoPorTesoreria cargo=
				target.replicate(a, ReplicationMode.IGNORE);
			}
		}
	}*/
	
	public void replicarAplicacionDePagosEnCargosPorTesoreria(){
		
		String sql="select a.abono_id" +				
				" from sx_cxc_aplicaciones a " +
				" join sx_ventas c on(a.cargo_id=c.cargo_id) " +
				" join sx_cxc_abonos b on(a.ABONO_ID=b.ABONO_ID) " +
				" where c.tipo=\'TES\' " +
				"   and b.tx_replicado is null ";
		List<String> ids=Services.getInstance().getJdbcTemplate().queryForList(sql, String.class);
		for(String id:ids){
			try {
				String hql="from Abono a left join fetch a.aplicaciones ap " +
						" left join fetch ap.autorizacion az" +
						" where a.id=?";
				List<Abono> found=Services.getInstance().getHibernateTemplate().find(hql,id);
				if(!found.isEmpty()){
					Abono abono=found.get(0);
					HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(abono.getSucursal().getId());
					target.replicate(abono, ReplicationMode.OVERWRITE);
					actualizarFechaDeReplicacion(abono);
					logger.info("Abono replicado: "+abono.getId()+ " Suc: "+abono.getSucursal().getId());
					
				}
				
			} catch (DataAccessException e) {
				logger.error("Error actualizando abono: "+id
						+"  "+ExceptionUtils.getRootCauseMessage(e));	
			}
		}		
	}
	
	private void actualizarFechaDeReplicacion(final Abono abono){
		String UPDATE="UPDATE SX_CXC_ABONOS SET TX_REPLICADO=NOW() WHERE ABONO_ID=?";
		Services.getInstance().getJdbcTemplate().update(UPDATE, new Object[]{abono.getId()});
	}

	
	public ReplicadorDeAbonos addSucursal(Long... sucursales){
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
		
		ReplicadorDeAbonos replicador=new ReplicadorDeAbonos();
		
			
		//replicador.importar("8a8a81e7-30d16c92-0130-d1de194c-00da",
				//3L,ReplicaServices.getInstance().getHibernateTemplate(3L) 
				//,Services.getInstance().getHibernateTemplate()
				//);
		
		//.importar("8a8a8190-2bd0ddd3-012b-d1227014-0028", 3L, ReplicaServices.getInstance().getHibernateTemplate(3L)
			//	, Services.getInstance().getHibernateTemplate());
		
		//.addSucursal(2L,6L,3L,5L)
		//replicador.addSucursal(9L)
		//.importar(6L,new Periodo("03/11/2010","04/11/2010"));
		//replicador.importar(6L,new Periodo("27/10/2010","01/11/2010"));
		/*.importar("8a8a81e7-2bd44cbb-012b-d52637db-0178"
			, 5L
			, ReplicaServices.getInstance().getHibernateTemplate(3L)
			, Services.getInstance().getHibernateTemplate()
			
		);*/
		/*.importar(5L
				, ReplicaServices.getInstance().getHibernateTemplate(5L)
				, Services.getInstance().getHibernateTemplate()
				,"8a8a8485-2c177069-012c-181ef0ec-00d0"
				);*/
		//.actualizarAbonosEliminados();
		//replicador.importar(6L, new Periodo("01/04/2012","30/04/2012")); 
		
		replicador
		.addSucursal(2L,6L,3L,5L,9L)
	//	.addSucursal(2L)
		.importar(new Periodo("01/06/2012","02/06/2012"));
		//replicador.replicarAplicacionDePagosEnCargosPorTesoreria();
//	.actualizarAbonosEliminados();
		
		
	}

}
