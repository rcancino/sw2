package com.luxsoft.sw3.replica;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;

public class ReplicadorDeAplicaciones {

	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 * Importa todas las aplicaciones de abonos <b>PENDIENTES</b> de una sucursal 
	 *  a la base de datos central
	 * 
	 */
	public void importarPendientes(){
		for(Long sucursalId:sucursales){
			importarPentidentes(sucursalId);
		}
	}
	
	public void importarPentidentes(Long sucursalId){
		String hql="select a.id from Acplicacion a " +
				" where a.cargo.sucursal.id=?" +
				" and year(a.fecha)>=2010 " +
				" and a.fecha>? " +
				" and a.importado is null";
		Object[] params={sucursalId,DateUtil.toDate("16/03/2010")};
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		List<String> ids=source.find(hql,params);
		for(String id:ids){			
			try {
				importar(id,sucursalId, source, target);
			} catch (Exception e) {
				logger.error("Imposible importar la aplicacion: "+id+ " Err:"+
						ExceptionUtils.getRootCause(e),e);
			}
		}
	}
	
	
	
	public void importar(final String id
			,final Long sucursalId
			,final HibernateTemplate source
			,final HibernateTemplate target){
		
		Aplicacion a=(Aplicacion)source.get(Aplicacion.class, id);
		if(a!=null){
			target.replicate(a, ReplicationMode.OVERWRITE);
		}
	}
	
	public void importarFaltantes(Periodo periodo){
		final HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		for(Long sucursalId:sucursales){
			try {
				HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
				List<Date> dias=periodo.getListaDeDias();
				for(Date dia:dias){
					String hql="select distinct a.abono.id " +
							"from Aplicacion a where a.fecha =? ";
					List<Aplicacion> aplicaciones=source.find(hql, new Object[]{dia});
					logger.info("Aplicaciones a importar: "+aplicaciones.size());
					for(Aplicacion a:aplicaciones){
						try {
							target.replicate(a, ReplicationMode.IGNORE);
						} catch (Exception e) {
							logger.error("Imposible replicar Aplicacion: "+a.getId(),e);
						}						
					}				
				}
			} catch (Exception e) {
				logger.error("Error al tratar de replicar aplicaciones para " +
						"la sucursal: "+sucursalId+ "Periodo: "+periodo,e);
			}
		}
	}
	
	public ReplicadorDeAplicaciones addSucursal(Long... sucursales){
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
		new ReplicadorDeAplicaciones()
		.addSucursal(3L)
		.importarFaltantes(new Periodo("16/03/2010","16/03/2010"));
	}
	
}
