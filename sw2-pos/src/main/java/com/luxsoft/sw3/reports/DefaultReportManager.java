package com.luxsoft.sw3.reports;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.view.JasperViewer;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import com.luxsoft.siipap.swing.utils.MessageUtils;

/**
 * Implementacion de ReportsManager que funciona correctamente de manera autonoma
 * en caso de ser colocado en un Spring context implementa {@link ApplicationContextAware}
 * y utiliza el contexto como {@link ResourceLoader} para obtener una instancia de {@link Resource}
 * para el reporte a ejectuar.   
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class DefaultReportManager implements ReportsManager,ApplicationContextAware{
	
	
	private Logger logger=Logger.getLogger(getClass());
	
	public static final String REPORTS_LOCATION_KEY="sw3.reports.path";
	public static final String DEFAULT_REPORTS_LOCATION="file:z:/Reportes_MySQL/";
	
	private ResourceLoader resourceLoader;
	private JdbcTemplate jdbcTemplate;
	
	private String reportsLocation;
	
	public DefaultReportManager(){
		resourceLoader=new DefaultResourceLoader();
		reportsLocation=System.getProperty(REPORTS_LOCATION_KEY, DEFAULT_REPORTS_LOCATION);
	}
	
	protected InputStream getInputStream(String reportsDir,String location) throws IOException{
		final Resource res=resourceLoader.getResource(reportsDir+location);		
		return res.getInputStream();
	}
	
	
	public void runReport(final String reportPath,final Map parameters){
		runReport(getReportsLocation(),reportPath, parameters);
	}
	
	public void runReport(final String reportsLocation,final String reportPath,final Map parameters){
		
		JasperPrint jp=(JasperPrint)getJdbcTemplate().execute(new ConnectionCallback(){
			public Object doInConnection(Connection con) throws SQLException,DataAccessException {
				try {
					logger.info("Ejecutando reporte: "+reportPath+ "con parametros: "+parameters);
					
					final InputStream is=getInputStream(reportsLocation,reportPath);
					return JasperFillManager.fillReport(is, parameters,con);
				} catch (JRException e) {
					logger.error("Error en JasperEngine", e);
				} catch (IOException e) {
					logger.error("Error en acceso a recursos InputStream",e);
				}
				return null;
			}			
		});		
		if(jp!=null){
			JasperViewer view=new JasperViewer(jp,false) ;
			
			view.setVisible(true);
			view.toFront();
			//view.setAlwaysOnTop(true);
		}
	}
	
	public void printReport(final String reportsLocation,final String reportPath,final Map parameters,boolean printPreview){
		printReport(getReportsLocation(),reportPath, parameters, printPreview);
	}
	
	/**
	 * Imprime un reporte directamente con la posiblidad de no presentar el previo de la impresion
	 * 
	 * @param location
	 * @param params
	 */
	public void printReport(final String reportPath,final Map<String, Object> parameters,boolean printPreview){
		JasperPrint jp=(JasperPrint)getJdbcTemplate().execute(new ConnectionCallback(){
			public Object doInConnection(Connection con) throws SQLException,DataAccessException {
				try {
					logger.info("Ejecutando reporte: "+reportPath+ "con parametros: "+parameters);
					
					final InputStream is=getInputStream(reportsLocation,reportPath);
					return JasperFillManager.fillReport(is, parameters,con);
				} catch (JRException e) {
					logger.error("Error en JasperEngine", e);
				} catch (IOException e) {
					logger.error("Error en acceso a recursos InputStream",e);
				}
				return null;
			}			
		});	
		try {
			JasperPrintManager.printReport(jp, printPreview);
		} catch (JRException e) {			
			MessageUtils.showError("Error imprimiento reporte", e);
			e.printStackTrace();
		}
	}
	

	public String getReportsLocation() {
		return reportsLocation;
	}

	public void setReportsLocation(String reportsDir) {
		this.reportsLocation = reportsDir;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}


	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		setResourceLoader(applicationContext);		
	}
	
	

}
