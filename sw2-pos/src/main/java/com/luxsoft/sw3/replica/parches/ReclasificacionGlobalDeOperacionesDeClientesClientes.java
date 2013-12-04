package com.luxsoft.sw3.replica.parches;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.luxsoft.sw3.replica.ReplicaServices;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.services.parches.ReclasificacionDeOprecionesPorClientes;


/**
 * Reclasifica operaciones de cliente en las oficinas y en las sucursales
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReclasificacionGlobalDeOperacionesDeClientesClientes {
	
	public Set<Long> sucursales=new HashSet<Long>();
	
	public void execute(final Long clienteOrigen,final Long clienteDestino){
		ReclasificacionDeOprecionesPorClientes task=new ReclasificacionDeOprecionesPorClientes(Services.getInstance().getHibernateTemplate());
		try {
			task.execute(clienteOrigen, clienteDestino);
		} catch (Exception e1) {
			System.out.println("Error reclasificando cliente en oficinas: "+ExceptionUtils.getRootCauseMessage(e1));
		}
		for(Long sucursalId:getSucursales()){
			try {
				execute(clienteOrigen, clienteDestino,sucursalId);
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("Error reclasificando cliente en oficinas: "+ExceptionUtils.getRootCauseMessage(e));
			}
			
		}
	}
	
	public void execute(final Long clienteOrigen,final Long clienteDestino,Long sucursalId){
		System.out.println("    Reclasificando para la sucursal: "+sucursalId);
		ReclasificacionDeOprecionesPorClientes task=new ReclasificacionDeOprecionesPorClientes(ReplicaServices.getInstance().getHibernateTemplate(sucursalId));
		task.execute(clienteOrigen, clienteDestino);
	}
	
	public ReclasificacionGlobalDeOperacionesDeClientesClientes addSucursal(Long... sucursales){
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
		ReclasificacionGlobalDeOperacionesDeClientesClientes task=new 
				ReclasificacionGlobalDeOperacionesDeClientesClientes();
		//task.addSucursal(2L,3L,5L,6L,9L);
		task.addSucursal(9L);
		task.execute(702011L,602105L);
		//task.execute(11311L, 10558L);
		//task.execute(11311L, 10558L);
		/*
		List<Map<String, Number>> pendientes=Services.getInstance()
			.getJdbcTemplate().queryForList("select CLIENTE_ORIGEN,CLIENTE_DESTINO from clientes_duplicados ");
		for(Map<String,Number> row:pendientes){
			Long clienteOrigen=row.get("CLIENTE_ORIGEN").longValue();
			Long clienteDestino=row.get("CLIENTE_DESTINO").longValue();
			task.execute(clienteOrigen, clienteDestino);
		}*/
	}

}
