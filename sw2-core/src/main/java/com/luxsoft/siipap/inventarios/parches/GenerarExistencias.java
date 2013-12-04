package com.luxsoft.siipap.inventarios.parches;

import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;


public class GenerarExistencias {
	
	public static void generarExistencias(Long sucursalOrigen,Long sucursalDestino,int year,int mes){
		final String hql="from Existencia i where i.sucursal.id=? and i.year=? and i.mes=?";
		List<Existencia> origen=ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{sucursalOrigen,year,mes});
		System.out.println("Existencias a generar: "+origen.size()+ " Mes: "+origen.get(0).getMes());
		Sucursal sd=(Sucursal)ServiceLocator2.getHibernateTemplate().get(Sucursal.class, sucursalDestino);
		for(Existencia o:origen){
			Existencia e=new Existencia();
			e.setSucursal(sd);
			e.setProducto(o.getProducto());
			e.setCreateUser("ADMIN");
			e.setFecha(new Date());
			e.setYear(year);
			e.setMes(mes);
			System.out.println("Salvando: "+e);
			ServiceLocator2.getExistenciaDao().save(e);
		}
	}
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		generarExistencias(5L, 9L, 2011, 8);
	}

}
