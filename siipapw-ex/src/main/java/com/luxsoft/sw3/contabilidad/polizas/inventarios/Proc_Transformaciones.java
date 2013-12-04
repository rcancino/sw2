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

public class Proc_Transformaciones implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		Periodo periodo=Periodo.getPeriodoEnUnMes(poliza.getFecha());
		JdbcTemplate jdbcTemplate=(JdbcTemplate)model.get("jdbcTemplate");
		final String asiento="Gasto por transformaciones";		
		String SQL=" SELECT 'GASTO TRANSFORMACIONES' AS TIPO,IC.SUCURSAL_ID,S.NOMBRE AS SUCURSAL,A.NOMBRE AS PROVEEDOR"
				+",IFNULL(ROUND(SUM((IC.CANTIDAD/IC.FACTORU*IC.GASTOS)),2),0) AS TOTAL" 
				+" FROM sx_inventario_trs IC " 
				+" JOIN sx_transformaciones D ON(D.TRANSFORMACION_ID=IC.TRANSFORMACION_ID) " 
				+" JOIN sw_sucursales S ON(S.SUCURSAL_ID=IC.SUCURSAL_ID) "
				+" JOIN sx_analisis_trs A ON(A.ANALISIS_ID=IC.ANALISIS_ID) "	// Se agrego para PROVEEDOR
				+" WHERE  IC.FECHA BETWEEN \'@FECHA_INI\' AND \'@FECHA_FIN\'" 
				+" GROUP BY IC.SUCURSAL_ID;";
		DateFormat df=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.0");
		SQL=SQL.replaceAll("@FECHA_INI", df.format(periodo.getFechaInicial()));
		SQL=SQL.replaceAll("@FECHA_FIN", df.format(periodo.getFechaFinal()));		
		List<Map<String, Object>> rows=jdbcTemplate.queryForList(SQL);
		
		for(Map<String ,Object> row:rows){
			Number valorNumerico=(Number)row.get("TOTAL");
			final BigDecimal total=BigDecimal.valueOf(valorNumerico.doubleValue());
			String sucursal=(String)row.get("SUCURSAL");
			String proveedor=(String)row.get("PROVEEDOR");
			//Cargo
			PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", true, total, "Gastos por TRS", proveedor, sucursal, asiento);
						
			//Abono
			PolizaDetFactory.generarPolizaDet(poliza, "119", "INVT01", false, total, "Gastos por TRS", proveedor, sucursal, asiento);
		}
		
	}
}
