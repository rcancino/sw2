package com.luxsoft.sw3.contabilidad.polizas.inventarios;

import java.math.BigDecimal;
import java.sql.Types;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
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
public class Proc_Inventario {
	
	Logger logger=LoggerHelper.getLogger();
	private Periodo periodo;
	Poliza poliza;
	private final JdbcTemplate jdbcTemplate;
	
	public Proc_Inventario(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate=jdbcTemplate;
	}
	
	public Poliza generarPoliza(Date fecha) {
		poliza=new Poliza();
		periodo=Periodo.getPeriodoEnUnMes(fecha);
		poliza.setFecha(periodo.getFechaFinal());
		poliza.setTipo(Poliza.Tipo.DIARIO);
		poliza.setDescripcion(MessageFormat.format("Inventarios : ",periodo.toString2()));
		poliza.setClase("INVENTARIOS");
		procesarPoliza();
		poliza.actualizar();
		return poliza;
	}
	
	protected void procesarPoliza() {
		//registrarMovimientosGenericos();
		//registrarRedondeo();
		registrarMaquila();
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
			Sucursal sucursal=(Sucursal)ServiceLocator2.getUniversalDao().get(Sucursal.class,sucursalID.longValue());
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
	
	
	private CuentaContable getCuenta(String clave){
		return ServiceLocator2.getCuentasContablesManager().buscarPorClave(clave);
	}
	
	
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	

	public static void main(String[] args) {
		Proc_Inventario model=new Proc_Inventario(ServiceLocator2.getJdbcTemplate());
		model.generarPoliza(DateUtil.toDate("15/08/2011"));
		
	}

	
}
