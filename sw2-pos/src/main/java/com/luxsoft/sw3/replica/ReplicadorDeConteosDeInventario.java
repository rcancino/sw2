package com.luxsoft.sw3.replica;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.inventarios.model.Conteo;
import com.luxsoft.siipap.inventarios.model.ExistenciaConteo;
import com.luxsoft.sw3.services.Services;

public class ReplicadorDeConteosDeInventario {
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	public void importarConteo(){
		
	}
	
	
	
	public void importarConteo(Long sucursalId){
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		List<String> pendientes=source.find(
				"select x.id from Conteo x where x.sucursal.id=?", sucursalId);
		for(String id:pendientes){
			Conteo c=(Conteo)source.get(Conteo.class, id);
			Services.getInstance().getHibernateTemplate().replicate(c, ReplicationMode.OVERWRITE);
			System.out.println("Conteo replicado: "+c.getId());
		}
	}
	
	public void importarExistenciasParaConteo(Long sucursalId){
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		List<String> pendientes=source.find(
				"select x.id from ExistenciaConteo x where x.sucursal.id=?", sucursalId);
		for(String id:pendientes){
			ExistenciaConteo c=(ExistenciaConteo)source.get(ExistenciaConteo.class, id);
			Services.getInstance().getHibernateTemplate().replicate(c, ReplicationMode.OVERWRITE);
			System.out.println("Existencia para conteo replicada: "+c.getId());
		}
	}
	
	public ReplicadorDeConteosDeInventario addSucursal(Long sucursalId){
		getSucursales().add(sucursalId);
		return this;
	}
	
	public Set<Long> getSucursales() {
		return sucursales;
	}


	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}


	public static void main(String[] args) {
		ReplicadorDeConteosDeInventario replicador=new ReplicadorDeConteosDeInventario();
		
		//replicador.importarExistenciasParaConteo(2L);		
		//replicador.importarConteo(2L);
	long[] sucursales ={2L,3L,5L,6L,9L};
		
		for ( int s=0 ; s<=sucursales.length-1 ; s++){
			
			//System.out.println(sucursales[s]);
			Long sucursal= sucursales[s];
			replicador.importarExistenciasParaConteo(sucursal);		
			replicador.importarConteo(sucursal);
			
				
		}
	}

}
