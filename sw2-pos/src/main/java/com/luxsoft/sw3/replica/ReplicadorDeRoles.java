package com.luxsoft.sw3.replica;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.sw3.services.Services;


/**
 * Permite enviar un rol a las distintas sucursales
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeRoles {
	
private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 * Inserta un nuevo rol tanto en Produccion como en las sucursales
	 * 
	 * @param roleName
	 * @param desc
	 */
	public void insertarRole(String roleName,String desc){
		Role r=new Role(roleName);
		r.setDescription(desc);
		r=(Role)Services.getInstance().getUniversalDao().save(r);
		for(Long sucursalId:getSucursales()){
			try {
				HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
				target.replicate(r, ReplicationMode.OVERWRITE);
			} catch (Exception e) {
				logger.info("Error replicando role: "+r+ " sucursal: "+sucursalId+ " "
						+ ExceptionUtils.getRootCauseMessage(e));
				logger.error(e);
			}
		}
	}
		
	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public ReplicadorDeRoles addSucursal(Long... sucursales){
		for (Long sucursalId:sucursales){
			getSucursales().add(sucursalId);
		}
		return this;
	}
	
	public static void main(String[] args) {
		ReplicadorDeRoles r=new ReplicadorDeRoles();
		r.addSucursal(2L,3L,5L,6L)
		.insertarRole(POSRoles.CONTROLADOR_DE_ANTICIPOS.name()
				, "Administración y control de anticipos facturados");
		
	}

}
