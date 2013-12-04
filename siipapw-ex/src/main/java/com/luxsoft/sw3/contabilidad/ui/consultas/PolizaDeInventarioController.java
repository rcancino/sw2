package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.math.BigDecimal;
import java.sql.Types;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SqlParameterValue;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.services.AbstractPolizaManager;
import com.luxsoft.sw3.contabilidad.services.PolizaContableManager;
import com.luxsoft.utils.LoggerHelper;



/**
 * Implementacion de {@link PolizaContableManager} para la generación y mantenimiento 
 * de la poliza de inventarios
 * 
 * NOTA: Correr diariamente el siquiente query hasta que la nueva version este en produccion
 * 
 * UPDATE SX_INVENTARIO_COM X SET X.COSTO=(
    SELECT ROUND(SUM((A.COSTO)*C.TC),6)  
		 FROM sx_cxp_analisisdet A 
		 JOIN sx_cxp C ON(C.CXP_ID=A.CXP_ID)
		 WHERE X.INVENTARIO_ID=A.ENTRADA_ID
	)
	WHERE DATE(X.FECHA) BETWEEN '2011-08-01' AND '2011-08-31'
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDeInventarioController extends AbstractPolizaManager{
	
	Logger logger=LoggerHelper.getLogger();
	private Periodo periodo;
	protected void inicializarPoliza(final Date fecha){
		poliza=new Poliza();
		
		periodo=Periodo.getPeriodoEnUnMes(fecha);
		poliza.setFecha(periodo.getFechaFinal());
		poliza.setTipo(Poliza.Tipo.DIARIO);
		poliza.setDescripcion(MessageFormat.format("Inventarios : ",periodo.toString2()));
	}
	
	protected void inicializarDatos(){}
	
	@Override
	protected void procesarPoliza() {
		//registrarCompras();
		registrarCostoDeVenta();
		//registrarDescuentosPendientes();
		registrarDevolucionesDeCompras();
		registrarGastoPorTransformacion();
		registrarMovimientosGenericos();
		registrarRedondeo();
		registrarMaquila();
	}
	
	private void registrarCompras(){
		final String asiento="Compras";
		
		String SQL="SELECT 'COMPRAS' AS TIPO,C.NOMBRE AS PROVEEDOR,S.NOMBRE AS SUCURSAL, C.CLAVE AS CONCEPTO"
				+",ROUND(SUM((IC.CANTIDAD/IC.FACTORU*IC.COSTO)),2) AS TOTAL" 
		 +" FROM sx_inventario_com IC "
		 +" JOIN sx_proveedores C ON(C.PROVEEDOR_ID=IC.PROVEEDOR_ID)"
		 +" JOIN sw_sucursales S ON(S.SUCURSAL_ID=IC.SUCURSAL_ID)"
		 +" WHERE  DATE(IC.FECHA) BETWEEN ? AND ? "
		 +" GROUP BY C.CLAVE,C.NOMBRE,IC.SUCURSAL_ID"
         +" ORDER BY C.CLAVE";
		
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
				,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
		};
		
		Map<String, BigDecimal> acumuladosPorSucursal=new HashMap<String, BigDecimal>();
		
		List<Map<String, Object>> rows=getJdbcTemplate().queryForList(SQL,params);
		
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
			String conceptoClave=(String)row.get("CONCEPTO");
			//Abono a Inventario por Proveedor
			PolizaDet abono=poliza.agregarPartida();
			abono.setCuenta(getCuenta("119"));
			ConceptoContable concepto=abono.getCuenta().getConcepto(conceptoClave);
			abono.setConcepto(concepto);
			abono.setHaber(total);
			abono.setDescripcion2("Proveedores COMPRAS");
			abono.setReferencia(proveedor);
			abono.setReferencia2(sucursal);
			abono.setAsiento(asiento);
			
		}
		
		//Cargos a Inventario por sucursal
		for(Map.Entry<String, BigDecimal> sucursalEntry:acumuladosPorSucursal.entrySet()){
			String sucursal=sucursalEntry.getKey();
			BigDecimal totalPorSuc=sucursalEntry.getValue();
			String conceptoClave="INVF_"+sucursal;
			PolizaDet cargo=poliza.agregarPartida();
			cargo.setCuenta(getCuenta("119"));
			ConceptoContable concepto=cargo.getCuenta().getConcepto(conceptoClave);	
			
			cargo.setConcepto(concepto);
			cargo.setDebe(totalPorSuc);
			cargo.setDescripcion2("Invetario final compras");
			//cargo.setReferencia(proveedor);
			cargo.setReferencia2(sucursal);
			cargo.setAsiento(asiento);
		}
		
	}
	
	/**
	 * TODO Verificar si se requiere
	 */
	private void registrarDescuentosPendientes(){
		String sql="SELECT S.NOMBRE AS SUCURSAL" +
				",SUM((A.CANTIDAD/IC.FACTORU*(A.PRECIO))*C.TC) AS IMPORTE " +
				",SUM((A.CANTIDAD/IC.FACTORU*(A.COSTO))*C.TC) AS COSTO " +
				",SUM((A.CANTIDAD/IC.FACTORU*(A.PRECIO-A.COSTO))*C.TC) AS PROVISION " +
				",(SUM((A.CANTIDAD/IC.FACTORU*(A.PRECIO-A.COSTO))*C.TC)*100/SUM((A.CANTIDAD/IC.FACTORU*(A.PRECIO))*C.TC)) AS PORC " +
				" FROM sx_inventario_com IC JOIN sx_cxp_analisisdet A ON(IC.INVENTARIO_ID=A.ENTRADA_ID) " +
				" JOIN sx_cxp C ON(C.CXP_ID=A.CXP_ID) JOIN sw_sucursales S ON(S.SUCURSAL_ID=IC.SUCURSAL_ID) " +
				" WHERE  DATE(IC.FECHA) between ?  and ? " +
				" GROUP BY IC.SUCURSAL_ID " +
				" HAVING SUM((A.CANTIDAD/IC.FACTORU*((CASE WHEN C.PROVEEDOR_ID IN(140,197,193,55) THEN A.PRECIO-A.COSTO ELSE 0 END)))*C.TC)>0 " +
				" ORDER BY C.CLAVE";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
				,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
		};
		System.out.println(sql);
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		Comparator<Map<String, Object>> c=new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				String s1=(String)o1.get("SUCURSAL");
				String s2=(String)o2.get("SUCURSAL");
				return s1.compareTo(s2);
			}
		}; 
				
		GroupingList<Map<String,Object>> porSucursal=new GroupingList<Map<String, Object>>(GlazedLists.eventList(rows),c);
		String asiento="Compras";
		
		for(List<Map<String,Object>> list:porSucursal){
			
			BigDecimal importePorSucursal=BigDecimal.ZERO;
			String sucursal=(String)list.get(0).get("SUCURSAL");
			
			for(Map<String,Object> row:list){
				BigDecimal importe=calcularImporteDelTotal(new BigDecimal( ((Number)row.get("PROVISION")).doubleValue()));
				importe=redondear(importe);
			}
			PolizaDet cargoAProvisionEnCompras=poliza.agregarPartida();
			cargoAProvisionEnCompras.setCuenta(getCuenta("119"));
			cargoAProvisionEnCompras.setDebe(importePorSucursal);
			cargoAProvisionEnCompras.setConcepto(cargoAProvisionEnCompras.getCuenta().getConcepto("DCOM1"));
			cargoAProvisionEnCompras.setDescripcion2("Descuentos sobre compras");
			cargoAProvisionEnCompras.setReferencia2(sucursal);
			cargoAProvisionEnCompras.setAsiento(asiento);
		}
		
	}
	
	private void registrarCostoDeVenta(){
		final String asiento="Costo de ventas";
		
	/*	String SQL="SELECT 'COSTOVTA' AS TIPO" +
				" ,IC.SUCURSAL_ID,S.NOMBRE AS SUCURSAL" +
				" ,ROUND(SUM((-IC.CANTIDAD/IC.FACTORU*IC.COSTOP)),2) AS TOTAL  " +
				"	FROM sx_ventasdet IC  JOIN sw_sucursales S ON(S.SUCURSAL_ID=IC.SUCURSAL_ID) " +
				" WHERE  IC.FECHA BETWEEN \'@FECHA_INI\' AND \'@FECHA_FIN\' " +
				" GROUP BY IC.SUCURSAL_ID";  */
		
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
		
		List<Map<String, Object>> rows=getJdbcTemplate().queryForList(SQL);
		
		for(Map<String ,Object> row:rows){
			Number valorNumerico=(Number)row.get("TOTAL");
			final BigDecimal total=BigDecimal.valueOf(valorNumerico.doubleValue());
			String sucursal=(String)row.get("SUCURSAL");
			Number sucursalID=(Number)row.get("SUCURSAL_ID");
			
			
			//String conceptoClave="CVTA"+sucursalID;
			
			//Abono a Inventario por Proveedor
			PolizaDet cargo=poliza.agregarPartida();
			cargo.setCuenta(getCuenta("501"));
			cargo.setConcepto(cargo.getCuenta().getConcepto("CVTA"+sucursalID));
			cargo.setDebe(total);
			cargo.setDescripcion2("Aplicación de costo de ventas");
			//abono.setReferencia(proveedor);
			cargo.setReferencia2(sucursal);
			cargo.setAsiento(asiento);
			
			PolizaDet abono=poliza.agregarPartida();
			abono.setCuenta(getCuenta("119"));
			abono.setConcepto(abono.getCuenta().getConcepto("INVF_"+sucursal));
			abono.setHaber(total);
			
			abono.setReferencia2(sucursal);
			abono.setDescripcion2("Inventario Final");
			abono.setAsiento(asiento);			
		}
	}
	
	private void registrarDevolucionesDeCompras(){
		final String asiento="Devolucion COMPRAS";
		
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
		
		List<Map<String, Object>> rows=getJdbcTemplate().queryForList(SQL,params);
		
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
			String conceptoClave=(String)row.get("CONCEPTO");
			//Abono a Inventario por Proveedor
			PolizaDet cargo=poliza.agregarPartida();
			cargo.setCuenta(getCuenta("119"));
			ConceptoContable concepto=cargo.getCuenta().getConcepto(conceptoClave);
			cargo.setConcepto(concepto);
			cargo.setDebe(total);
			cargo.setDescripcion2("Proveedores devolucion de compras");
			cargo.setReferencia(proveedor);
			cargo.setReferencia2(sucursal);
			cargo.setAsiento(asiento);			
		}
		
		//Cargos a Inventario por sucursal
		for(Map.Entry<String, BigDecimal> sucursalEntry:acumuladosPorSucursal.entrySet()){
			String sucursal=sucursalEntry.getKey();
			BigDecimal totalPorSuc=sucursalEntry.getValue();
			String conceptoClave="INVF_"+sucursal;
			PolizaDet abono=poliza.agregarPartida();
			abono.setCuenta(getCuenta("119"));
			ConceptoContable concepto=abono.getCuenta().getConcepto(conceptoClave);	
			
			abono.setConcepto(concepto);
			abono.setHaber(totalPorSuc);
			abono.setDescripcion2("Invetario final devolucion");
			//cargo.setReferencia(proveedor);
			abono.setReferencia2(sucursal);
			abono.setAsiento(asiento);
		}
		
	}
	
	private void registrarGastoPorTransformacion(){		
		final String asiento="Gasto por transformaciones";		
		String SQL=" SELECT 'GASTO TRANSFORMACIONES' AS TIPO,IC.SUCURSAL_ID,S.NOMBRE AS SUCURSAL"
				+",IFNULL(ROUND(SUM((IC.CANTIDAD/IC.FACTORU*IC.GASTOS)),2),0) AS TOTAL" 
				+" FROM sx_inventario_trs IC " 
				+" JOIN sx_transformaciones D ON(D.TRANSFORMACION_ID=IC.TRANSFORMACION_ID) " 
				+" JOIN sw_sucursales S ON(S.SUCURSAL_ID=IC.SUCURSAL_ID) "
				+" WHERE  IC.FECHA BETWEEN \'@FECHA_INI\' AND \'@FECHA_FIN\'" 
				+" GROUP BY IC.SUCURSAL_ID;";
		DateFormat df=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.0");
		SQL=SQL.replaceAll("@FECHA_INI", df.format(periodo.getFechaInicial()));
		SQL=SQL.replaceAll("@FECHA_FIN", df.format(periodo.getFechaFinal()));		
		List<Map<String, Object>> rows=getJdbcTemplate().queryForList(SQL);
		
		for(Map<String ,Object> row:rows){
			Number valorNumerico=(Number)row.get("TOTAL");
			final BigDecimal total=BigDecimal.valueOf(valorNumerico.doubleValue());
			String sucursal=(String)row.get("SUCURSAL");
			Number sucursalID=(Number)row.get("SUCURSAL_ID");			
			//Cargo
			PolizaDet cargo=poliza.agregarPartida();
			cargo.setCuenta(getCuenta("119"));
			cargo.setConcepto(cargo.getCuenta().getConcepto("INVT"+sucursalID));
			cargo.setDebe(total);
			cargo.setDescripcion2("Inventario final gasto por TRS");
			cargo.setReferencia2(sucursal);
			cargo.setAsiento(asiento);
			//Abono
			PolizaDet abono=poliza.agregarPartida();
			abono.setCuenta(getCuenta("119"));
			abono.setConcepto(abono.getCuenta().getConcepto("INVF_"+sucursal));
			abono.setHaber(total);
			abono.setReferencia2(sucursal);
			abono.setDescripcion2("Inventario Final");
			abono.setAsiento(asiento);			
		}
		
	}
	
	private void registrarMovimientosGenericos(){		
		final String asiento="Movimientos genericos";		
		String SQL=SQLUtils.loadSQLQueryFromResource("sql/contabilidad/PolizaDeInventario_Genericos.sql");
		DateFormat df=new SimpleDateFormat("yyyy/MM/dd");
		SQL=SQL.replaceAll("@FECHA_INI", df.format(periodo.getFechaInicial()));
		SQL=SQL.replaceAll("@FECHA_FIN", df.format(periodo.getFechaFinal()));		
		List<Map<String, Object>> rows=getJdbcTemplate().queryForList(SQL);
		
		for(Map<String ,Object> row:rows){
			
			String conceptoClave=(String)row.get("CONCEPTO");
			String descripcion2=(String)row.get("DESCRIPCION");
			Number valorNumerico=(Number)row.get("COSTO");
			final BigDecimal total=BigDecimal.valueOf(valorNumerico.doubleValue());
			
			Number sucursalID=(Number)row.get("SUCURSAL_ID");
			Sucursal sucursal=getSucursal(sucursalID.longValue());
		
			if("ITRD1".equals(conceptoClave) ){
				PolizaDet registro=poliza.agregarPartida();
				registro.setCuenta(getCuenta("119"));
				registro.setConcepto(registro.getCuenta().getConcepto(conceptoClave));
				registro.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				registro.setDescripcion2(descripcion2);
				registro.setAsiento(asiento);
				if(total.doubleValue()<0){
					//Cargo
					registro.setDebe(total.abs());
				}else{
					registro.setHaber(total.abs());
				}
				
			}else if("OING1".equals(conceptoClave)){				
				PolizaDet abono=poliza.agregarPartida();
				abono.setCuenta(getCuenta("702"));
				abono.setConcepto(abono.getCuenta().getConcepto(conceptoClave));
				abono.setHaber(total.abs());
				abono.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				abono.setDescripcion2(descripcion2);
				abono.setAsiento(asiento);
				
				PolizaDet cargo=poliza.agregarPartida();
				cargo.setCuenta(getCuenta("119"));
				cargo.setConcepto(cargo.getCuenta().getConcepto("INVF_"+sucursal.getNombre()));
				cargo.setDebe(total.abs());
				cargo.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				cargo.setDescripcion2(descripcion2);
				cargo.setAsiento(asiento);
			}else if("OGST1".equals(conceptoClave)){				
				PolizaDet cargo=poliza.agregarPartida();
				cargo.setCuenta(getCuenta("704"));
				cargo.setConcepto(cargo.getCuenta().getConcepto(conceptoClave));
				cargo.setDebe(total.abs());
				cargo.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				cargo.setDescripcion2(descripcion2);
				cargo.setAsiento(asiento);
				
				PolizaDet abono=poliza.agregarPartida();
				abono.setCuenta(getCuenta("119"));
				abono.setConcepto(abono.getCuenta().getConcepto("INVF_"+sucursal.getNombre()));
				abono.setHaber(total.abs());
				abono.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				abono.setDescripcion2(descripcion2);
				abono.setAsiento(asiento);
			}else if("NDED1".equals(conceptoClave)){				
				PolizaDet cargo=poliza.agregarPartida();
				cargo.setCuenta(getCuenta("600"));
				cargo.setConcepto(cargo.getCuenta().getConcepto(conceptoClave));
				cargo.setDebe(total.abs());
				cargo.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				cargo.setDescripcion2(descripcion2);
				cargo.setAsiento(asiento);
				
				PolizaDet abono=poliza.agregarPartida();
				abono.setCuenta(getCuenta("119"));
				abono.setConcepto(abono.getCuenta().getConcepto("INVF_"+sucursal.getNombre()));
				abono.setHaber(total.abs());
				abono.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				abono.setDescripcion2(descripcion2);
				abono.setAsiento(asiento);
			}else if("MATE1".equals(conceptoClave)){
				PolizaDet cargo=poliza.agregarPartida();
				cargo.setCuenta(getCuenta("600"));
				cargo.setConcepto(cargo.getCuenta().getConcepto(conceptoClave));
				cargo.setDebe(total.abs());
				cargo.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				cargo.setDescripcion2(descripcion2);
				cargo.setAsiento(asiento);
				
				PolizaDet abono=poliza.agregarPartida();
				abono.setCuenta(getCuenta("119"));
				abono.setConcepto(abono.getCuenta().getConcepto("INVF_"+sucursal.getNombre()));
				abono.setHaber(total.abs());
				abono.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				abono.setDescripcion2(descripcion2);
				abono.setAsiento(asiento);
			}else if("PAPC1".equals(conceptoClave)){
				PolizaDet cargo=poliza.agregarPartida();
				cargo.setCuenta(getCuenta("600"));
				cargo.setConcepto(cargo.getCuenta().getConcepto(conceptoClave));
				cargo.setDebe(total.abs());
				cargo.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				cargo.setDescripcion2(descripcion2);
				cargo.setAsiento(asiento);
				
				PolizaDet abono=poliza.agregarPartida();
				abono.setCuenta(getCuenta("119"));
				abono.setConcepto(abono.getCuenta().getConcepto("INVF_"+sucursal.getNombre()));
				abono.setHaber(total.abs());
				abono.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				abono.setDescripcion2(descripcion2);
				abono.setAsiento(asiento);
			}else if("PUBP1".equals(conceptoClave)){
				PolizaDet cargo=poliza.agregarPartida();
				cargo.setCuenta(getCuenta("600"));
				cargo.setConcepto(cargo.getCuenta().getConcepto(conceptoClave));
				cargo.setDebe(total.abs());
				cargo.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				cargo.setDescripcion2(descripcion2);
				cargo.setAsiento(asiento);
				
				PolizaDet abono=poliza.agregarPartida();
				abono.setCuenta(getCuenta("119"));
				abono.setConcepto(abono.getCuenta().getConcepto("INVF_"+sucursal.getNombre()));
				abono.setHaber(total.abs());
				abono.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				abono.setDescripcion2(descripcion2);
				abono.setAsiento(asiento);
			}
			
						
		}
		
	}
	
	
	
	private void registrarRedondeo(){		
		final String asiento="Redondeo";		
		String SQL=SQLUtils.loadSQLQueryFromResource("sql/contabilidad/PolizaDeInventario_Redondeo.sql");
		DateFormat df=new SimpleDateFormat("yyyy/MM/dd");
		SQL=SQL.replaceAll("@FECHA_INI", df.format(periodo.getFechaInicial()));
		SQL=SQL.replaceAll("@FECHA_FIN", df.format(periodo.getFechaFinal()));		
		List<Map<String, Object>> rows=getJdbcTemplate().queryForList(SQL);
		System.out.println(SQL);
		
		BigDecimal totalAcumulado=BigDecimal.ZERO;
		PolizaDet calle4Det=null;
		
		for(Map<String ,Object> row:rows){
			String conceptoClave=(String)row.get("CONCEPTO");
			Number valorNumerico=(Number)row.get("COSTO");
			final BigDecimal total=BigDecimal.valueOf(valorNumerico.doubleValue());
			totalAcumulado=totalAcumulado.add(total);
			Number sucursalID=(Number)row.get("SUCURSAL_ID");
			Sucursal sucursal=getSucursal(sucursalID.longValue());
			String descripcion2=(String)row.get("DESCRIPCION");
		
			if("IRED1".equals(conceptoClave) ){
				PolizaDet registro=poliza.agregarPartida();
				registro.setCuenta(getCuenta("119"));
				registro.setConcepto(registro.getCuenta().getConcepto(conceptoClave));
				registro.setReferencia2(sucursal!=null?sucursal.getNombre():" "+sucursalID);
				registro.setDescripcion2(descripcion2);
				registro.setAsiento(asiento);
				if(total.doubleValue()>0){
					//Cargo
					registro.setDebe(total.abs());
				}else{
					registro.setHaber(total.abs());
				}
				if(sucursalID.longValue()==2L)
					calle4Det=registro;
			}		
		}
		System.out.println("Redondeo: "+totalAcumulado);
		if(calle4Det!=null && totalAcumulado.abs().doubleValue()>0){			
			if(calle4Det.getDebe().abs().doubleValue()>0){
				System.out.println(" Calle 4: "+calle4Det.getDebe());
				calle4Det.setDebe(calle4Det.getDebe().add(totalAcumulado));
			}else{
				System.out.println(" Calle 4: "+calle4Det.getHaber());
				calle4Det.setHaber(calle4Det.getHaber().add(totalAcumulado));
			}
		}
	}
	
	private void registrarMaquila(){		
		final String asiento="Maquila";		
		String SQL=SQLUtils.loadSQLQueryFromResource("sql/contabilidad/PolizaDeInventario_Maquila.sql");
		DateFormat df=new SimpleDateFormat("yyyy/MM/dd");
		SQL=SQL.replaceAll("@FECHA_INI", df.format(periodo.getFechaInicial()));
		SQL=SQL.replaceAll("@FECHA_FIN", df.format(periodo.getFechaFinal()));		
		List<Map<String, Object>> rows=getJdbcTemplate().queryForList(SQL);
		System.out.println(SQL);
		
		for(Map<String ,Object> row:rows){
			
			String conceptoClave=(String)row.get("CONCEPTO");
			String descripcion2=(String)row.get("DESCRIPCION");
			Number valorNumerico=(Number)row.get("COSTO");
			final BigDecimal total=BigDecimal.valueOf(valorNumerico.doubleValue());
			
			//Number sucursalID=(Number)row.get("SUCURSAL_ID");
			String sucursal=(String)row.get("SUCURSAL");			
			String almacen=(String)row.get("ALMACEN");
			almacen=StringUtils.trimToEmpty(almacen);
			
			PolizaDet cargo=poliza.agregarPartida();
			cargo.setCuenta(getCuenta("119"));
			cargo.setConcepto(cargo.getCuenta().getConcepto("INVF_"+sucursal));
			cargo.setDebe(total.abs());
			cargo.setReferencia2(sucursal);
			cargo.setDescripcion2(descripcion2);
			cargo.setAsiento(asiento);
			
			PolizaDet abono=poliza.agregarPartida();
			abono.setCuenta(getCuenta("119"));
			abono.setConcepto(cargo.getCuenta().getConcepto("IMAQM_"+almacen));
			abono.setHaber(total.abs());
			abono.setReferencia2(sucursal);
			abono.setDescripcion2(descripcion2);
			abono.setAsiento(asiento);
			
			
			final BigDecimal totalCorte=BigDecimal.valueOf( ((Number)row.get("CORTE")).doubleValue() );
			
			PolizaDet cargoCorte=poliza.agregarPartida();
			cargoCorte.setCuenta(getCuenta("119"));
			cargoCorte.setConcepto(cargoCorte.getCuenta().getConcepto("INVF_"+sucursal));
			cargoCorte.setDebe(totalCorte.abs());
			cargoCorte.setReferencia2(sucursal);
			cargoCorte.setDescripcion2(descripcion2);
			cargoCorte.setAsiento(asiento);
			
			PolizaDet abonoPorCorte=poliza.agregarPartida();
			abonoPorCorte.setCuenta(getCuenta("119"));
			abonoPorCorte.setConcepto(cargoCorte.getCuenta().getConcepto("IMAQC_"+almacen));
			abonoPorCorte.setHaber(totalCorte.abs());
			abonoPorCorte.setReferencia2(sucursal);
			abonoPorCorte.setDescripcion2(descripcion2);
			abonoPorCorte.setAsiento(asiento);
			
			final BigDecimal totalFlete=BigDecimal.valueOf( ((Number)row.get("FLETE")).doubleValue() );
			
			PolizaDet cargoPorFlete=poliza.agregarPartida();
			cargoPorFlete.setCuenta(getCuenta("119"));
			cargoPorFlete.setConcepto(cargoPorFlete.getCuenta().getConcepto("INVF_"+sucursal));
			cargoPorFlete.setDebe(totalFlete.abs());
			cargoPorFlete.setReferencia2(sucursal);
			cargoPorFlete.setDescripcion2(descripcion2);
			cargoPorFlete.setAsiento(asiento);
			
			PolizaDet abonoPorFlete=poliza.agregarPartida();
			abonoPorFlete.setCuenta(getCuenta("119"));
			abonoPorFlete.setConcepto(cargoPorFlete.getCuenta().getConcepto("IMAQF_"+almacen));
			abonoPorFlete.setHaber(totalFlete.abs());
			abonoPorFlete.setReferencia2(sucursal);
			abonoPorFlete.setDescripcion2(descripcion2);
			abonoPorFlete.setAsiento(asiento);
			
		}
		
	}
	
	private static ConceptoContable altasDeConcepto(CuentaContable cuenta,String clave,String descripcion){
		ConceptoContable c=new ConceptoContable();
		c.setCuenta(cuenta);
		c.setClave(clave);
		c.setDescripcion(descripcion);
		return (ConceptoContable)ServiceLocator2.getHibernateTemplate().merge(c);
	}

	public static void main(String[] args) {
		PolizaDeInventarioController model=new PolizaDeInventarioController();
		model.generarPoliza(DateUtil.toDate("15/08/2011"));
		
	}

	
}
