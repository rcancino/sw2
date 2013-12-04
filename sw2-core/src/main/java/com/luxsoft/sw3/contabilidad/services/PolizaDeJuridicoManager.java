package com.luxsoft.sw3.contabilidad.services;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.cxc.model.Juridico;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.tesoreria.model.TipoDeAplicacion;

import freemarker.core.ReturnInstruction.Return;

/**
 * Implementacion de {@link PolizaContableManager} para la generación y administracion
 * de polizas de ventas segun las reglas de negocios vigentes
 *  
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDeJuridicoManager  implements PolizaContableManager{
		
	
	
	private EventList<CIngresoContado> ingresos;
	
	private Map<String, List<CIngresoContado>> ingresosPorOrigen;
	
	private Map<String, List<CCobranza>> cobranzaPorOrigen;
	
	
	
	private Poliza poliza;
	
	public PolizaDeJuridicoManager(){}
	
	private void inicializar(final Date fecha){
		poliza=new Poliza();
		poliza.setFecha(fecha);
		poliza.setTipo(Poliza.Tipo.INGRESO);
		poliza.setClase("CXC TRAMITE JURIDICO");
		poliza.setDescripcion(MessageFormat.format("Poliza de Cobranza CXC Juridico del {0,date,short}",fecha));
		
	}
	
	private void inicializarDatos(){
		cargarIngresosDeTesoreria();
		cargarCobranza();
	}
	
	public void cargarIngresosDeTesoreria(){		
		ingresos=GlazedLists.eventList(CIngresoContado.buscarIngresosCredito(poliza.getFecha()));
		FunctionList.Function<CIngresoContado, String> function=new FunctionList.Function<CIngresoContado, String>(){
			public String evaluate(CIngresoContado sourceValue) {
				return sourceValue.getORIGEN();
			}						
		};
		ingresosPorOrigen=GlazedLists.syncEventListToMultiMap(ingresos, function);
	}
	
	private void cargarCobranza(){
		cobranzaPorOrigen=ContabilidadSqlSupport.getInstance().buscarCobranzaPorOrigen(poliza.getFecha(),"JUR");
	}
	
	public  Poliza generarPoliza(final Date fecha){
		
		inicializar(fecha);		
		// Carga de informacion;
		inicializarDatos();
		registrarSaldosAFavor("JUR","CXC Juridico");
		registrarCobranza();
		registrarSaldosAFavor("JUR","CXC Juridico");
		registrarSaldosAFavorDeOtrasCarteras();
		registrarAnticipos("CXC Juridico");
		registrarIETU();
		registrarDevoluciones();
		registrarNotasDeCargo();
		registrarCargosTraspasoAJuridico();
	//	registrarDepositosEnTransito();
		poliza.actualizar();
		return poliza;
	}
	
	
	
	private  void registrarOtrosGastos(String origen,String asiento){
		String sql="SELECT (SELECT S.NOMBRE FROM sw_sucursales S WHERE A.SUCURSAL_ID=S.SUCURSAL_ID) AS SUCURSAL" +
				" ,SUM(TOTAL) AS TOTAL,(SELECT X.CAR_ORIGEN FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID) AS ORIGEN " +
				" FROM sx_cxc_abonos a where fecha=?  AND TIPO_ID=\'PAGO_DIF\' " +
				" AND (SELECT X.CAR_ORIGEN FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID) =? " +
				" GROUP BY SUCURSAL_ID,(SELECT X.CAR_ORIGEN FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID)";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
				,new SqlParameterValue(Types.VARCHAR,origen)
		};
		
		//final String asiento="CXC Juridico";
		
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		for(Map<String,Object> row:rows){
			PolizaDet cargoAGastos=poliza.agregarPartida();
			cargoAGastos.setCuenta(getCuenta("704"));
			cargoAGastos.setDebe((BigDecimal)row.get("TOTAL"));
			cargoAGastos.setDescripcion("OTROS GASTOS");
			cargoAGastos.setDescripcion2("Ajustes automaticos menores a $1");
			cargoAGastos.setReferencia((String)row.get("ORIGEN"));
			cargoAGastos.setReferencia2((String)row.get("SUCURSAL"));
			cargoAGastos.setAsiento(asiento);
			if("JUR".equals(origen)){
				PolizaDet abonoaClientes=poliza.agregarPartida();
				abonoaClientes.setCuenta(getCuenta("114"));
				abonoaClientes.setHaber((BigDecimal)row.get("TOTAL"));
				abonoaClientes.setDescripcion("CUENTAS EN TRAMITE JURIDICO");
				abonoaClientes.setDescripcion2("Juridico otros gastos");
				abonoaClientes.setReferencia((String)row.get("ORIGEN"));
				abonoaClientes.setReferencia2((String)row.get("SUCURSAL"));
				abonoaClientes.setAsiento(asiento);
			}
		}
	}
	
	private  void registrarOtrosProductos(String origen,String asiento){
		String sql="SELECT (SELECT s.nombre FROM sw_sucursales s where s.SUCURSAL_ID=a.SUCURSAL_ID) as SUCURSAL" +
				" ,SUM(a.DIFERENCIA) as TOTAL"
			+",(SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID) as ORIGEN"
			+" FROM sx_cxc_abonos a " +
			" where A.diferencia_fecha=? AND A.SAF=A.DIFERENCIA_FECHA" +
			"   AND A.diferencia<>0 " +
			"   AND A.TIPO_ID<>'PAGO_HXE' " +
			"   AND (SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID)=?"
			+" GROUP BY SUCURSAL_ID,(SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID)";

		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
				,new SqlParameterValue(Types.VARCHAR,origen)
		};
		//final String asiento="CXC Juridico";
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		for(Map<String,Object> row:rows){
			String origenRow=(String)row.get("ORIGEN");
			if(origenRow.equals(origen)){
				PolizaDet abonoAProductos=poliza.agregarPartida();
				abonoAProductos.setCuenta(getCuenta("702"));
				abonoAProductos.setHaber((BigDecimal)row.get("TOTAL"));
				abonoAProductos.setDescripcion("OTROS INGRESOS");
				abonoAProductos.setDescripcion2("Ajustes automaticos menores a $10 PA");
				abonoAProductos.setReferencia(origen);
				abonoAProductos.setReferencia2((String)row.get("SUCURSAL"));
				abonoAProductos.setAsiento(asiento);
				
				BigDecimal importe=CantidadMonetaria.pesos(MonedasUtils.calcularImporteDelTotal((BigDecimal)row.get("TOTAL"))).amount();
				
				PolizaDet cargoIETU=poliza.agregarPartida();
				cargoIETU.setCuenta(getCuenta("902"));
				cargoIETU.setDebe(importe.multiply(BigDecimal.valueOf(1)));
				cargoIETU.setDescripcion("IETU ACUMULABLE");
				cargoIETU.setDescripcion2("IETU Acumulable OI Aju. PA");
				cargoIETU.setReferencia(origen);
				cargoIETU.setReferencia2((String)row.get("SUCURSAL"));
				cargoIETU.setAsiento(asiento);
				
				PolizaDet abonoIETU=poliza.agregarPartida();
				abonoIETU.setCuenta(getCuenta("903"));
				abonoIETU.setHaber(importe.multiply(BigDecimal.valueOf(1)));
				abonoIETU.setDescripcion("ACUMULABLE IETU");
				abonoIETU.setDescripcion2("IETU Acumulable OI Aju. PA");
				abonoIETU.setReferencia(origen);
				abonoIETU.setReferencia2((String)row.get("SUCURSAL"));
				abonoIETU.setAsiento(asiento);
			}
		}
		
		sql="SELECT (SELECT s.nombre FROM sw_sucursales s where s.SUCURSAL_ID=a.SUCURSAL_ID) as SUCURSAL" +
		" ,SUM(a.DIFERENCIA) as TOTAL"
		+",(SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID) as ORIGEN"
		+" FROM sx_cxc_abonos a " +
		" where A.diferencia_fecha=? AND A.SAF<>A.DIFERENCIA_FECHA" +
		"   AND A.diferencia<>0 " +
		"   AND A.TIPO_ID<>'PAGO_HXE' " +
		"   AND (SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID)=?"
		+" GROUP BY SUCURSAL_ID,(SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID)";

		params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
				,new SqlParameterValue(Types.VARCHAR,origen)
		};
		
		rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		for(Map<String,Object> row:rows){
			String origenRow=(String)row.get("ORIGEN");
			if(origenRow.equals(origen)){
				PolizaDet abonoAProductos=poliza.agregarPartida();
				abonoAProductos.setCuenta(getCuenta("702"));
				abonoAProductos.setHaber((BigDecimal)row.get("TOTAL"));
				abonoAProductos.setDescripcion("OTROS INGRESOS");
				abonoAProductos.setDescripcion2("Ajustes automaticos menores a $10 SAF");
				abonoAProductos.setReferencia(origen);
				abonoAProductos.setReferencia2((String)row.get("SUCURSAL"));
				abonoAProductos.setAsiento(asiento);
				
				PolizaDet cargoASAF=poliza.agregarPartida();
				cargoASAF.setCuenta(getCuenta("203"));
				cargoASAF.setDebe((BigDecimal)row.get("TOTAL"));
				cargoASAF.setDescripcion("ACREEDORES DIVERSOS");
				cargoASAF.setDescripcion2("Ajustes Otros Prod. SAF");
				cargoASAF.setReferencia(origen);
				cargoASAF.setReferencia2((String)row.get("SUCURSAL"));
				cargoASAF.setAsiento(asiento);
			}
		}
	}
	
	private  void registrarAnticipos(String asiento){
		String sql="select 'ANTICIPO' AS TIPO,A.ABONO_ID AS ORIGEN_ID" +
				",A.FECHA" +
				",(SELECT S.NOMBRE FROM sw_sucursales S WHERE A.SUCURSAL_ID=S.SUCURSAL_ID) AS SUCURSAL " +
				",A.ABONO_ID" +
				",A.ORIGEN" +
				",concat('anticipo (',substr(a.TIPO_ID,6,3),') de cte.:',a.nombre) as CONCEPTO" +
				",(TOTAL) AS IMPORTE" +
				",(SELECT C.DESCRIPCION FROM sw_cuentas C WHERE C.ID=A.CUENTA_ID) AS BANCO" +
				",substr(a.TIPO_ID,6,3) AS DESCRIPCION " +
				"from sx_cxc_abonos a where A.ORIGEN=\'JUR\' AND A.TIPO_ID<>\'PAGO_TAR\' " +
				"AND a.FECHA=? AND A.TIPO_ID LIKE 'PAGO%' AND A.ANTICIPO IS TRUE";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
		};	
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		for(Map<String,Object> row:rows){
			
			/*
			 * Inicio Modificacion de Anticipo "Total" = Importe antes de IVA y su cuenta de IVA CPG
			 */
			BigDecimal total= (BigDecimal)row.get("IMPORTE");
			BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total);
			BigDecimal impuesto=MonedasUtils.calcularImpuesto(importe);
			
			PolizaDet abonoAAnticiposDeClientes=poliza.agregarPartida();
			abonoAAnticiposDeClientes.setCuenta(getCuenta("204"));
			abonoAAnticiposDeClientes.setHaber(importe);
			abonoAAnticiposDeClientes.setDescripcion("ANTICIPOS DE CLIENTES");
			abonoAAnticiposDeClientes.setDescripcion2((String)row.get("CONCEPTO"));
			abonoAAnticiposDeClientes.setReferencia((String)row.get("ORIGEN"));
			abonoAAnticiposDeClientes.setReferencia2((String)row.get("SUCURSAL"));
			abonoAAnticiposDeClientes.setAsiento(asiento);
			
			//Cargo  a IVA de Anticipos
			PolizaDet cargoaIvaAnticipo=poliza.agregarPartida();
			cargoaIvaAnticipo.setCuenta(getCuenta("206"));
			cargoaIvaAnticipo.setDescripcion(IVA_EN_ANTICIPO);
			cargoaIvaAnticipo.setDescripcion2("IVA Trasladado en Anticipos");
			cargoaIvaAnticipo.setHaber(impuesto);
			cargoaIvaAnticipo.setReferencia((String)row.get("ORIGEN"));
			cargoaIvaAnticipo.setReferencia2((String)row.get("SUCURSAL"));
			cargoaIvaAnticipo.setAsiento(asiento);
			
			//IETU ANTICIPO 
			PolizaDet cargoIETUaAnticipos=poliza.agregarPartida();
			cargoIETUaAnticipos.setCuenta(getCuenta("902"));
			cargoIETUaAnticipos.setDebe(importe);
			cargoIETUaAnticipos.setDescripcion("ACUMULABLE IETU CLIENTES ANTICIPO");
			cargoIETUaAnticipos.setDescripcion2("IETU Acumulable Anticipo Cliente");
			cargoIETUaAnticipos.setReferencia((String)row.get("ORIGEN"));
			cargoIETUaAnticipos.setReferencia2((String)row.get("SUCURSAL"));
			cargoIETUaAnticipos.setAsiento(asiento);
			
			PolizaDet abonoIETUaAnticipos=poliza.agregarPartida();
			abonoIETUaAnticipos.setCuenta(getCuenta("903"));
			abonoIETUaAnticipos.setHaber(importe);
			abonoIETUaAnticipos.setDescripcion("IETU ACUMULABLE CLIENTES ANTICIPO");
			abonoIETUaAnticipos.setDescripcion2("IETU Acumulable Anticipo Cliente");
			abonoIETUaAnticipos.setReferencia((String)row.get("ORIGEN"));
			abonoIETUaAnticipos.setReferencia2((String)row.get("SUCURSAL"));
			abonoIETUaAnticipos.setAsiento(asiento);
		
		
		}
	}
	
	
	
	
	private  void registrarIETU(){		
		final String asiento="IETU";
		
		
		String SQL="select 'APLICACION' AS TIPO,A.APLICACION_ID AS ORIGEN_ID,A.FECHA" +
			",A.CAR_SUCURSAL AS SUCURSAL,A.ABONO_ID,a.CAR_ORIGEN AS ORIGEN" +
			",a.ABN_DESCRIPCION as CONCEPTO,(importe) AS IMPORTE" +
			",(SELECT C.DESCRIPCION FROM sx_cxc_abonos X JOIN sw_cuentas C ON(C.ID=X.CUENTA_ID) WHERE X.ABONO_ID=A.ABONO_ID) AS BANCO" +
			",substr(a.ABN_DESCRIPCION,1,3) AS DESCRIPCION" +
			" from sx_cxc_aplicaciones a" +
			" where a.fecha=? " +
			"   and a.CAR_ORIGEN=? " +
			"   AND substr(a.ABN_DESCRIPCION,1,3)<>\'TAR\'" +
			"   AND substr(a.ABN_DESCRIPCION,1,3)<>\'AJU\'" +
			"   AND substr(a.ABN_DESCRIPCION,1,3)<>\'SAF\'" +
			"   AND A.TIPO=\'PAGO\'";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
				,new SqlParameterValue(Types.VARCHAR,"JUR")
				};
		List<CCobranza> res=ServiceLocator2.getJdbcTemplate().query(SQL, params, new BeanPropertyRowMapper(CCobranza.class));
		
		BigDecimal importe=BigDecimal.ZERO;
		
		for(CCobranza cc:res){
			importe=importe.add(cc.getImporte());
		}
		importe=MonedasUtils.calcularImporteDelTotal(importe, 2);
		
		String sucursal="OFICINAS";
		String descripcion="IETU Acumulable de cobranza ";
		PolizaDet cargoIETU=poliza.agregarPartida();
		cargoIETU.setCuenta(getCuenta("902"));
		cargoIETU.setDebe(importe);
		cargoIETU.setDescripcion("ACUMULABLE IETU");
		cargoIETU.setDescripcion2(descripcion);
		cargoIETU.setReferencia("CRE");
		cargoIETU.setReferencia2(sucursal);
		cargoIETU.setAsiento(asiento);
		
		PolizaDet abonoIETU=poliza.agregarPartida();
		abonoIETU.setCuenta(getCuenta("903"));
		abonoIETU.setHaber(importe);
		abonoIETU.setDescripcion("IETU ACUMULABLE");
		abonoIETU.setDescripcion2(descripcion);
		abonoIETU.setReferencia("CRE");
		abonoIETU.setReferencia2(sucursal);
		abonoIETU.setAsiento(asiento);
	}
	
	
	
	public BigDecimal calcularSAFDisp(String sucursal){
		
		String sql="select 'SALDO_A_FAVOR_DISP' AS TIPO,(SELECT S.NOMBRE FROM sw_sucursales S WHERE A.SUCURSAL_ID=S.SUCURSAL_ID) AS SUCURSAL"
			+",SUM((A.TOTAL-IFNULL((SELECT (X.DIFERENCIA) FROM sx_cxc_abonos X WHERE X.ABONO_ID=A.ABONO_ID AND X.DIFERENCIA_FECHA=A.SAF),0)-IFNULL((SELECT SUM(X.IMPORTE) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID AND X.fecha=A.SAF),0) ))AS IMPORTE"
			+" from sx_cxc_abonos a where a.SAF=? AND A.TIPO_ID LIKE 'PAGO%' AND (SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID AND X.FECHA=A.SAF) ='JUR'"
			+" GROUP BY (SELECT S.NOMBRE FROM sw_sucursales S WHERE A.SUCURSAL_ID=S.SUCURSAL_ID)"
			+" HAVING SUM((A.TOTAL-IFNULL((SELECT (X.DIFERENCIA) FROM sx_cxc_abonos X WHERE X.ABONO_ID=A.ABONO_ID AND X.DIFERENCIA_FECHA=A.SAF),0)-IFNULL((SELECT SUM(X.IMPORTE) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID AND X.fecha=A.SAF),0) ))>0";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
						};
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		BigDecimal ivaSAF= new BigDecimal(0);
		
		for(Map<String,Object> row:rows){
			if (sucursal.equals(row.get("SUCURSAL"))){
				ivaSAF=(BigDecimal)row.get("IMPORTE");
			}
			System.out.println("Calc.SAF :" + ivaSAF+ " Suc.:"+sucursal);
		}
		return ivaSAF;
		
	}
	
	private void registrarSaldosAFavor(String origen,String asiento){
		//final String asiento="VENTAS MOS";
		String sql="select 'SALDO_A_FAVOR' AS TIPO,A.ABONO_ID AS ORIGEN_ID,A.SAF AS FECHA,(SELECT S.NOMBRE FROM sw_sucursales S WHERE A.SUCURSAL_ID=S.SUCURSAL_ID) AS SUCURSAL,A.ABONO_ID"
			+",(SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID) AS ORIGEN" 
			+ ",concat('Saldo a Favor del cte.:',a.nombre,' en',substr(a.TIPO_ID,6,3)) as CONCEPTO"
			+",(A.TOTAL-IFNULL((SELECT (X.DIFERENCIA) FROM sx_cxc_abonos X WHERE X.ABONO_ID=A.ABONO_ID AND X.DIFERENCIA_FECHA=A.SAF),0)-IFNULL((SELECT SUM(X.IMPORTE) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID AND X.fecha=A.SAF),0) )AS IMPORTE"
			+",(SELECT C.DESCRIPCION FROM sw_cuentas C WHERE C.ID=A.CUENTA_ID) AS BANCO,substr(a.TIPO_ID,6,3) AS DESCRIPCION"
			+" from sx_cxc_abonos a where a.SAF=? AND A.TIPO_ID LIKE 'PAGO%' AND (SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID) =? AND "
			+" (A.TOTAL-IFNULL((SELECT (X.DIFERENCIA) FROM sx_cxc_abonos X WHERE X.ABONO_ID=A.ABONO_ID AND X.DIFERENCIA_FECHA=A.SAF),0)-IFNULL((SELECT SUM(X.IMPORTE) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID AND X.fecha=A.SAF),0) )>0";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
				,new SqlParameterValue(Types.VARCHAR,origen)
		};
		
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		for(Map<String,Object> row:rows){
			String origenRow=(String)row.get("ORIGEN");
			if(origen.equals(origenRow)){
				
				/*
				 * Inicio Modificacion de Anticipo "Total" = Importe antes de IVA y su cuenta de IVA CPG
				 */
				BigDecimal total= (BigDecimal)row.get("IMPORTE");
				BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total);
				BigDecimal impuesto=MonedasUtils.calcularImpuesto(importe);
				
				
				PolizaDet abonoAAcredoresDiversos=poliza.agregarPartida();
				abonoAAcredoresDiversos.setCuenta(getCuenta("203"));
				abonoAAcredoresDiversos.setHaber((BigDecimal)row.get("IMPORTE"));
				abonoAAcredoresDiversos.setDescripcion("Saldo a Favor");
				abonoAAcredoresDiversos.setDescripcion2((String)row.get("CONCEPTO"));
				abonoAAcredoresDiversos.setReferencia(origen);
				abonoAAcredoresDiversos.setReferencia2((String)row.get("SUCURSAL"));
				abonoAAcredoresDiversos.setAsiento(asiento);
				
				//Cargo  a IVA de Saldo a Favor
				PolizaDet cargoaIvaAnticipo=poliza.agregarPartida();
				cargoaIvaAnticipo.setCuenta(getCuenta("206"));
				cargoaIvaAnticipo.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
				cargoaIvaAnticipo.setDescripcion2("IVA por Trasladar Saldo a Favor");
				cargoaIvaAnticipo.setDebe(impuesto.multiply(new BigDecimal(-1)));
				cargoaIvaAnticipo.setReferencia(origen);
				cargoaIvaAnticipo.setReferencia2((String)row.get("SUCURSAL"));
				cargoaIvaAnticipo.setAsiento(asiento);
				
				//Abono  a IVA de Saldo a Favor
				PolizaDet abonoaIvaAnticipo=poliza.agregarPartida();
				abonoaIvaAnticipo.setCuenta(getCuenta("206"));
				abonoaIvaAnticipo.setDescripcion(IVA_EN_VENTAS);
				abonoaIvaAnticipo.setDescripcion2("IVA Trasladado Saldo a Favor");
				abonoaIvaAnticipo.setHaber(impuesto.multiply(new BigDecimal(-1)));
				abonoaIvaAnticipo.setReferencia(origen);
				abonoaIvaAnticipo.setReferencia2((String)row.get("SUCURSAL"));
				abonoaIvaAnticipo.setAsiento(asiento);
				
				
				//EITU de Saldo a Favor
				PolizaDet cargoIETU=poliza.agregarPartida();
				cargoIETU.setCuenta(getCuenta("902"));
				cargoIETU.setDebe(importe);
				cargoIETU.setDescripcion("ACUMULABLE IETU");
				cargoIETU.setDescripcion2("IETU Deducible por saldo a favor");
				cargoIETU.setReferencia(origen);
				cargoIETU.setReferencia2((String)row.get("SUCURSAL"));
				cargoIETU.setAsiento(asiento);
				
				PolizaDet abonoIETU=poliza.agregarPartida();
				abonoIETU.setCuenta(getCuenta("903"));
				abonoIETU.setHaber(importe);
				abonoIETU.setDescripcion("IETU ACUMULABLE");
				abonoIETU.setDescripcion2("IETU Deducible por saldo a favor");
				abonoIETU.setReferencia(origen);
				abonoIETU.setReferencia2((String)row.get("SUCURSAL"));
				abonoIETU.setAsiento(asiento);			
				
			}
		}
	}
		
	private void registrarSaldosAFavorDeOtrasCarteras(){
		String SQL="select 'APLICACION' AS TIPO,A.APLICACION_ID AS ORIGEN_ID,A.FECHA ,A.CAR_SUCURSAL AS SUCURSAL,A.ABONO_ID" +
				",a.CAR_ORIGEN AS ORIGEN ,a.ABN_DESCRIPCION as CONCEPTO,(A.importe) AS IMPORTE " +
				",substr(trim(a.ABN_DESCRIPCION),1,3) AS DESCRIPCION  " +
				",(SELECT Y.CAR_ORIGEN FROM sx_cxc_aplicaciones Y WHERE Y.ABONO_ID=X.ABONO_ID AND Y.FECHA=X.SAF) AS BANCO" +
				" from sx_cxc_aplicaciones a  JOIN sx_cxc_abonos X ON(A.ABONO_ID=X.ABONO_ID) " +
				" where a.fecha=? AND A.CAR_ORIGEN='JUR' AND A.CAR_ORIGEN <>(SELECT MAX(Y.CAR_ORIGEN) FROM sx_cxc_aplicaciones Y WHERE Y.ABONO_ID=X.ABONO_ID AND Y.FECHA=X.SAF) " +
				" AND substr(a.ABN_DESCRIPCION,1,3)='SAF' AND A.TIPO='PAGO'";
		EventList<CIngresoContado> depositosConciliados=GlazedLists.eventList(ServiceLocator2.getJdbcTemplate().query(SQL
				,new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())}
				,new BeanPropertyRowMapper(CIngresoContado.class)
				));
		String asiento="CXC TRAMITE JURIDICO"; 
		for(CIngresoContado c:depositosConciliados){
			BigDecimal total=c.getIMPORTE();
			
			PolizaDet cargo=poliza.agregarPartida();
			cargo.setCuenta(getCuenta("203"));
			cargo.setDebe(total);
			cargo.setDescripcion("ACREEDORES DIVERSOS ("+c.getBANCO()+ ")");
			cargo.setDescripcion2("SALDO A FAVOR DE ORIGEN: "+c.getBANCO());
			cargo.setReferencia(c.getBANCO());
			cargo.setReferencia2(c.getSUCURSAL());
			cargo.setAsiento(asiento);
			
			PolizaDet abono=poliza.agregarPartida();
			abono.setCuenta(getCuenta("203"));
			abono.setHaber(total);
			abono.setDescripcion("ACREEDORES DIVERSOS ("+c.getORIGEN() + ")");
			abono.setDescripcion2("TRASPASO DE SALDO A FAVOR DE ORIGEN: "+c.getBANCO());
			abono.setReferencia(c.getORIGEN());
			abono.setReferencia2(c.getSUCURSAL());
			abono.setAsiento(asiento);
		}
	}
	
	
	private void registrarCobranza(){
		
		String sql="SELECT 'DEPOSITO' AS TIPO,a.ABONO_ID AS ORIGEN_ID,B.FECHA,S.NOMBRE AS SUCURSAL,b.CARGOABONO_ID "+
		",case when B.ORIGEN='VENTA_CAMIONETA' THEN 'CAM' WHEN B.ORIGEN='VENTA_MOSTRADOR' THEN 'MOS' ELSE 'N/D' END AS ORIGEN "+
		",CASE WHEN A.TRANSFERENCIA<>0 THEN CONCAT('TRANSFERENCIA','-',(SELECT X.DESCRIPCION FROM sw_cuentas X  WHERE X.id=B.CUENTA_ID),' REF:',IFNULL(A.REFERENCIA,'')) ELSE CONCAT('DEPOSITO','-',(SELECT X.DESCRIPCION FROM sw_cuentas X  WHERE X.id=B.CUENTA_ID),' REF:',IFNULL(A.REFERENCIA,'')) END AS CONCEPTO "+
		",B.IMPORTE,(SELECT X.DESCRIPCION FROM sw_cuentas X  WHERE X.id=B.CUENTA_ID) AS BANCO "+ 
		" ,CASE WHEN A.TRANSFERENCIA<>0 THEN 'TRANSFERENCIA' ELSE 'DEPOSITO' END DESCRIPCION "+  
		" FROM sw_bcargoabono b join sx_cxc_abonos a on(a.ABONO_ID=b.PAGO_ID) JOIN sw_sucursales S ON(S.SUCURSAL_ID=B.SUCURSAL_ID) " +
		" where b.fecha=? and b.ORIGEN IN('JUR') and b.conciliado=true";
		EventList<CIngresoContado> depositosConciliados=GlazedLists.eventList(ServiceLocator2.getJdbcTemplate().query(sql
				,new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())}
				,new BeanPropertyRowMapper(CIngresoContado.class)
				));
		String asiento="CXC Juridico (Conciliados)";
		
		for(CIngresoContado c:depositosConciliados){
			BigDecimal total=c.getIMPORTE();
			
			PolizaDet abonoCliente=poliza.agregarPartida();
			abonoCliente.setCuenta(getCuenta("114"));
			abonoCliente.setHaber(total);
			abonoCliente.setDescripcion("CUENTAS EN TRAMITE JURIDICO");
			abonoCliente.setDescripcion2(c.getCONCEPTO());
			abonoCliente.setReferencia(c.getORIGEN());
			abonoCliente.setReferencia2(c.getSUCURSAL());
			abonoCliente.setAsiento(asiento);
			
		}
		Map<String,BigDecimal> totalConciliadoPorSucursal=new HashMap<String, BigDecimal>();
		Comparator<CIngresoContado> comparator=GlazedLists.beanPropertyComparator(CIngresoContado.class, "SUCURSAL");
		GroupingList<CIngresoContado> depositosConciladosPorSucursal=new GroupingList<CIngresoContado>(depositosConciliados,comparator);
		for(List<CIngresoContado> depositos:depositosConciladosPorSucursal){
			
			BigDecimal totalConciliado=BigDecimal.valueOf(0);
			BigDecimal importe=BigDecimal.valueOf(0);
			BigDecimal iva=BigDecimal.valueOf(0);
			
			for(CIngresoContado c:depositos){
				totalConciliado=totalConciliado.add(c.getIMPORTE());								
			}
			importe=MonedasUtils.calcularImporteDelTotal(totalConciliado);
			iva=MonedasUtils.calcularImpuesto(importe);
			if(totalConciliado.doubleValue()>0){
				CIngresoContado c=depositos.get(0);
				totalConciliadoPorSucursal.put(c.getSUCURSAL(), totalConciliado);
				PolizaDet cargoAcredores=poliza.agregarPartida();
				cargoAcredores.setCuenta(getCuenta("203"));
				cargoAcredores.setDebe(importe);
				cargoAcredores.setDescripcion("ACREEDORES DIVERSOS");
				cargoAcredores.setDescripcion2("Dep. Conciliados");
				cargoAcredores.setReferencia(c.getORIGEN());
				cargoAcredores.setReferencia2(c.getSUCURSAL());
				cargoAcredores.setAsiento(asiento);
				
				PolizaDet cargoIva=poliza.agregarPartida();
				cargoIva.setCuenta(getCuenta("206"));
				cargoIva.setDebe(iva);
				cargoIva.setDescripcion(IVA_EN_DEV_VTAS_PENDIENTE);
				cargoIva.setDescripcion2("Iva por Trasladado Dep. Conciliados");
				cargoIva.setReferencia(c.getORIGEN());
				cargoIva.setReferencia2(c.getSUCURSAL());
				cargoIva.setAsiento(asiento);
				
				PolizaDet cargoIETU=poliza.agregarPartida();
				cargoIETU.setCuenta(getCuenta("902"));
				cargoIETU.setDebe(importe.multiply(BigDecimal.valueOf(-1)));
				cargoIETU.setDescripcion("IETU ACUMULABLE");
				cargoIETU.setDescripcion2("IETU Acumulable Dep. Conciliados");
				cargoIETU.setReferencia(c.getORIGEN());
				cargoIETU.setReferencia2(c.getSUCURSAL());
				cargoIETU.setAsiento(asiento);
				
				PolizaDet abonoIETU=poliza.agregarPartida();
				abonoIETU.setCuenta(getCuenta("903"));
				abonoIETU.setHaber(importe.multiply(BigDecimal.valueOf(-1)));
				abonoIETU.setDescripcion("ACUMULABLE IETU");
				abonoIETU.setDescripcion2("IETU Acumulable Dep. Conciliados");
				abonoIETU.setReferencia(c.getORIGEN());
				abonoIETU.setReferencia2(c.getSUCURSAL());
				abonoIETU.setAsiento(asiento);
			}
			
		}
		
		//Cargo a Bancos
		asiento="CXC Juridico";
		for(CIngresoContado ingreso:ingresos){
			if(ingreso.getORIGEN().equals("JUR")){
				String concepto=ingreso.getCONCEPTO();
				if( concepto.startsWith(TipoDeAplicacion.COMISION_AMEX.name())
						 ||concepto.startsWith(TipoDeAplicacion.COMISION_CREDITO.name())
						 ||concepto.startsWith(TipoDeAplicacion.COMISION_DEBITO.name()) 
						 ||concepto.startsWith(TipoDeAplicacion.IMPUESTO.name())
						 )
						continue;
				PolizaDet cargoABancos=poliza.agregarPartida();
				cargoABancos.setCuenta(getCuenta("102"));
				cargoABancos.setDebe(ingreso.getIMPORTE().abs());
				cargoABancos.setDescripcion(ingreso.getBANCO());	
				cargoABancos.setDescripcion2(ingreso.getCONCEPTO());	
				cargoABancos.setReferencia(ingreso.getORIGEN());
				cargoABancos.setReferencia2(ingreso.getSUCURSAL());
				cargoABancos.setAsiento(asiento);
				
				/*Cargo a clientes
				PolizaDet abonoAClientes=poliza.agregarPartida();
				CuentaContable clientes=getCuenta("106");
					
				abonoAClientes.setCuenta(clientes);
				abonoAClientes.setHaber(ingreso.getIMPORTE().abs());
				abonoAClientes.setDescripcion("CLIENTES");
				abonoAClientes.setReferencia(ingreso.getORIGEN());
				abonoAClientes.setReferencia2(ingreso.getSUCURSAL());
				abonoAClientes.setAsiento(asiento);
				*/
				
			}
		}
		// Abono a Clientes 
		String SQL="select 'APLICACION' AS TIPO,A.APLICACION_ID AS ORIGEN_ID,A.FECHA" +
				",A.CAR_SUCURSAL AS SUCURSAL,A.ABONO_ID,a.CAR_ORIGEN AS ORIGEN" +
				",a.ABN_DESCRIPCION as CONCEPTO,(importe) AS IMPORTE" +
				",(SELECT C.DESCRIPCION FROM sx_cxc_abonos X JOIN sw_cuentas C ON(C.ID=X.CUENTA_ID) WHERE X.ABONO_ID=A.ABONO_ID) AS BANCO" +
				",substr(trim(a.ABN_DESCRIPCION),1,3) AS DESCRIPCION" +
				" from sx_cxc_aplicaciones a" +
				" where a.fecha=? " +
				"   and a.CAR_ORIGEN=? " +
				"   AND substr(a.ABN_DESCRIPCION,1,3)<>\'AJU\'" +
				"   AND A.TIPO=\'PAGO\'";
		Object[] params=new Object[]{
					new SqlParameterValue(Types.DATE,poliza.getFecha())
					,new SqlParameterValue(Types.VARCHAR,"JUR")
					};
		List<CCobranza> res=ServiceLocator2.getJdbcTemplate().query(SQL, params, new BeanPropertyRowMapper(CCobranza.class));
				
		BigDecimal importe=BigDecimal.ZERO;
		BigDecimal saf=BigDecimal.ZERO;
		BigDecimal anticipos=BigDecimal.ZERO;
		BigDecimal tar=BigDecimal.ZERO;
				
		for(CCobranza cc:res){
			importe=importe.add(cc.getImporte());
			if(cc.getDescripcion().equalsIgnoreCase("SAF")){
				saf=cc.getImporte();
			}
			if(cc.getDescripcion().equalsIgnoreCase("ANT")){
				anticipos=cc.getImporte();
			}					
			if(cc.getDescripcion().equalsIgnoreCase("TAR")){
				tar=tar.add(cc.getImporte());
			}					
		}
		BigDecimal impuestoSAF=MonedasUtils.calcularImpuestoDelTotal(saf);
		impuestoSAF=CantidadMonetaria.pesos(impuestoSAF).amount();					
		BigDecimal importeAnticipo=MonedasUtils.calcularImporteDelTotal(anticipos,6);
		BigDecimal impuestoAnticipo=MonedasUtils.calcularImpuesto(importeAnticipo);
		importeAnticipo=CantidadMonetaria.pesos(importeAnticipo).amount();
		impuestoAnticipo=CantidadMonetaria.pesos(impuestoAnticipo).amount();
					
		BigDecimal impuestoTAR=MonedasUtils.calcularImpuestoDelTotal(tar);
		impuestoTAR=CantidadMonetaria.pesos(impuestoSAF).amount();
					
		String sucursal="OFICINAS";
		String descripcion="CUENTAS EN TRAMITE JURIDICO";
		PolizaDet abonoaClientes=poliza.agregarPartida();
		abonoaClientes.setCuenta(getCuenta("114"));
		BigDecimal totalConciliado=totalConciliadoPorSucursal.get(sucursal);
		if(totalConciliado==null)
			totalConciliado=BigDecimal.ZERO;
		abonoaClientes.setHaber(importe.subtract(totalConciliado));
		abonoaClientes.setDescripcion(descripcion);
		abonoaClientes.setDescripcion2("Juridico (Cobranza)");
		abonoaClientes.setReferencia("JUR");
		abonoaClientes.setReferencia2(sucursal);
		abonoaClientes.setAsiento(asiento);
				
		BigDecimal impuesto=MonedasUtils.calcularImpuestoDelTotal(importe);
		impuesto=impuesto.subtract(impuestoSAF).subtract(impuestoAnticipo).add(MonedasUtils.calcularImpuestoDelTotal(calcularSAFDisp(sucursal)));
		impuesto=CantidadMonetaria.pesos(impuesto).amount();	
		PolizaDet abonoAIvaTrasladado=poliza.agregarPartida();
		abonoAIvaTrasladado.setCuenta(getCuenta("206"));
		abonoAIvaTrasladado.setHaber(impuesto.subtract(MonedasUtils.calcularImpuestoDelTotal(totalConciliado)).subtract(impuestoTAR));
		abonoAIvaTrasladado.setDescripcion(IVA_EN_VENTAS);
		abonoAIvaTrasladado.setDescripcion2("IVA Trasladado (Cobranza)");
		abonoAIvaTrasladado.setReferencia("JUR");
		abonoAIvaTrasladado.setReferencia2(sucursal);
		abonoAIvaTrasladado.setAsiento(asiento);
					
		PolizaDet cargoAIvaEnVentas=poliza.agregarPartida();
		cargoAIvaEnVentas.setCuenta(getCuenta("206"));
		cargoAIvaEnVentas.setDebe(impuesto.subtract(MonedasUtils.calcularImpuestoDelTotal(totalConciliado)).subtract(impuestoTAR));
		cargoAIvaEnVentas.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
		cargoAIvaEnVentas.setDescripcion2("IVA por Trasladar (Cobranza)");
		cargoAIvaEnVentas.setReferencia("JUR");
		cargoAIvaEnVentas.setReferencia2(sucursal);
		cargoAIvaEnVentas.setAsiento(asiento);
					
		if(tar.doubleValue()>0){
			PolizaDet cargoAcredDiversTarj=poliza.agregarPartida();
			cargoAcredDiversTarj.setCuenta(getCuenta("203"));
			cargoAcredDiversTarj.setDebe(tar);
			cargoAcredDiversTarj.setDescripcion("ACREEDORES DIVERSOS");
			cargoAcredDiversTarj.setDescripcion2("Aplicacion de Tarjeta JUR");
			cargoAcredDiversTarj.setReferencia("JUR");
			cargoAcredDiversTarj.setReferencia2(sucursal);
			cargoAcredDiversTarj.setAsiento(asiento);
		}
		//Cargo a Acredores diversos de aplicaciones de SAF					
		if(saf.doubleValue()>0){
			PolizaDet cargoSaldosAFavor=poliza.agregarPartida();
			cargoSaldosAFavor.setCuenta(getCuenta("203"));
			cargoSaldosAFavor.setDebe(saf);
			cargoSaldosAFavor.setDescripcion("ACREEDORES DIVERSOS");
			cargoSaldosAFavor.setDescripcion2("Aplicacion de Saldo a Favor");
			cargoSaldosAFavor.setReferencia("JUR");
			cargoSaldosAFavor.setReferencia2(sucursal);
			cargoSaldosAFavor.setAsiento(asiento);
						
			PolizaDet abonoAIvaTrasladadoSAF=poliza.agregarPartida();
			abonoAIvaTrasladadoSAF.setCuenta(getCuenta("206"));
			abonoAIvaTrasladadoSAF.setHaber(impuestoSAF);
			abonoAIvaTrasladadoSAF.setDescripcion(IVA_EN_VENTAS);
			abonoAIvaTrasladadoSAF.setDescripcion2("IVA Trasladado SAF");
			abonoAIvaTrasladadoSAF.setReferencia("JUR");
			abonoAIvaTrasladadoSAF.setReferencia2(sucursal);
			abonoAIvaTrasladadoSAF.setAsiento(asiento);
						
			PolizaDet cargoAIvaEnVentasSAF=poliza.agregarPartida();
			cargoAIvaEnVentasSAF.setCuenta(getCuenta("206"));
			cargoAIvaEnVentasSAF.setDebe(impuestoSAF);
			cargoAIvaEnVentasSAF.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
			cargoAIvaEnVentasSAF.setDescripcion2("IVA por Trasladar SAF");
			cargoAIvaEnVentasSAF.setReferencia("JUR");
			cargoAIvaEnVentasSAF.setReferencia2(sucursal);
			cargoAIvaEnVentasSAF.setAsiento(asiento);
		}
		if(anticipos.doubleValue()>0){
			PolizaDet cargoAAnticipos=poliza.agregarPartida();
			cargoAAnticipos.setCuenta(getCuenta("204"));
			cargoAAnticipos.setDebe(importeAnticipo);
			cargoAAnticipos.setDescripcion("ANTICIPOS DE CLIENTES");
			cargoAAnticipos.setDescripcion2("Aplicacion de Anticipo");
			cargoAAnticipos.setReferencia("JUR");
			cargoAAnticipos.setReferencia2(sucursal);
			cargoAAnticipos.setAsiento(asiento);
						
			PolizaDet cargoAIvaTrasladadoAnticipo=poliza.agregarPartida();
			cargoAIvaTrasladadoAnticipo.setCuenta(getCuenta("206"));
			cargoAIvaTrasladadoAnticipo.setDebe(impuestoAnticipo);
			cargoAIvaTrasladadoAnticipo.setDescripcion(IVA_EN_ANTICIPO);
			cargoAIvaTrasladadoAnticipo.setDescripcion2("IVA en Anticipo");
			cargoAIvaTrasladadoAnticipo.setReferencia("JUR");
			cargoAIvaTrasladadoAnticipo.setReferencia2(sucursal);
			cargoAIvaTrasladadoAnticipo.setAsiento(asiento);			
						
			PolizaDet abonoAIvaTrasladadoAnt=poliza.agregarPartida();
			abonoAIvaTrasladadoAnt.setCuenta(getCuenta("206"));
			abonoAIvaTrasladadoAnt.setHaber(impuestoAnticipo);
			abonoAIvaTrasladadoAnt.setDescripcion(IVA_EN_VENTAS);
			abonoAIvaTrasladadoAnt.setDescripcion2("IVA Trasladado Anticipo");
			abonoAIvaTrasladadoAnt.setReferencia("JUR");
			abonoAIvaTrasladadoAnt.setReferencia2(sucursal);
			abonoAIvaTrasladadoAnt.setAsiento(asiento);
						
			PolizaDet cargoAIvaPorTrasladarAnt=poliza.agregarPartida();
			cargoAIvaPorTrasladarAnt.setCuenta(getCuenta("206"));
			cargoAIvaPorTrasladarAnt.setDebe(impuestoAnticipo);
			cargoAIvaPorTrasladarAnt.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
			cargoAIvaPorTrasladarAnt.setDescripcion2("IVA por Trasladar Anticipo");
			cargoAIvaPorTrasladarAnt.setReferencia("JUR");
			cargoAIvaPorTrasladarAnt.setReferencia2(sucursal);
			cargoAIvaPorTrasladarAnt.setAsiento(asiento);
						
			PolizaDet cargoIETUaAnticipos=poliza.agregarPartida();
			cargoIETUaAnticipos.setCuenta(getCuenta("902"));
			cargoIETUaAnticipos.setDebe(importeAnticipo.multiply(BigDecimal.valueOf(-1)));
			cargoIETUaAnticipos.setDescripcion("ACUMULABLE IETU CLIENTES ANTICIPO");
			cargoIETUaAnticipos.setDescripcion2("IETU Acumulable Anticipo Cliente");
			cargoIETUaAnticipos.setReferencia("JUR");
			cargoIETUaAnticipos.setReferencia2(sucursal);
			cargoIETUaAnticipos.setAsiento(asiento);
						
			PolizaDet abonoIETUaAnticipos=poliza.agregarPartida();
			abonoIETUaAnticipos.setCuenta(getCuenta("903"));
			abonoIETUaAnticipos.setHaber(importeAnticipo.multiply(BigDecimal.valueOf(-1)));
			abonoIETUaAnticipos.setDescripcion("IETU ACUMULABLE CLIENTES ANTICIPO");
			abonoIETUaAnticipos.setDescripcion2("IETU Acumulable Anticipo Cliente");
			abonoIETUaAnticipos.setReferencia("JUR");
			abonoIETUaAnticipos.setReferencia2(sucursal);
			abonoIETUaAnticipos.setAsiento(asiento);
						
			PolizaDet cargoIETUaAntCamioneta=poliza.agregarPartida();
			cargoIETUaAntCamioneta.setCuenta(getCuenta("902"));
			cargoIETUaAntCamioneta.setDebe(importeAnticipo);
			cargoIETUaAntCamioneta.setDescripcion("ACUMULABLE IETU JURIDICO");
			cargoIETUaAntCamioneta.setDescripcion2("IETU Acumulable Juridico");
			cargoIETUaAntCamioneta.setReferencia("JUR");
			cargoIETUaAntCamioneta.setReferencia2(sucursal);
			cargoIETUaAntCamioneta.setAsiento(asiento);
						
			PolizaDet abonoIETUaAntCamioneta=poliza.agregarPartida();
			abonoIETUaAntCamioneta.setCuenta(getCuenta("903"));
			abonoIETUaAntCamioneta.setHaber(importeAnticipo);
			abonoIETUaAntCamioneta.setDescripcion("IETU ACUMULABLE JURIDICO");
			abonoIETUaAntCamioneta.setDescripcion2("IETU Acumulable Juridico");
			abonoIETUaAntCamioneta.setReferencia("JUR");
			abonoIETUaAntCamioneta.setReferencia2(sucursal);
			abonoIETUaAntCamioneta.setAsiento(asiento);					
		}					
		registrarOtrosGastos("JUR",asiento);
		registrarOtrosProductos("JUR",asiento);
	}
	
	/**
	 * Se procesan las notas de credito (Por devolucion y bonificacion) 
	 * 
	 */
	private void registrarDevoluciones(){
		
		
		String SQL="select 'FAC_ORI' AS TIPO,A.VENTA_ID AS ORIGEN_ID,X.FECHA,A.SUCURSAL,A.ABONO_ID,a.ORIGEN,A.IMPORTE "+ 
		",CONCAT(substr(X.TIPO_ID,6,3),' Folio:',X.FOLIO,' Doc.:',a.DOCUMENTO) as CONCEPTO  "+
		",(SELECT C.DESCRIPCION FROM sw_cuentas C  WHERE C.ID=X.CUENTA_ID) AS BANCO "+
		",substr(X.TIPO_ID,6,3) AS DESCRIPCION  "+
		" from sx_cxc_abonos x join sx_nota_det a ON(X.ABONO_ID=A.ABONO_ID) "+
		" where X.fecha=?  and a.ORIGEN in('JUR') ";
		
/*		String SQL="select 'NOTAS' AS TIPO,A.ABONO_ID AS ORIGEN_ID,A.FECHA,'OFICINAS' AS SUCURSAL,A.ABONO_ID,a.ORIGEN AS ORIGEN,A.TOTAL AS IMPORTE" +
		",CONCAT(substr(a.TIPO_ID,6,3),' Folio:',A.FOLIO) as CONCEPTO" +
		",'' AS BANCO,substr(a.TIPO_ID,6,3) AS DESCRIPCION" +
		" from sx_cxc_abonos a where a.fecha=? and a.ORIGEN in(\'JUR\') AND A.TIPO_ID LIKE \'NOTA%\'";*/
		
		
		Object[] params=new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())};
		List<CCobranza> aplicaciones=ServiceLocator2.getJdbcTemplate().query(SQL, params, new BeanPropertyRowMapper(CCobranza.class));
		final String asiento="Devoluciones y descuentos"; 
		for(CCobranza cc:aplicaciones){
			String origen=cc.getOrigen();
			BigDecimal importe=MonedasUtils.calcularImporteDelTotal(cc.getImporte());
			BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
			BigDecimal total=cc.getImporte();
			
			if(origen.equals("JUR")){
				if(cc.getDescripcion().startsWith("BON")){
					// Cargo a Descuetos sobre ventas (406)
					PolizaDet cargoADescuentos=poliza.agregarPartida();
					cargoADescuentos.setCuenta(getCuenta("406"));
					cargoADescuentos.setDebe(importe);
					cargoADescuentos.setDescripcion("DESCUENTOS SOBRE VENTAS");
					cargoADescuentos.setDescripcion2(cc.getConcepto());
					cargoADescuentos.setReferencia(origen);
					cargoADescuentos.setReferencia2(cc.getSucursal());
					cargoADescuentos.setAsiento(asiento);
					
					PolizaDet cargoAIva=poliza.agregarPartida();
					cargoAIva.setCuenta(getCuenta("206"));
					cargoAIva.setDebe(iva);
					cargoAIva.setDescripcion(IVA_EN_DESC_VTAS_PENDIENTE);
					cargoAIva.setDescripcion2("Iva por Trasladar en Desc. Sobre Vtas");
					cargoAIva.setReferencia(origen);
					cargoAIva.setReferencia2(cc.getSucursal());
					cargoAIva.setAsiento(asiento);
					
					//Abono a clientes
					
					PolizaDet abonoAClientes=poliza.agregarPartida();
					abonoAClientes.setCuenta(getCuenta("114"));
					abonoAClientes.setHaber(total);
					abonoAClientes.setDescripcion("CUENTAS EN TRAMITE JURIDICO ".concat(origen.equals("JUR")?"JURIDICO":"ND"));
					abonoAClientes.setDescripcion2("Juridico ".concat(" Bonificacion"));
					abonoAClientes.setReferencia(origen);
					abonoAClientes.setReferencia2(cc.getSucursal());
					abonoAClientes.setAsiento(asiento);
					
				}

			}

		}
		
	}
	
	private void registrarNotasDeCargo(){
		final String asiento="Nota Cargo Intereses";
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from NotaDeCargo nota " +
						" where nota.fecha=?  " +
						" and nota.origen=\'JUR\'" +
						" order by nota.cliente desc";
				List<NotaDeCargo> cargos=session.createQuery(hql).setParameter(0, poliza.getFecha(),Hibernate.DATE).list();
				
				BigDecimal intereses=BigDecimal.ZERO;
				
				for(NotaDeCargo cargo:cargos){
					//AsientoContable as=new AsientoContable();	
					PolizaDet cargoANotasDeCargoCtes=poliza.agregarPartida();
					
					cargoANotasDeCargoCtes.setCuenta(getCuenta("114"));
					cargoANotasDeCargoCtes.setDebe(cargo.getTotal());
					String concepto="Nota Cargo:"+cargo.getDocumento()+" "+cargo.getNombre();
					cargoANotasDeCargoCtes.setDescripcion("CUENTAS EN TRAMITE JURIDICO");
					cargoANotasDeCargoCtes.setDescripcion2(concepto);
					cargoANotasDeCargoCtes.setReferencia(cargo.getOrigen().name());
					cargoANotasDeCargoCtes.setReferencia2(cargo.getSucursal().getNombre());
					cargoANotasDeCargoCtes.setAsiento(asiento);
					
					//poliza.getRegistros().add(as);
					intereses=intereses.add(cargo.getTotal());
				}
				if(intereses.abs().doubleValue()>0){
					
					BigDecimal importeIntereses=MonedasUtils.calcularImporteDelTotal(intereses, 4);
					BigDecimal ivaIntereses=MonedasUtils.calcularImpuesto(importeIntereses);
					importeIntereses=CantidadMonetaria.pesos(importeIntereses).amount();
					ivaIntereses=CantidadMonetaria.pesos(ivaIntereses).amount();
					
					PolizaDet abonoIntereses=poliza.agregarPartida();
					abonoIntereses.setHaber(importeIntereses);
					abonoIntereses.setCuenta(getCuenta("701"));
					abonoIntereses.setDescripcion("PRODUCTOS FINANCIEROS");
					abonoIntereses.setDescripcion2("Nota de Cargo Intereses Moratorios");
					abonoIntereses.setAsiento(asiento);
					abonoIntereses.setReferencia("JUR");
					abonoIntereses.setReferencia2("OFICINAS");
					
					//poliza.getRegistros().add(as);
					PolizaDet abonoIva=poliza.agregarPartida();
					abonoIva.setHaber(ivaIntereses);
					abonoIva.setCuenta(getCuenta("206"));
					abonoIva.setDescripcion(IVA_EN_OTROS_INGRESOS);
					abonoIva.setDescripcion2("IVA por Trasladar Intereses Moratorios");
					abonoIva.setAsiento(asiento);
					abonoIva.setReferencia("JUR");
					abonoIva.setReferencia2("OFICINAS");
				}				
				return null;
			}			
		});
	}

	private void registrarCargosTraspasoAJuridico(){
		final Date fecha=poliza.getFecha();
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				
				//String hql="from ChequeDevuelto c where c.fecha=?";
				String hql="from Juridico j where j.traspaso=? and j.importe>0";
				String asiento="Traspaso Juridico";
				
				
				BigDecimal importe=BigDecimal.ZERO;
				
				Iterator<Juridico> it=session.createQuery(hql).setParameter(0, fecha,Hibernate.DATE).iterate();
				while(it.hasNext()){
					Juridico ju=it.next();
					//PagoConCheque cheque=(PagoConCheque)ca.getPago();
					//ChequeDevuelto cheque=
					PolizaDet abono=poliza.agregarPartida();
					
					String cuentaOrigen = null;
					if(ju.getCargo().getOrigen().name().equalsIgnoreCase("CRE")){
						cuentaOrigen= "106";
					}
					if(ju.getCargo().getOrigen().name().equalsIgnoreCase("CHE")){
						cuentaOrigen= "113";
					}
					if(ju.getCargo().getOrigen().name().equalsIgnoreCase("CAM")){
						cuentaOrigen= "105";
					}
					//return cuentaOrigen;
					
					
					abono.setCuenta(getCuenta(cuentaOrigen));
				//	abono.setCuenta(getCuenta("114"));
					abono.setHaber(ju.getTotal().abs());
					abono.setDescripcion(ju.getNombre());
					abono.setDescripcion2("Doc: "+ju.getDocumento()+"Origen "+ju.getCargo().getOrigen().name()+" (TRASPASO)");
					abono.setReferencia("JUR");
					abono.setReferencia2("OFICINAS");
					abono.setAsiento(asiento);
					
					PolizaDet cargo=poliza.agregarPartida();
					cargo.setCuenta(getCuenta("114"));
					cargo.setDebe(ju.getTotal().abs());
					cargo.setDescripcion(ju.getNombre());
					cargo.setDescripcion2("Doc: "+ju.getDocumento()+"Origen "+ju.getCargo().getOrigen().name()+" (TRASPASO)");
					cargo.setReferencia("JUR");
					cargo.setReferencia2("OFICINAS");
					cargo.setAsiento(asiento);
					
					importe=importe.add(MonedasUtils.calcularImporteDelTotal(ju.getImporte(), 4));
					
				}
				
				
				BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
				
				importe=CantidadMonetaria.pesos(importe).amount();
				iva=CantidadMonetaria.pesos(iva).amount();
				/*
				PolizaDet abonoIvaVentasJur=poliza.agregarPartida();
				abonoIvaVentasJur.setCuenta(getCuenta("206"));
				abonoIvaVentasJur.setHaber(iva);
				abonoIvaVentasJur.setDescripcion(IVA_EN_VENTAS);
				abonoIvaVentasJur.setDescripcion2("IVA EN VENTAS JURIDICO");
				abonoIvaVentasJur.setReferencia("JUR");
				abonoIvaVentasJur.setReferencia2("OFICINAS");
				abonoIvaVentasJur.setAsiento(asiento);
				
				
				PolizaDet abonoIvaPendVentasJur=poliza.agregarPartida();
				abonoIvaPendVentasJur.setCuenta(getCuenta("206"));
				abonoIvaPendVentasJur.setHaber(iva.abs());
				abonoIvaPendVentasJur.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
				abonoIvaPendVentasJur.setDescripcion2("IVA PENDIENTE EN VENTAS JURIDICO");
				abonoIvaPendVentasJur.setReferencia("JUR");
				abonoIvaPendVentasJur.setReferencia2("OFICINAS");
				abonoIvaPendVentasJur.setAsiento(asiento);
				
				PolizaDet cargoIETUaJuridico=poliza.agregarPartida();
				cargoIETUaJuridico.setCuenta(getCuenta("902"));
				cargoIETUaJuridico.setDebe(importe);
				cargoIETUaJuridico.setDescripcion("ACUMULABLE IETU JURIDICO");
				cargoIETUaJuridico.setDescripcion2("IETU Acumulable Juridico");
				cargoIETUaJuridico.setReferencia("CHE");
				cargoIETUaJuridico.setReferencia2("OFICINAS");
				cargoIETUaJuridico.setAsiento(asiento);
				
				PolizaDet abonoIETUaJuridico=poliza.agregarPartida();
				abonoIETUaJuridico.setCuenta(getCuenta("903"));
				abonoIETUaJuridico.setHaber(importe);
				abonoIETUaJuridico.setDescripcion("IETU ACUMULABLE JURIDICO");
				abonoIETUaJuridico.setDescripcion2("IETU Acumulable Juridico");
				abonoIETUaJuridico.setReferencia("CHE");
				abonoIETUaJuridico.setReferencia2("OFICINAS");
				abonoIETUaJuridico.setAsiento(asiento);
				*/
				return null;
			}
			
		});
	}
	
	
	private CuentaContable getCuenta(String clave){
		return ServiceLocator2.getCuentasContablesManager().buscarPorClave(clave);
	}
	
	public Poliza salvarPoliza(Poliza target){
		return ServiceLocator2.getPolizasManager().salvarPoliza(target);
	}
	
	
			
	public boolean eliminarPoliza(Poliza poliza) {
		ServiceLocator2.getPolizasManager().getPolizaDao().remove(poliza.getId());
		return true;
		
	}
			
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		PolizaDeCuentasPorCobrarManager manager=new PolizaDeCuentasPorCobrarManager();
		manager.generarPoliza(DateUtil.toDate("01/06/2011"));
		
	}

	

}
