package com.luxsoft.sw3.tesoreria.migracion;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;


public class ImportadorDeComprasDeGastos {
	
	private List errores=new ArrayList();
	
	public JdbcTemplate getOrigenTemplate(){
		return ServiceLocator2.getAnalisisJdbcTemplate();
	}
	
	public JdbcTemplate getDestino(){
		return ServiceLocator2.getJdbcTemplate();
	}
	
	public void importar(){
		String sql="SELECT * FROM SW_GCOMPRA WHERE FECHA>=?";
		SqlParameterValue p1=new SqlParameterValue(Types.DATE,DateUtil.toDate("01/01/2010"));
		List<Map<String, Object>> rows=getOrigenTemplate().queryForList(sql, new Object[]{p1});
		for(Map<String,Object> row:rows){
			try {
				insertar(row);
			} catch (Exception e) {
				String msg=ExceptionUtils.getRootCauseMessage(e);
				System.out.println(msg);
				errores.add(row);
			}			
		}
		if(!errores.isEmpty()){
			System.out.println("\nErrores en la importacion:\n\n ");
			printErrores();
		}		
	}
	
	public void insertar(Map<String,Object> row){
		
		
		StringBuffer buf=new StringBuffer();		
		buf.append("INSERT INTO ( ");
		Iterator<String> iter=row.keySet().iterator();
		while (iter.hasNext()){
			buf.append(iter.next());
			if(iter.hasNext())
				buf.append(",");
		}
		buf.append(" )");
		System.out.println(buf.toString());
		Object params=new Object[row.values().size()];
		for(Entry<String,Object> entry:row.entrySet()){
			if(entry.getKey().equals("SUCURSAL_ID")){
				Long sucursalId=1L;
				Number valor=(Number)entry.getValue();
				switch (valor.intValue()) {
				case 206:
					sucursalId=1L;
					break;
				case 208:
					sucursalId=6L;
					break;
				case 209:
					sucursalId=5L;
					break;
				case 210:
					sucursalId=2L;
					break;
				case 212:
					sucursalId=8L;
					break;
				case 214:
					sucursalId=4L;
					break;
				case 96868:
					sucursalId=3L;
					break;
				case 200173:
					sucursalId=1L;
					break;
				case 327509:
					sucursalId=1L;
					break;
				default:
					break;
				}
			}
				
		}
		
	}
	
	public void printErrores(){
		for(Object o:errores){
			System.out.println(o);
		}
	}
	
	
	
	
	
	public static void main(String[] args) {
		new ImportadorDeComprasDeGastos().importar();
	}

}
