package com.luxsoft.sw3.services.parches;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.persister.entity.AbstractEntityPersister;

import com.luxsoft.siipap.service.ServiceLocator2;

public class AfinacionBaseDeDatosDetectorDeTablas {
	
	
	
	public static void main(String[] args) {
		System.setProperty("jdbc.url", "jdbc:mysql://10.10.1.228/produccion");
		System.setProperty("sucursalOrigen", "OFICINAS");
		List<String> tablasTotales=ServiceLocator2.getJdbcTemplate()
				.queryForList("SELECT table_name FROM INFORMATION_SCHEMA.Tables WHERE table_schema =?",new Object[]{"produccion"}, String.class);
		System.out.println("Tablas totales: "+tablasTotales.size());
		Map map=ServiceLocator2.getHibernateTemplate().getSessionFactory().getAllClassMetadata();
		List<String> tablasHibernate=new ArrayList<String>();
		for(Object o:map.entrySet()){
			Map.Entry entry=(Map.Entry)o;
			AbstractEntityPersister ap=(AbstractEntityPersister)entry.getValue();
			
			//System.out.println(ap.getName()+ " - "+ap.getTableName());
			//System.out.println(entry.getKey().getClass().getName()+ " Data: "+entry.getValue().getClass().getName() );
			tablasHibernate.add(ap.getTableName().toLowerCase());
		}
		System.out.println("Tablas Hibernate: "+tablasHibernate.size());
		List<String> paraEliminar=new ArrayList<String>();//ListUtils.subtract(tablasTotales, tablasHibernate);
		for(String tabla:tablasTotales){
			if(tabla.equalsIgnoreCase("sx_ventas")){
				System.out.println("Debuf...");
			}
			if(!tablasHibernate.contains(tabla.toLowerCase())){
				paraEliminar.add(tabla);
			}
		}
		System.out.println("Tablas por eliminar: "+paraEliminar.size());
		for(String s:paraEliminar){
			System.out.println("drop table "+s+";");
		}
	}

}
