package com.luxsoft.sw3.contabilidad.services;

import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;

/**
 * Utilerias para buscar informacion en la base de datos 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ContabilidadSqlSupport {
	
	
	private static ContabilidadSqlSupport INSTANCE;
	
	public static ContabilidadSqlSupport getInstance(){
		if(INSTANCE==null){
			INSTANCE=new ContabilidadSqlSupport(ServiceLocator2.getJdbcTemplate());
		}
		return INSTANCE;
	}
	
	public ContabilidadSqlSupport() {
	}
	
	public ContabilidadSqlSupport(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}



	/**
	 * Todos los registros de cobranza en la fecha indicada
	 * 
	 * @param fecha
	 * @return
	 */
	public List<CCobranza> buscarCobranza(Date fecha){
		String SQL="select 'APLICACION' AS TIPO,A.APLICACION_ID AS ORIGEN_ID,A.FECHA" +
			//	" ,A.CAR_SUCURSAL AS SUCURSAL" +
				" ,(SELECT S.NOMBRE FROM sx_cxc_abonos x join sw_sucursales s on(s.SUCURSAL_ID=x.SUCURSAL_ID) where x.ABONO_ID=a.abono_id) AS SUCURSAL " +
				" ,A.ABONO_ID,a.CAR_ORIGEN AS ORIGEN" +
				" ,a.ABN_DESCRIPCION as CONCEPTO,(importe) AS IMPORTE" +
		",(SELECT C.DESCRIPCION FROM sx_cxc_abonos X JOIN sw_cuentas C ON(C.ID=X.CUENTA_ID) WHERE X.ABONO_ID=A.ABONO_ID) AS BANCO,substr(a.ABN_DESCRIPCION,1,3) AS DESCRIPCION" +
		" from sx_cxc_aplicaciones a" +
		" where a.fecha=? and (a.CAR_ORIGEN in(\'MOS\',\'CAM\') OR (A.CAR_ORIGEN=\'CRE\' AND substr(a.ABN_DESCRIPCION,1,3)=\'TAR\'))" +
		" AND A.TIPO=\'PAGO\' AND A.CARGO_ID NOT IN(SELECT X.CARGO_ID FROM SX_VENTAS X WHERE X.CARGO_ID=A.CARGO_ID AND X.TIPO='TES')";
		Object[] params=new Object[]{new SqlParameterValue(Types.DATE,fecha)};
		List<CCobranza> res=getJdbcTemplate().query(SQL, params, new BeanPropertyRowMapper(CCobranza.class));
		return res;
	}
	
	
	public List<CCobranza> buscarCobranza(Date fecha,String origen){
		String SQL="select 'APLICACION' AS TIPO,A.APLICACION_ID AS ORIGEN_ID,A.FECHA" +
			//	",A.CAR_SUCURSAL AS SUCURSAL" +
				" ,(SELECT S.NOMBRE FROM sx_cxc_abonos x join sw_sucursales s on(s.SUCURSAL_ID=x.SUCURSAL_ID) where x.ABONO_ID=a.abono_id) AS SUCURSAL " +
				",A.ABONO_ID,a.CAR_ORIGEN AS ORIGEN" +
				",a.ABN_DESCRIPCION as CONCEPTO,(importe) AS IMPORTE" +
				",(SELECT C.DESCRIPCION FROM sx_cxc_abonos X JOIN sw_cuentas C ON(C.ID=X.CUENTA_ID) WHERE X.ABONO_ID=A.ABONO_ID) AS BANCO" +
				",substr(a.ABN_DESCRIPCION,1,3) AS DESCRIPCION" +
		" from sx_cxc_aplicaciones a" +
		" where a.fecha=? " +
		"   and a.CAR_ORIGEN=\'@ORIGEN\' " +
		"   AND substr(a.ABN_DESCRIPCION,1,3)<>\'TAR\'" +
		"   AND A.TIPO=\'PAGO\'";
		SQL=SQL.replaceAll("@ORIGEN", origen);
		Object[] params=new Object[]{new SqlParameterValue(Types.DATE,fecha)};
		List<CCobranza> res=getJdbcTemplate().query(SQL, params, new BeanPropertyRowMapper(CCobranza.class));
		return res;
	}
	
	
	/**
	 * Regresa un Map de la cobranza por origen, en donde para cada llave del map existe una lista
	 * de cobranza. Cada llave del map (key) corresponde a un origen de operacion (CRE,CAM etc)
	 * 
	 * @param fecha
	 * @return
	 */
	public Map<String, List<CCobranza>> buscarCobranzaPorOrigen(final Date fecha){
		EventList<CCobranza> cobranzas=GlazedLists.eventList(buscarCobranza(fecha));				
		FunctionList.Function<CCobranza, String> function=new FunctionList.Function<CCobranza, String>(){
			public String evaluate(CCobranza sourceValue) {
				return sourceValue.getOrigen();
			}						
		};
		final Map<String, List<CCobranza>> ingresosPorOrigen=GlazedLists.syncEventListToMultiMap(cobranzas, function);
		return ingresosPorOrigen;
	}
	
	/**
	 * Regresa un Map de la cobranza por origen, en donde para cada llave del map existe una lista
	 * de cobranza. Cada llave del map (key) corresponde a un origen de operacion (CRE,CAM etc)
	 * 
	 * @param fecha
	 * @return
	 */
	public Map<String, List<CCobranza>> buscarCobranzaPorOrigen(final Date fecha,String origen){
		EventList<CCobranza> cobranzas=GlazedLists.eventList(buscarCobranza(fecha,origen));				
		FunctionList.Function<CCobranza, String> function=new FunctionList.Function<CCobranza, String>(){
			public String evaluate(CCobranza sourceValue) {
				return sourceValue.getOrigen();
			}						
		};
		final Map<String, List<CCobranza>> ingresosPorOrigen=GlazedLists.syncEventListToMultiMap(cobranzas, function);
		return ingresosPorOrigen;
	}
	
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public static void main(String[] args) {
		Map<String, List<CCobranza>> res=ContabilidadSqlSupport
		.getInstance()
		.buscarCobranzaPorOrigen(DateUtil.toDate("2/05/2011"));
		for(String key:res.keySet()){
			List<CCobranza> data=res.get(key);
			System.out.println(key+ " Registros: "+data.size());
		}
	}	

}
