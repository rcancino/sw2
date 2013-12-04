package com.luxsoft.siipap.inventarios.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;

import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Utilerias relacionadas con la estructura de datos
 * para inventario
 * 
 * @author Ruben Cancino
 *
 */
public class InventarioDbUtils {
	
	/**
	 * Genera la vista global de movimientos de inventarios
	 * 
	 *  
	 */
	public static void updateInventarioGlobalView(){
		String res=(String)ServiceLocator2.getJdbcTemplate().execute(new ConnectionCallback(){

			public Object doInConnection(Connection con) throws SQLException,
					DataAccessException {
				StringBuffer buff=new StringBuffer();
				ResultSet rs=con.getMetaData().getTables(null, null, null, null);
				List<String> tables=new ArrayList<String>();
				while(rs.next()){
					String table=rs.getString("TABLE_NAME");
					if(table.toUpperCase().startsWith("SX_INVENT")){
						tables.add(table);
					}
						
				}
				Iterator<String> iter=tables.iterator();
				while(iter.hasNext()){
					buff.append("\nSELECT SUCURSAL_ID,FECHA,PRODUCTO_ID,CLAVE,DESCRIPCION,CANTIDAD,NACIONAL,UNIDAD_ID,COSTO,COSTOP,COMENTARIO,DOCUMENTO FROM "+iter.next());
					if(iter.hasNext())
						buff.append("\nUNION");
				}
				return buff.toString();
			}
			
		});
		res="CREATE VIEW V_INVENTARIO AS "+res;
		System.out.println(res);
		ServiceLocator2.getJdbcTemplate().update(res);
	}
	
	

}
