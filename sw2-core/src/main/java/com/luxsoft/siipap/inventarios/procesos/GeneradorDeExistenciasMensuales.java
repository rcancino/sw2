package com.luxsoft.siipap.inventarios.procesos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;

/**
 * Proceso para generar todos los registros de exitencias requeridos
 * para operar en un mes
 * 
 * TODO Mover al  InventariosManager del POS
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class GeneradorDeExistenciasMensuales {
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	public GeneradorDeExistenciasMensuales addSucursales(Long...sucs){
		for(Long l:sucs)
			sucursales.add(l);
		return this;
	}
	
	
	/**
	 * Genera los registros de existencias para el año y mes indicado
	 * 
	 * @param year El año desado
	 * @param mes  El mes desado Ej Marzo=3
	 */
	public void execute(int year, int mes){
		Periodo per=Periodo.getPeriodoEnUnMes(mes-1, year);
		System.out.println("Periodo: "+per);
		String sql="SELECT DISTINCT CLAVE FROM SX_EXISTENCIAS WHERE YEAR=?";
		List<String> claves=ServiceLocator2.getJdbcTemplate().queryForList(sql, new Object[]{year},String.class);
		
		for(String clave:claves){
			for(Long sucursalId:sucursales){
				ServiceLocator2.getExistenciaDao()
					.generar(clave, per.getFechaInicial(), sucursalId);
			}
			System.out.println("Clave generada: "+clave);
		}
		
		
		
	}
	
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		new GeneradorDeExistenciasMensuales()
		.addSucursales(2L,3L,5L,6L)
		.execute(2010, 3);
	}

}
