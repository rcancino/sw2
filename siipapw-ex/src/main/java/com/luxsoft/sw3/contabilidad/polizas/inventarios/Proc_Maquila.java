package com.luxsoft.sw3.contabilidad.polizas.inventarios;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_Maquila implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		Periodo periodo=Periodo.getPeriodoEnUnMes(poliza.getFecha());
		JdbcTemplate jdbcTemplate=(JdbcTemplate)model.get("jdbcTemplate");
		final String asiento="Maquila";		
		String SQL=SQLUtils.loadSQLQueryFromResource("sql/contabilidad/PolizaDeInventario_Maquila.sql");
		DateFormat df=new SimpleDateFormat("yyyy/MM/dd");
		SQL=SQL.replaceAll("@FECHA_INI", df.format(periodo.getFechaInicial()));
		SQL=SQL.replaceAll("@FECHA_FIN", df.format(periodo.getFechaFinal()));		
		List<Map<String, Object>> rows=jdbcTemplate.queryForList(SQL);
		System.out.println(" Maquila: "+SQL);
		
		for(Map<String ,Object> row:rows){
			
			//String conceptoClave=(String)row.get("CONCEPTO");
			String descripcion2=(String)row.get("DESCRIPCION").toString();
			Number valorNumerico=(Number)row.get("COSTO");
			final BigDecimal total=BigDecimal.valueOf(valorNumerico.doubleValue());
			
			//Number sucursalID=(Number)row.get("SUCURSAL_ID");
			String sucursal=(String)row.get("SUCURSAL");			
			String almacen=(String)row.get("ALMACEN");
			Number clave=(Number)row.get("CLAVE");
			almacen=StringUtils.trimToEmpty(almacen);
			
			PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", true, total.abs(), descripcion2+" MATERIA PRIMA", almacen, sucursal, asiento);
			
			PolizaDetFactory.generarPolizaDet(poliza, "119", clave.toString(), false, total.abs(), descripcion2+" MATERIA PRIMA", almacen, sucursal, asiento);
			
			
			final BigDecimal totalCorte=BigDecimal.valueOf( ((Number)row.get("CORTE")).doubleValue() );
			
			PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", true, totalCorte.abs(), descripcion2+" CORTE", almacen, sucursal, asiento);
			
			PolizaDetFactory.generarPolizaDet(poliza, "119", clave.toString(), false, totalCorte.abs(), descripcion2+" CORTE", almacen, sucursal, asiento);
			
			
			final BigDecimal totalFlete=BigDecimal.valueOf( ((Number)row.get("FLETE")).doubleValue() );
			
			PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", true, totalFlete.abs(), descripcion2+" FLETE", almacen, sucursal, asiento);
			
			PolizaDetFactory.generarPolizaDet(poliza, "119", clave.toString(), false, totalFlete.abs(), descripcion2+" FLETE", almacen, sucursal, asiento);
			
			
		}
		
		
	}
}
