package com.luxsoft.siipap.ventas.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Facade para los servicios de DB y otros relacionados
 *  con las consultas de analisis de ventas
 *   
 * 
 * @author Ruben Cancino
 *
 */
public class AnalisisDeVentasManager {
	
	public static List<AnalisisDeVenta> buscarVentasPorSucursal(Integer year){
		String sql="SELECT LINEA,B.NOMBRE AS SUCURSAL,CASE WHEN SERIE=\'E\' THEN \'CRE\' ELSE \'CON\' END AS TIPO_VENTA" +
				",MES,SUM(IMP_PART) AS IMPORTE_BRUTO,SUM(IMP_PART) AS DESCUENTO, SUM(IMP_PART) AS UTILIDAD " +
				"FROM VENTASDET_@YEAR A JOIN SW_SUCURSALES B ON(A.SUCURSAL=B.CLAVE)" +
				" GROUP BY LINEA,B.NOMBRE,CASE WHEN SERIE=\'E\' THEN \'CRE\' ELSE \'CON\' END,MES";
		sql=sql.replace("@YEAR", String.valueOf(year));		
		return ServiceLocator2.getJdbcTemplate().query(sql,new RowMapper(){
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				AnalisisDeVenta a=new AnalisisDeVenta();
				a.setLinea(rs.getString("LINEA"));
				a.setSucursal(rs.getString("SUCURSAL"));
				a.setTipo(rs.getString("TIPO_VENTA"));
				a.setMes(rs.getInt("MES"));
				a.setVentaBruta(rs.getBigDecimal("IMPORTE_BRUTO").doubleValue());
				
				return a;
			}
			
		});
	}
	
	
	
	public static void main(String[] args) {
		List res=buscarVentasPorSucursal(2007);
		for(Object r:res){
			System.out.println(r);
		}
	}

}
