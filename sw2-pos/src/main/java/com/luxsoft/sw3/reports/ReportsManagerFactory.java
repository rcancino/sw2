package com.luxsoft.sw3.reports;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Acceso a una instancia de {@link ReportsManagerFactory} para la ejecucion de reportes
 * utiliza {@link ServiceLoader} para la obtencion de un ReportManager si no lo encuentra
 * regresa una instancia de {@link DefaultReportManager}
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class ReportsManagerFactory {
	
	public static ReportsManager INSTANCE;
	private static Logger logger=Logger.getLogger(ReportsManagerFactory.class);
	
	public static ReportsManager getInstance(){
		if(INSTANCE==null){
			INSTANCE=findReportManager();
			if(INSTANCE==null){
				DefaultReportManager drm=new DefaultReportManager();
				POSDBUtils.whereWeAre();
				drm.setJdbcTemplate(Services.getInstance().getJdbcTemplate());
				INSTANCE=drm;
			}
		}
		return INSTANCE;
	}
	
	
	
	private static ReportsManager findReportManager(){
		try {
			ServiceLoader<ReportsManager> sl=ServiceLoader.load(ReportsManager.class);
			Iterator<ReportsManager> iter=sl.iterator();
			if(iter.hasNext())
				return iter.next();
			return null;
		} catch (Exception e) {
			logger.info("Error al tratar de localizar un ReportManager con ServiceLoader\n"+ExceptionUtils.getRootCauseMessage(e));
			return null;
		}
	}

}
