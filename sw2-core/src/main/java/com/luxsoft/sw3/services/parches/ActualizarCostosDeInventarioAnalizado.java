package com.luxsoft.sw3.services.parches;

import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Actualiza los costos del inventario para todos los movimientos rlacionados con analisis
 * 
 * @author Ruben Cancino
 *
 */
public class ActualizarCostosDeInventarioAnalizado {
	
	
	
	
	public static void actualizarConAnalisisDeCompras(int year,int mes){
		
	}
	
	
	public static HibernateTemplate getTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}

}
