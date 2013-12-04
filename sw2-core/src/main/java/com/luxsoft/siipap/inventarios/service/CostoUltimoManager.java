package com.luxsoft.siipap.inventarios.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Centraliza el mantenimiento de costos tomando como base
 * la ultima compra  
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CostoUltimoManager {
	
	
	private Logger logger=Logger.getLogger(getClass());
	
	private CostoPromedioManager costoPromedioManager;
	
	private JdbcTemplate jdbcTemplate;
	
	/**
	 * Actualiza el campo de costo ultimo en la tabla de costos promedios
	 * SX_COSTO_P
	 *  
	 *  Al final del proceso actualiza las costo de las exitencias
	 *   
	 * @param year
	 * @param mes
	 */
	public void actualizarCosto(final int year, int mes){
		logger.info("Actualizando el ultimo costo en el inventario.   Periodo: "+year+ " - "+mes);
		List<CostoPromedio> costosp=getCostoPromedioManager().buscarCostosPromedios(year, mes);
		Periodo per=Periodo.getPeriodoEnUnMes(mes-1, year);
		for(CostoPromedio cp:costosp){
			BigDecimal costo=buscarUltimoCosto(cp.getClave(), per.getFechaFinal());
			if(costo==null) costo=BigDecimal.ZERO;
			if(costo.doubleValue()==0){
				costo=buscarUltimoCostoEnOracle(cp.getClave());
			}
			cp.setCostoUltimo(costo);
			cp=costoPromedioManager.save(cp);
			if(logger.isDebugEnabled()){
				logger.debug("Costo ultimo acutalizado: "+cp);
			}
		}
	}
	
	/**
	 * Busca el ultimo costo del producto y lo persiste en la tabla de costos promedios
	 * 
	 * @param clave
	 * @param fecha
	 * @return
	 */
	public BigDecimal  buscarUltimoCosto(final String clave,final Date fecha){
		
		Map<String, Object> row=CostosUtils.buscarUltimoCosto(clave, fecha);
		if(row==null ) return BigDecimal.ZERO;
		Number res=(Number)row.get("COSTO");
		return BigDecimal.valueOf(res.doubleValue());
	}
	
	/**
	 * Busca el ultimo costo registrado en oracle
	 * 
	 * @param clave
	 * @return
	 */
	public BigDecimal buscarUltimoCostoEnOracle(final String clave){
		Map<String, Object> row=CostosUtils.buscarUltimoCostoEnOracle(clave);
		if(row==null) return BigDecimal.ZERO;
		Number res=(Number)row.get("NETO");
		return BigDecimal.valueOf(res.doubleValue());
	}
	
	
	

	public CostoPromedioManager getCostoPromedioManager() {
		return costoPromedioManager;
	}

	public void setCostoPromedioManager(CostoPromedioManager costoPromedioManager) {
		this.costoPromedioManager = costoPromedioManager;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public static void main(String[] args) {
		ServiceLocator2.getCostosServices().getCostoUltimoManager().actualizarCosto(2009, 7);
	}
	

}
