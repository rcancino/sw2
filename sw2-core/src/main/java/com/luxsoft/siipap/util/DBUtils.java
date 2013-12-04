package com.luxsoft.siipap.util;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import com.luxsoft.siipap.service.ServiceLocator2;

public final class DBUtils {
	
	private static String DB_URL;
	
	public static synchronized String getAplicationDB_URL(){
		if(DB_URL==null){
			Object res=ServiceLocator2.getJdbcTemplate().execute(new ConnectionCallback(){
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
	
	
	public static synchronized String getAplicationDB_URL(JdbcTemplate template){
		String DB_URL="ND";
		Object res=template.execute(new ConnectionCallback(){
			public Object doInConnection(Connection con) throws SQLException,
					DataAccessException {
				return con.getMetaData().getURL();
			}
			
		});
		DB_URL=res!=null?StringUtils.substringBefore(res.toString(), "?"):"ERROR NO DB";
		DB_URL=StringUtils.substringAfter(DB_URL, "jdbc:");
		return DB_URL;
	}
	
	public static void whereWeAre(){
		System.out.println(getAplicationDB_URL());
	}
	
	public static void main(String[] args) {
		whereWeAre();
	}
	
	

}
