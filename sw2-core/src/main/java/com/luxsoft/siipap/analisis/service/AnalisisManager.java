package com.luxsoft.siipap.analisis.service;

import java.math.BigDecimal;

import com.luxsoft.siipap.analisis.model.AnalisisFV;
import com.luxsoft.siipap.model.Periodo;

public interface AnalisisManager {
	
	/**
	 * Genera una analisis de ventas financiero global
	 * para el YTD
	 * 
	 * @return
	 */
	public AnalisisFV generarAnalisisGlobalYTD();
	
	/**
	 * Regresa el monto de la provision historica para un
	 * periodo de ventas.   
	 * 
	 * @param mes
	 * @return
	 */
	public BigDecimal obtnerProvisionHistorica(final Periodo mes);

}
