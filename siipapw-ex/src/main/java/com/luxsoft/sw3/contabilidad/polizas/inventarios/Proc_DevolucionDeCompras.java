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

public class Proc_DevolucionDeCompras implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		final String asiento="Devolucion COMPRAS";
		Periodo periodo=Periodo.getPeriodoEnUnMes(poliza.getFecha());
		JdbcTemplate jdbcTemplate=(JdbcTemplate)model.get("jdbcTemplate");
		String SQL="SELECT 'DEV COMPRAS DEC' AS TIPO,D.NOMBRE AS PROVEEDOR,S.NOMBRE AS SUCURSAL,D.CLAVE AS CONCEPTO,ROUND(SUM((-IC.CANTIDAD/IC.FACTORU*IC.COSTOP)),2) AS TOTAL"  
					+ " FROM sx_inventario_dec IC "
					+ " JOIN sx_devolucion_compras D ON(D.DEVOLUCION_ID=IC.DEVOLUCION_ID)"+ 
					" JOIN sw_sucursales S ON(S.SUCURSAL_ID=IC.SUCURSAL_ID) "+
					" WHERE  IC.FECHA BETWEEN ? AND ?"+ 
					" GROUP BY D.CLAVE,D.PROVEEDOR_ID,IC.SUCURSAL_ID";
		
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
			
			//Abono a Inventario por Proveedor
			//PolizaDetFactory.generarPolizaDet(poliza, "200", conceptoClave, true, total, "Proveedores devolucion de compras", proveedor, sucursal, asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "119", "DCOM01", true, total, "DECS", proveedor, sucursal, asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", false, total, "DECS", proveedor, sucursal, asiento);
						
		/*	PolizaDetFactory.generarPolizaDet(poliza, "119", "DCOM01", true, total, "DECS", proveedor, sucursal, asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", false, total, "DECS", proveedor, sucursal, asiento);
			*/
			
		}
		
		//Cargos a Inventario por sucursal
/*		for(Map.Entry<String, BigDecimal> sucursalEntry:acumuladosPorSucursal.entrySet()){
			String sucursal=sucursalEntry.getKey();
			BigDecimal totalPorSuc=sucursalEntry.getValue();
			String conceptoClave="ITNS04";
			
			PolizaDetFactory.generarPolizaDet(poliza, "119", conceptoClave, false, totalPorSuc, "DECS", "", sucursal, asiento);
			
		}*/
		
	}
}
