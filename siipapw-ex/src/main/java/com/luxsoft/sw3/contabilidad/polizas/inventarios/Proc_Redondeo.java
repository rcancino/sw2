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
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_Redondeo implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		Periodo periodo=Periodo.getPeriodoEnUnMes(poliza.getFecha());
		JdbcTemplate jdbcTemplate=(JdbcTemplate)model.get("jdbcTemplate");
		final String asiento="Redondeo";		
		String SQL=SQLUtils.loadSQLQueryFromResource("sql/contabilidad/PolizaDeInventario_Redondeo.sql");
		DateFormat df=new SimpleDateFormat("yyyy/MM/dd");
		SQL=SQL.replaceAll("@FECHA_INI", df.format(periodo.getFechaInicial()));
		SQL=SQL.replaceAll("@FECHA_FIN", df.format(periodo.getFechaFinal()));		
		List<Map<String, Object>> rows=jdbcTemplate.queryForList(SQL);
		System.out.println("SQL REDONDEO: "+SQL);
		
		BigDecimal totalAcumulado=BigDecimal.ZERO;
		
		for(Map<String ,Object> row:rows){
			String conceptoClave=(String)row.get("CONCEPTO");
			Number valorNumerico=(Number)row.get("COSTO");
			final BigDecimal total=BigDecimal.valueOf(valorNumerico.doubleValue());
			totalAcumulado=totalAcumulado.add(total);
			Number sucursalID=(Number)row.get("SUCURSAL_ID");
			Sucursal sucursal=(Sucursal)ServiceLocator2.getUniversalDao().get(Sucursal.class,sucursalID.longValue());
			String descripcion2=(String)row.get("DESCRIPCION");
			String ref2= sucursal!=null?sucursal.getNombre():" "+sucursalID;
		
			if("IRED01".equals(conceptoClave) ){
				boolean cargo=true;
				if(total.doubleValue()>0){
					cargo=false;
				}
				
				//Se sustituye conceptoClave de IRED01 a INVF01
				PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", cargo, total.abs(), descripcion2, "",ref2, asiento);
				
				
			}		
		}
		
		if(totalAcumulado.doubleValue()>0){
			PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", true, totalAcumulado.abs(), "Neto de diferencias", "","CALLE4", asiento);
		}else{
			PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", false, totalAcumulado.abs(), "Neto de diferencias", "","CALLE4", asiento);
		}
		
		
	}
}
