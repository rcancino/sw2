package com.luxsoft.siipap.inventarios.procesos;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;

/**
 * Tarea para generar entidades de existencia para los articulos activos
 * 
 * @author pato
 *
 */
public class ActualizadorDeExistencias {
	
	/**
	 * TODO: Se requiere incluir mas productos inactivos pero con existencia o movimientos ???
	 * 
	 */
	public void generarExistencias(){
		
		String hql="from Producto p where p.activo=? and p.inventariable=? order by p.clave desc " ;	
		Sucursal sucursal=ServiceLocator2.getConfiguracion().getSucursal();
		List<Producto> productos=ServiceLocator2.getHibernateTemplate().find(hql, new Object[]{Boolean.TRUE,Boolean.TRUE});
		for(Producto p:productos){
			try {
				ServiceLocator2.getInventarioManager().generarExistenciaDeProducto(sucursal,p);
				
			} catch (Exception e) {
				String err=ExceptionUtils.getRootCauseMessage(e);
				System.out.println("Clave: "+p.getClave()+ "Error:\n\t:"+err);
			}
			
		}
		
	}
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		ActualizadorDeExistencias task=new ActualizadorDeExistencias();
		task.generarExistencias();
	}

}
