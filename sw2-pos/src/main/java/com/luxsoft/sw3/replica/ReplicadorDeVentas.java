package com.luxsoft.sw3.replica;

import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;


/**
 * Replicador para las operaciones de ventas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeVentas {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 * Importa todas las ventas <b>PENDIENTES</b> de una sucursal a la base de datos central
	 * para el tipo de venta indicado
	 * 
	 */
	public synchronized void importarPendientes(){
		for(Long sucursalId:sucursales){
			try {
				importarPentidentes(sucursalId);
			} catch (Exception e) {
				logger.error("Error replicando ventas de la sucursal: "+sucursalId
						+"  "+ExceptionUtils.getRootCauseMessage(e),e);
			}
		}
		replicarCargosPorTesoreria();
	}
	
	public synchronized void importarPentidentes(Long sucursalId){
		String hql="select v.id from Venta v where v.sucursal.id=? and v.fecha>=? and v.importado is null";
		Object[] params={sucursalId,DateUtil.toDate("24/05/2010")};
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		List<String> ids=source.find(hql,params);
		for(String id:ids){
			importar(sucursalId, source, target, id);
			
		}
	} 
	
	/**
	 * Importa <b>TODAS LAS VENTAS DE UN DIA</b> de una sucursal a la base de datos central
	 * para el tipo de venta indicado
	 *  
	 * 
	 * @param fecha
	 * @param sucursalId
	 * @param origen
	 * @param sourceTemplate
	 * @param targetTemplate
	 */
	public synchronized void importar(
			final Date fecha
			,Long sucursalId
			,OrigenDeOperacion origen
			,HibernateTemplate sourceTemplate
			,HibernateTemplate targetTemplate){
		
		logger.info(
				MessageFormat.format(
						"Importando  ventas de {2} para {0,date,short} de la sucursal: {1}"
						,fecha,sucursalId,origen.name()));
		String sql="select cargo_id from sx_ventas where sucursal_id=? and fecha=? and origen=\'@ORIGEN\'";
		sql=sql.replaceAll("@ORIGEN", origen.name());
		Object[] args={
				new SqlParameterValue(Types.NUMERIC,sucursalId)
				,new SqlParameterValue(Types.DATE,fecha)
		};
		List<String> res=ReplicaServices.getInstance().getJdbcTemplate(sucursalId).queryForList(sql, args, String.class);
		
		for(String id:res){			
			importar(id, sucursalId, sourceTemplate, targetTemplate);
		}
	}	
	
	/**
	 * Importa la venta indicada de una sucursal a la base de datos central
	 * 
	 * @param id
	 * @param sucursalId
	 * @param source
	 * @param target
	 */
	public synchronized void importar(final String id
			,final Long sucursalId
			,final HibernateTemplate source
			,final HibernateTemplate target){
			String hql="from Venta v " +
			" left join fetch v.partidas p" +
			" left join fetch v.cliente c" +
			" where v.id=?"
		;
		List<Venta> ventas=source.find(hql, id);
		if(!ventas.isEmpty()){
			Venta v=ventas.get(0);
			v.setPedido(null);
			try {
				//v.setReplicado(new Date());
				v.setImportado(new Date());
				if(v.isCancelado()){
					target.replicate(v, ReplicationMode.OVERWRITE);
				}
				else
					target.replicate(v, ReplicationMode.IGNORE);
				source.update(v);
				logger.info("Venta replicada: "+v.getId());
				
			} catch (Exception e) {
				//e.printStackTrace();				
				if(e instanceof HibernateObjectRetrievalFailureException){
					logger.info("Hibernate Optimistic lock exception.. Permanecera pendiente");
				}
				logger.error("Error replicando venta: "+v.getId()+"  "+" Sucursal: "+sucursalId+"  "+ ExceptionUtils.getRootCauseMessage(e));
			}
		}
	}
	
	
	
	public synchronized void importar(final Long sucursalId
			,final HibernateTemplate source
			,final HibernateTemplate target,String...ids){
		for(String id:ids){
			try {
				importar(id,sucursalId,source,target);
			} catch (Exception e) {
				logger.error(ExceptionUtils.getRootCause(e),e);
			}
			
		}
	}
	
	public void importarFaltantes(Periodo periodo){
		for(Long sucursalId:getSucursales()){
			importarFaltantes(sucursalId,periodo);
		}
	}
	
	public void importarFaltantes(Long sucursalId,Periodo periodo){
		importarFaltantes(sucursalId, periodo.getListaDeDias().toArray(new Date[0]));
	}
	
	public void importarFaltantes(Long sucursalId,String...sdia){
		List<Date> dias=new ArrayList<Date>();
		for(String d:sdia){
			dias.add(DateUtil.toDate(d));
		}
		importarFaltantes(sucursalId, dias.toArray(new Date[0]));
	}
	
	/**
	 *  Importa las ventas faltantes de la sucursal para las fechas indicadas
	 * 
	 * @param sucursalId
	 */
	public void importarFaltantes(final Long sucursalId,Date...dias){
		
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		for(Date dia:dias){
			String msg=MessageFormat.format("Importando ventas pendientes del {0} para la sucursal: {1}", dia,sucursalId);
			System.out.println(msg);
			String hql="select v.id from Venta v where v.sucursal.id=? and v.fecha=? ";
			HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			List<String> faltantes=new ArrayList<String>();
			List<String> ids=source.find(hql, new Object[]{sucursalId,dia});
			for(String id:ids){
				//System.out.println("Verificando ID: "+id);
				Venta v=(Venta)target.get(Venta.class, id);
				if(v==null){
					System.out.println("Faltante ID: "+id);
					faltantes.add(id);
				}
			}
			System.out.println("Faltantes detectados: "+faltantes.size());
			importar(sucursalId, source, target, faltantes.toArray(new String[0]));
		}
	}
	
	/**
	 * Exporta una venta desde oficinas a la sucursal origen
	 * es decir la sucursal de la venta o cargo.
	 * 
	 * Asume q el cargo no existe en la sucursal
	 * 
	 */
	public void exportarCargo(final String id){
		Cargo cargo=(Cargo)Services.getInstance().getUniversalDao().get(Cargo.class, id);
		ReplicaServices.getInstance()
			.getHibernateTemplate(cargo.getSucursal().getId())
			.replicate(cargo, ReplicationMode.OVERWRITE);
		;
	}
	
	/**
	 * Replica todos los cargos por tesoreria pendientes
	 * 
	 */
	public void replicarCargosPorTesoreria(){
		String sql="SELECT CARGO_ID FROM SX_VENTAS WHERE TIPO=\'TES\' AND TX_REPLICADO IS NULL";
		List<String> ids=Services.getInstance().getJdbcTemplate().queryForList(sql, String.class);
		for(String id:ids){
			//System.out.println("Exportando cargo: "+id);
			Cargo cargo=(Cargo)Services.getInstance().getUniversalDao().get(Cargo.class, id);
			cargo.setReplicado(new Date());
			cargo.setImportado(new Date());
			HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(cargo.getSucursal().getId());
			target.replicate(cargo, ReplicationMode.OVERWRITE);
			Services.getInstance().getUniversalDao().save(cargo);
		}
	}
	
	public ReplicadorDeVentas addSucursal(Long... sucursales){
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
		POSDBUtils.whereWeAre();
		ReplicadorDeVentas replicador=new ReplicadorDeVentas();
		//replicador.importarPendientes();
		//replicador.importarFaltantes(5L, new Periodo("30/09/2010"));
		//replicador.addSucursal(3L,6L,2L,5L)
		//.importarFaltantes(new Periodo("01/11/2010","2/11/2010"));
		//replicador.importarFaltantes(7L, new Periodo("20/12/2010"));
		//replicador.replicarCargosPorTesoreria();
		
		/*replicador
		//	.addSucursal(7l,3L,5L,6L,2L)
			//.importarFaltantes(new Periodo("10/05/2010","10/05/2010"));
				.importarPentidentes(7L);
		 */
		
		/*replicador.importar("8a8a8588-2dcd2dea-012d-cd50dd4e-0006", 2L
								, Services.getInstance().getHibernateTemplate()
								, ReplicaServices.getInstance().getHibernateTemplate(2L)
				);
		*/
		
		/**/
		/*replicador.importar("8a8a828b-29180c61-0129-183cee5b-0002", 6L
				, ReplicaServices.getInstance().getHibernateTemplate(6L)
				, Services.getInstance().getHibernateTemplate()
				);*/
		
		
		replicador
		.addSucursal(3L,6L,2L,5L,9L)
	//	.addSucursal(3L)
		.importarFaltantes(new Periodo("01/06/2012","02/06/2012"));
				
	}
	

}
