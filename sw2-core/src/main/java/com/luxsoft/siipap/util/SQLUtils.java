package com.luxsoft.siipap.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.luxsoft.siipap.service.ServiceLocator2;


public class SQLUtils {
	
	private static Logger logger=Logger.getLogger(SQLUtils.class);
	
	public static String loadSQLQueryFromResource(final String path) {
		try{
			InputStream io=SQLUtils.class
			.getClassLoader()
			.getResourceAsStream(path);
			Assert.notNull(io,"El recurso no existe: "+path);
			BufferedReader r=new BufferedReader(new InputStreamReader(io));
			String sql="";	
			String line;
			while ((line = r.readLine())!=null){
				if(line!=null){
					sql+=line+"\n";
				}		
			}	
			r.close();			
			return sql;
		}catch (Exception e) {
			logger.error(e);
			String msg=MessageFormat.format("Imposible cargar el query {0} " +
					"error: {1}", path,e.getMessage());
			throw new RuntimeException(msg,e);
		}
		
	}
	
	
	@SuppressWarnings("unchecked")
	public static void printColumnNames(final String sql){
		JdbcTemplate tm=ServiceLocator2.getJdbcTemplate();
		tm.setMaxRows(1);
		
		List<Map<String, Object>> rows=tm.queryForList(sql);
		StringBuffer buffer=new StringBuffer();
		for(Map<String, Object> row:rows){
			Set<String> columnas=row.keySet();
			
			buffer.append("{");
			for(String s:columnas){
				buffer.append("\"");				
				buffer.append(s);
				buffer.append("\"");
				buffer.append(",");
			}
			buffer.append("}");
		}
		System.out.println(buffer);
		buffer=new StringBuffer();
		for(Map<String, Object> row:rows){
			Set<String> columnas=row.keySet();
			
			buffer.append("{");
			for(String s:columnas){
				//buffer.append("\"");
				Object val=row.get(s);
				Class clazz=val!=null?val.getClass():String.class;
				if(clazz==Timestamp.class)
					clazz=Date.class;
				buffer.append(ClassUtils.getShortName(clazz)+".class");
				//buffer.append("\"");
				buffer.append(",");
			}
			buffer.append("}");
		}
		System.out.println(buffer);
	}
	
	public static void printBeanClasFromSQL(final String sql){
		printBeanClasFromSQL(sql, false);
	}
	
	/**
	 * Genera un JavaBean a partir de una clase
	 * 
	 * @param sql
	 */
	public static void printBeanClasFromSQL(final String sql,boolean uncapitalize){
		JdbcTemplate tm=ServiceLocator2.getJdbcTemplate();
		tm.setMaxRows(1);
		
		List<Map<String, Object>> rows=tm.queryForList(sql);
		StringBuffer buffer=new StringBuffer();
		for(Map<String, Object> row:rows){
			Set<String> columnas=row.keySet();
			
			for(String s:columnas){
				
				Object val=row.get(s);
				Class clazz=val!=null?val.getClass():String.class;
				if(clazz==Timestamp.class)
					clazz=Date.class;
				if(clazz==Double.class)
					clazz=double.class;
				if(clazz==Long.class)
					clazz=long.class;
				if(clazz==Integer.class)
					clazz=int.class;
				String pattern="private {0} {1};";
				if(uncapitalize){
					s=s.toLowerCase();
				}
				buffer.append(MessageFormat.format(pattern
						,ClassUtils.getShortName(clazz),s));
				buffer.append("\n");
			}
			
		}
		System.out.println(buffer);
	}
	
	public static void printBeanClasFromSQL(final String sql,Object[] params){
		JdbcTemplate tm=ServiceLocator2.getJdbcTemplate();
		tm.setMaxRows(1);
		
		List<Map<String, Object>> rows=tm.queryForList(sql,params);
		StringBuffer buffer=new StringBuffer();
		for(Map<String, Object> row:rows){
			Set<String> columnas=row.keySet();
			
			for(String s:columnas){
				
				Object val=row.get(s);
				Class clazz=val!=null?val.getClass():String.class;
				if(clazz==Timestamp.class)
					clazz=Date.class;
				if(clazz==Double.class)
					clazz=double.class;
				String pattern="private {0} {1}";
				buffer.append(MessageFormat.format(pattern
						,ClassUtils.getShortName(clazz),s));
				buffer.append("\n");
			}
			
		}
		System.out.println(buffer);
	}
	
	public static void printBeanPropertiesSQL(final String sql,boolean capitalize){
		JdbcTemplate tm=ServiceLocator2.getJdbcTemplate();
		tm.setMaxRows(1);
		
		List<Map<String, Object>> rows=tm.queryForList(sql);
		StringBuffer buffer=new StringBuffer();
		buffer.append("{\n");
		for(Map<String, Object> row:rows){
			Set<String> columnas=row.keySet();
			for(Iterator<String> it=columnas.iterator();it.hasNext();){
				String property=it.next();
				if(capitalize)
					property=StringUtils.capitalize(property.toLowerCase());
				buffer.append("\""+property+"\"");
				if(it.hasNext())
					buffer.append(",\n");
				else
					buffer.append("\n};");
			}
			
		}
		System.out.println(buffer);
	}
	
	public static void main(String[] args) {
		/*String sql=loadSQLQueryFromResource("sql/costoDeVentas.sql");		
		sql=sql.replaceAll("@INI_YEAR", "2009");
		sql=sql.replaceAll("@INI_MES", "4");
		sql=sql.replaceAll("@YEAR", "2009");
		sql=sql.replaceAll("@MES", "5");
		printColumnNames(sql);*/
		
		//System.out.println(sql);
		printBeanClasFromSQL("SELECT b.CARGOABONO_ID,B.fecha,B.REFERENCIA,B.CUENTA_ID,B.FORMAPAGO,C.PROVEEDOR_ID,C.NOMBRE,B.IMPORTE,C.CXP_ID,C.DOCUMENTO,C.FECHA,C.MONEDA,C.TC,C.TOTAL,C.IMPORTE,C.IMPUESTO,C.FLETE,C.FLETE_IVA,C.FLETE_RET,(C.TOTAL*C.TC) AS TOT_MN FROM SX_CXP C  JOIN sw_trequisiciondet D ON(C.CXP_ID=D.CXP_ID) JOIN sw_trequisicion T ON(T.REQUISICION_ID=D.REQUISICION_ID) JOIN sw_bcargoabono B ON(T.CARGOABONO_ID=B.CARGOABONO_ID)");
		
	}
	

}
