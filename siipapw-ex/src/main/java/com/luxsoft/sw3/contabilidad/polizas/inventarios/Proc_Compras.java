package com.luxsoft.sw3.contabilidad.polizas.inventarios;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_Compras implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		final String asiento="COMPRAS";
		Periodo periodo=Periodo.getPeriodoEnUnMes(poliza.getFecha());
		JdbcTemplate jdbcTemplate=(JdbcTemplate)model.get("jdbcTemplate");
		String SQL="select 'COMPRAS' AS TIPO,A.NOMBRE AS PROVEEDOR,S.NOMBRE AS SUCURSAL,A.CLAVE AS CONCEPTO,SUM(  (X.CANTIDAD/I.FACTORU)*(a.TC * x.COSTO)) as TOTAL"
				+" from sx_inventario_com I" 
				+" JOIN sx_analisisdet X ON(I.INVENTARIO_ID=X.ENTRADA_ID)" 
				+" JOIN sx_analisis  R ON (R.ANALISIS_ID=X.ANALISIS_ID)"
				+" LEFT JOIN SX_CXP A ON(A.CXP_ID=R.CXP_ID) "
				+" JOIN sw_sucursales S ON(S.SUCURSAL_ID=I.SUCURSAL_ID)"
				+" where DATE(I.FECHA) BETWEEN ? AND ?"
				+" group by A.NOMBRE,S.NOMBRE,A.CLAVE";
		/*
		String SQL="SELECT 'COMPRAS' AS TIPO,P.NOMBRE AS PROVEEDOR,S.NOMBRE AS SUCURSAL,P.CLAVE AS CONCEPTO"
				+" ,ROUND(SUM((IC.CANTIDAD/IC.FACTORU*(SELECT min(x.COSTO*yy.tc) FROM sx_analisisdet x join sx_analisis xx on(xx.analisis_id=x.analisis_id) join sx_cxp yy on( xx.cxp_id=yy.cxp_id) where x.ENTRADA_ID=ic.inventario_id ))),2) AS TOTAL  "  
				+ " FROM sx_inventario_com IC "
				+ " JOIN sx_entrada_compras D ON(D.ID=IC.RECEPCION_ID) "
				+ " JOIN sw_sucursales S ON(S.SUCURSAL_ID=IC.SUCURSAL_ID) " 
				+ " join sx_proveedores p on(p.PROVEEDOR_ID=ic.PROVEEDOR_ID) "
				+ " WHERE  DATE(IC.FECHA) BETWEEN ? AND ?" 
				+ " GROUP BY P.CLAVE,P.PROVEEDOR_ID,IC.SUCURSAL_ID";
		*/
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
				,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
		};
		
		Map<String, BigDecimal> acumuladosPorSucursal=new HashMap<String, BigDecimal>();
		
		List<Map<String, Object>> rows=jdbcTemplate.queryForList(SQL,params);
		
		for(Map<String ,Object> row:rows){
			Number valorNumerico=(Number)row.get("TOTAL");
			final BigDecimal total=BigDecimal.valueOf(valorNumerico.doubleValue());
			String sucursal=(String)row.get("SUCURSAL");
			
			BigDecimal totalAcumulado=acumuladosPorSucursal.get(sucursal);
			if(totalAcumulado==null){
				acumuladosPorSucursal.put(sucursal, total);
			}else{
				totalAcumulado=totalAcumulado.add(total);
				acumuladosPorSucursal.put(sucursal, totalAcumulado);
				
			}
			String proveedor=(String)row.get("PROVEEDOR");
			//String conceptoClave=(String)row.get("CONCEPTO");
			
			
			PolizaDetFactory.generarPolizaDet(poliza, "119", "INVC01", false, total, "COMPRAS", proveedor, sucursal, asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", true, total, "COMPRAS", proveedor, sucursal, asiento);
			
			
		}
		

		
	}
}
