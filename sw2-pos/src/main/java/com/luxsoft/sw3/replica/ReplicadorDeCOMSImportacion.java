package com.luxsoft.sw3.replica;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import com.luxsoft.siipap.compras.model.RecepcionDeCompra;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador de coms unitarios
 * 
 * @author Ruben Cancino
 *
 */
public class ReplicadorDeCOMSImportacion {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 *
	 * 
	 */
	public void replicar(){		
		String hql="select r.id from RecepcionDeCompra r" +
				" where r.compra.importacion=true " +
				" and r.replicado is null";
		List<String> ids=Services.getInstance().getHibernateTemplate().find(hql);
		for(String id:ids){
			try {
				replicar(id);
			} catch (Exception e) {
				String msg="Errior replicando recepcion: "+id+ " Causa: "+ExceptionUtils.getRootCauseMessage(e);
				logger.error(msg,e);
			}
		}
	}

	/**
	 * Replica el com indicado
	 * 
	 * @param id
	 */
	public void replicar(String id){
		String hql="from RecepcionDeCompra r " +
				" left join fetch r.partidas p  " +
				" where r.id=? ";
		List<RecepcionDeCompra> res=Services.getInstance().getHibernateTemplate().find(hql,id);
		Assert.notEmpty(res,"No existe la recepcion: "+id);
		RecepcionDeCompra com=res.get(0);
		for(Long sucursalId:getSucursales()){
			if(com.getSucursal().getId().equals(sucursalId))
				continue;
			HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			target.replicate(res, ReplicationMode.EXCEPTION);
		}
	}
	
	
	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public ReplicadorDeCOMSImportacion addSucursales(Long...longs){
		for(Long suc:longs){
			getSucursales().add(suc);
		}
		return this;
	}

	public static void main(String[] args) {
		ReplicadorDeCOMSImportacion replicador=new ReplicadorDeCOMSImportacion();
		replicador.addSucursales(2L,3L,5L,6L)
		.replicar("")
		;
	}
}
