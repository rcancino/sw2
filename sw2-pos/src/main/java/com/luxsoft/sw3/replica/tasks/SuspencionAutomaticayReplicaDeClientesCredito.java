package com.luxsoft.sw3.replica.tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.model.core.Cliente;


import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.replica.ReplicaServices;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

public class SuspencionAutomaticayReplicaDeClientesCredito {
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	
	public void execute(){
		POSDBUtils.whereWeAre();
		String sql=SQLUtils.loadSQLQueryFromResource("sql/suspencion_automatica_clientes.sql");
		//System.out.println(sql);
		
		//List<Cliente> replicables=new ArrayList<Cliente>();
		
		List<Map<String, Object>> rows=Services.getInstance()
				.getJdbcTemplate()
				.queryForList(sql);
		System.out.println("Registros a procesar a procesar: "+rows.size());
		for(Map<String,Object> row :rows){
			String clave=(String)row.get("CLAVE");
			Cliente c=Services.getInstance().getClientesManager().buscarPorClave(clave);
			System.out.println("Cliente: "+c);
			if(!c.getCredito().isSuspendido()){
				System.out.println("Actualizando suspendido cliente: "+c);
				c.getCredito().setSuspendido(true);
				c.agregarComentario("SUSP_AUT", DateUtil.getDate(new Date()));
				c=Services.getInstance().getClientesManager().save(c);
				//replicables.add(c);
			}
		}
		
		/*for(Long sucursalId:getSucursales()){
			System.out.println("Replicando clientes a sucursal: "+sucursalId);
			HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			for(Cliente c:replicables){
				try {
					target.replicate(c, ReplicationMode.OVERWRITE);
				} catch (Exception e) {
					System.out.println(ExceptionUtils.getRootCauseMessage(e));
					
				}
				
			}
		}*/
		
	}


	public Set<Long> getSucursales() {
		return sucursales;
	}


	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public static void main(String[] args) {
		SuspencionAutomaticayReplicaDeClientesCredito task=new SuspencionAutomaticayReplicaDeClientesCredito();
		Set<Long> sucs=new HashSet<Long>();
		sucs.add(2L);
		sucs.add(3L);
		sucs.add(5L);
		sucs.add(6L);
		sucs.add(7L);
		task.setSucursales(sucs);
		task.execute();
	}

}
