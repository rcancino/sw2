package com.luxsoft.sw3.reports;

import java.util.Map;

public interface ReportsManager {
	
	public void runReport(final String reportPath,final Map parameters);
	
	public void runReport(final String reportsLocation,final String reportPath,final Map parameters);
	
	/**
	 * Imprime un reporte directamente con la posiblidad de no presentar el previo de la impresion
	 * 
	 * @param location
	 * @param params
	 */
	public void printReport(String location,Map<String, Object> params,boolean printPreview);
	
	/**
	 * Imprime un reporte directamente con la posiblidad de no presentar el previo de la impresion
	 * @param reportsLocation
	 * @param reportPath
	 * @param parameters
	 * @param printPreview
	 */
	public void printReport(final String reportsLocation,final String reportPath,final Map parameters,boolean printPreview);

}
