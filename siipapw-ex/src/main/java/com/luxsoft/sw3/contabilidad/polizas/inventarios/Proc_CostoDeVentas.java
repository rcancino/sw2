package com.luxsoft.sw3.contabilidad.polizas.inventarios;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_CostoDeVentas implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		final String asiento="Costo de ventas";	
		
		Periodo periodo=Periodo.getPeriodoEnUnMes(poliza.getFecha());
		JdbcTemplate jdbcTemplate=(JdbcTemplate)model.get("jdbcTemplate");
		
		String SQL=" SELECT X.TIPO,X.SUCURSAL_ID,X.SUCURSAL,SUM(TOTAL) AS TOTAL FROM ( " +
			" SELECT 'COSTOVTA' AS TIPO,IC.SUCURSAL_ID,S.NOMBRE AS SUCURSAL,ROUND(SUM((IFNULL(-IC.CANTIDAD/IC.FACTORU*IC.COSTOP,0))),2) AS TOTAL  " +
			" FROM sx_ventasdet IC use index (INDX_VDET2) JOIN sw_sucursales S ON(S.SUCURSAL_ID=IC.SUCURSAL_ID) " +
			" WHERE  IC.FECHA BETWEEN \'@FECHA_INI\' AND \'@FECHA_FIN\' GROUP BY IC.SUCURSAL_ID " +
			" UNION " +
			" SELECT 'COSTOVTA' AS TIPO,IC.SUCURSAL_ID,S.NOMBRE AS SUCURSAL,ROUND(SUM((-IC.CANTIDAD/IC.FACTORU*IC.COSTOP)),2) AS TOTAL  " +
			" FROM sx_inventario_dev IC JOIN sw_sucursales S ON(S.SUCURSAL_ID=IC.SUCURSAL_ID) " +
			" WHERE  IC.FECHA BETWEEN \'@FECHA_INI\' AND \'@FECHA_FIN\' " +
			" GROUP BY IC.SUCURSAL_ID ) AS X GROUP BY X.TIPO,X.SUCURSAL_ID,X.SUCURSAL ";
			
			DateFormat df=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.0");
			SQL=SQL.replaceAll("@FECHA_INI", df.format(periodo.getFechaInicial()));
			DateFormat df1=new SimpleDateFormat("yyyy/MM/dd 23:59:59.0");
			SQL=SQL.replaceAll("@FECHA_FIN", df1.format(periodo.getFechaFinal()));
			//System.out.println("SQL: "+SQL);
			List<Map<String, Object>> rows=jdbcTemplate.queryForList(SQL);
			
			for(Map<String ,Object> row:rows){
				Number valorNumerico=(Number)row.get("TOTAL");
				final BigDecimal total=BigDecimal.valueOf(valorNumerico.doubleValue());
				String sucursal=(String)row.get("SUCURSAL");
				
				//Abono a Inventario por Proveedor
				PolizaDetFactory.generarPolizaDet(poliza, "501", "CVTA01", true, total, "Aplicación de costo de ventas", "", sucursal, asiento);
				
				PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", false, total, "Costo de ventas", "", sucursal, asiento);
							
			}
	}
}
