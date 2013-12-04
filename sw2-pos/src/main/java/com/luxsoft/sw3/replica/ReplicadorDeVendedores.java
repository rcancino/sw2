package com.luxsoft.sw3.replica;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.ventas.model.Vendedor;
import com.luxsoft.sw3.services.Services;

public class ReplicadorDeVendedores {
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	public void replciar(Long vendedorId){
		final Vendedor v=(Vendedor)Services.getInstance().getUniversalDao().get(Vendedor.class, vendedorId);
		for(Long sucursalId:getSucursales()){
			HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			target.replicate(v, ReplicationMode.OVERWRITE);
		}
	}
	
	public ReplicadorDeVendedores addSucursal(Long... sucursales){
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
		new ReplicadorDeVendedores()
		.addSucursal(2L,3L,5L,6L)
		.replciar(53L);
	}

}
