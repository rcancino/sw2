package com.luxsoft.sw3.services;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;

public final class POSDBUtils {
	
	private static String DB_URL;
	
	public static synchronized String getAplicationDB_URL(){
		if(DB_URL==null){
			Object res=Services.getInstance().getJdbcTemplate().execute(new ConnectionCallback(){
				public Object doInConnection(Connection con) throws SQLException,
						DataAccessException {
					return con.getMetaData().getURL();
				}
				
			});
			DB_URL=res!=null?StringUtils.substringBefore(res.toString(), "?"):"ND";
			DB_URL=StringUtils.substringAfter(DB_URL, "jdbc:");
		}
		return DB_URL;
	}
	
	
	public static void whereWeAre(){
		System.out.println("POSDBUtils: "+getAplicationDB_URL());
		
	}
	
	
	public static DataSource createSiipapDataSource(){
		BasicDataSource ds=new BasicDataSource();
		ds.setDriverClassName("sun.jdbc.odbc.JdbcOdbcDriver");
		ds.setUrl("jdbc:odbc:SIIPAP");
		return ds;
	}
	
	
	public static void main(String[] args) {
		whereWeAre();
	}
	

}
