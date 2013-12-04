package com.luxsoft.siipap.util;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlWriter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import com.luxsoft.siipap.service.ServiceLocator2;

public final class DBUnitUtils {
	
	
	public static void exportDataSet(JdbcTemplate source,final String targetPath,final QueryDataSetCallback callback) throws Exception {
		source.execute(new ConnectionCallback(){

			public Object doInConnection(Connection con) throws SQLException,DataAccessException {
				
				IDatabaseConnection icon;
				try {
					icon = new DatabaseConnection(con);
					QueryDataSet dataSet=new QueryDataSet(icon);
					callback.addData(dataSet);
					
					FileOutputStream out=new FileOutputStream(targetPath);
					FlatXmlWriter writer=new FlatXmlWriter(out);
					writer.write(dataSet);
				} catch (Exception  e) {
					throw new RuntimeException("Error al exportar DataSet",e);
				}
				
				return null;
			}
			
		});
		
	}
	
	
	/**
	 * Callback interface para personalizar las tablas/query DataSet 
	 * a extraer
	 * 
	 * @author RUBEN
	 *
	 */
	public static interface QueryDataSetCallback{
		
		public void addData(QueryDataSet dataSet);
		
	}
	
	public static void main(String[] args) throws Exception{
		exportDataSet(ServiceLocator2.getJdbcTemplate(),"src/test/resources/dbunit/entradas_com.xml",new QueryDataSetCallback(){
			public void addData(QueryDataSet dataSet) {
				dataSet.addTable("SX_INVENTARIO_COM", "select * from sx_inventario_com where month(fecha)=1 and year(fecha)=2008" );
				
			}
		});
	}

}
