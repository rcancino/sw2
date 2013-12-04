package com.luxsoft.sw3.contabilidad.polizas.inventarios;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_Movimientos implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		Periodo periodo=Periodo.getPeriodoEnUnMes(poliza.getFecha());
		JdbcTemplate jdbcTemplate=(JdbcTemplate)model.get("jdbcTemplate");
		final String asiento="Movimientos genericos";		
		String SQL=SQLUtils.loadSQLQueryFromResource("sql/contabilidad/PolizaDeInventario_Genericos.sql");
		DateFormat df=new SimpleDateFormat("yyyy/MM/dd");
		SQL=SQL.replaceAll("@FECHA_INI", df.format(periodo.getFechaInicial()));
		SQL=SQL.replaceAll("@FECHA_FIN", df.format(periodo.getFechaFinal()));		
		List<Map<String, Object>> rows=jdbcTemplate.queryForList(SQL);
		//System.out.println("SQL: "+SQL);
		for(Map<String ,Object> row:rows){
			
			String conceptoClave=(String)row.get("CONCEPTO");
			String descripcion2=(String)row.get("DESCRIPCION");
			Number valorNumerico=(Number)row.get("COSTO");
			final BigDecimal total=BigDecimal.valueOf(valorNumerico.doubleValue());
			
			Number sucursalID=(Number)row.get("SUCURSAL_ID");
			Sucursal sucursal=(Sucursal)ServiceLocator2.getUniversalDao().get(Sucursal.class, sucursalID.longValue());
			String ref2= sucursal!=null?sucursal.getNombre():" "+sucursalID;
		
			if("INVF01".equals(conceptoClave) ){
				boolean cargo=true;
				if(total.doubleValue()<0){
					cargo=false;
				}
				PolizaDetFactory.generarPolizaDet(poliza, "119", conceptoClave, cargo, total.abs(), descripcion2, "",ref2, asiento);
				
			}else if("OING01".equals(conceptoClave)){				
				
				PolizaDetFactory.generarPolizaDet(poliza, "702", conceptoClave, false, total.abs(), descripcion2, "", ref2, asiento);
				
				PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", true, total.abs(), descripcion2, "", ref2, asiento);
				
			}else if("OGST01".equals(conceptoClave)){				
				
				PolizaDetFactory.generarPolizaDet(poliza, "704", conceptoClave, true, total.abs(), descripcion2, "", ref2, asiento);
				
				PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", false, total.abs(), descripcion2, "", ref2, asiento);
				
				
			}else if("151654".equals(conceptoClave)){
				
				PolizaDetFactory.generarPolizaDet(poliza, "600", conceptoClave, true, total.abs(), descripcion2, "", ref2, asiento);
				
				PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", false, total.abs(), descripcion2, "", ref2, asiento);
								
			}else if("151656".equals(conceptoClave)){
				PolizaDetFactory.generarPolizaDet(poliza, "600", conceptoClave, true, total.abs(), descripcion2, "", ref2, asiento);
				
				PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", false, total.abs(), descripcion2, "", ref2, asiento);
				
			}else if("151530".equals(conceptoClave)){
				PolizaDetFactory.generarPolizaDet(poliza, "600", conceptoClave, true, total.abs(), descripcion2, "", ref2, asiento);
				
				PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", false, total.abs(), descripcion2, "", ref2, asiento);
				
			}else if("151688".equals(conceptoClave)){
				
				PolizaDetFactory.generarPolizaDet(poliza, "600", conceptoClave, true, total.abs(), descripcion2, "", ref2, asiento);				
				PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", false, total.abs(), descripcion2, "", ref2, asiento);
				
			}			
		}
		
	}
}
