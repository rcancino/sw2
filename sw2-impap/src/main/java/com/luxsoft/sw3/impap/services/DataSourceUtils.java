package com.luxsoft.sw3.impap.services;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

/**
 * Utilerias para fuentes de datos externas
 * 
 * @author Ruben Cancino
 *
 */
public class DataSourceUtils {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private static DataSourceUtils INSTANCE;
	
	private DataSourceUtils() {
		
	}

	private BasicDataSource papelDataSource;
	
	public DataSource getPapelDataSource(){		
		
		if(papelDataSource==null){
			logger.debug("Creando un DataSource a PAPEL: ");
			papelDataSource=new BasicDataSource();
			papelDataSource.setDriverClassName("com.mysql.jdbc.Driver");
			papelDataSource.setUrl("jdbc:mysql://10.10.1.221/produccion");
			papelDataSource.setUsername("root");
			papelDataSource.setPassword("sys");
			
			
		}
		return papelDataSource;		
		
	}
	
	public void close() throws SQLException{
		if(papelDataSource!=null)
			papelDataSource.close();
	}
	
	public static DataSourceUtils getInstance(){
		if(INSTANCE==null){
			INSTANCE=new DataSourceUtils();
		}
		return INSTANCE;
	}

}
