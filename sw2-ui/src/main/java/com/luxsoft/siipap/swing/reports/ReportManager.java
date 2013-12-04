package com.luxsoft.siipap.swing.reports;

import java.util.Map;

import javax.swing.JComponent;

import org.springframework.core.io.Resource;

/**
 * Inerfaz general del administrador de reportes;
 * 
 * @author Ruben Cancino
 *
 */
public interface ReportManager {
	
	/**
	 * Ejecuta un reporte refernciado por un Resource, normalmente
	 * un archivo
	 * 	  
	 * @param resource
	 * @param params
	 */
	public JComponent execute(Resource resource,Map<String, Object> params);
	
	/**
	 * Ejecuta un reporte tratando de resolver la ruta especificada
	 * @param resourcePath
	 * @param params
	 */
	public JComponent execute(String resourcePath,Map<String, Object> params);
	
	/**
	 * Imprime un reporte directamente a la impresora
	 * 
	 * @param location
	 * @param params
	 */
	public void printReport(String location,Map<String, Object> params,boolean printPreview);

}
