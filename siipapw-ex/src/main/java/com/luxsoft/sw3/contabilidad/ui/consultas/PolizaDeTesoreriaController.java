package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.model.gastos.TipoDeCompra;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.services.AbstractPolizaManager;
import com.luxsoft.sw3.contabilidad.services.PolizaContableManager;
import com.luxsoft.utils.LoggerHelper;



/**
 * Implementacion de {@link PolizaContableManager} para la generación y mantenimiento 
 * de tesoreria
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDeTesoreriaController extends AbstractPolizaManager{
	
	Logger logger=LoggerHelper.getLogger();
	
	protected void inicializarPoliza(final Date fecha){
		poliza=new Poliza();
		poliza.setFecha(fecha);
		poliza.setTipo(Poliza.Tipo.INGRESO);
		poliza.setDescripcion(MessageFormat.format("Tesorería para el: {0,date,short}",fecha));
	}
	
	protected void inicializarDatos(){}
	
	@Override
	protected void procesarPoliza() {
		registrarDepositosPorIdentificar();
		registrarMorralla();
		registrarFaltantes();
		registrarSobrantes();
		registrarRembolso();
	}
	
	private void registrarDepositosPorIdentificar(){
		
		String sql="select C.DESCRIPCION AS BANCO,B.* from sw_bcargoabono B JOIN SW_CUENTAS C ON(B.CUENTA_ID=C.ID )where fecha=? and concepto_id=737331";
		List<Map<String, Object>> rows=ServiceLocator2
			.getJdbcTemplate()
			.queryForList(sql,
					new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())}
					);
		String asiento="Depositos por identificar";
		for(Map<String, Object> row:rows){
			Number val=(Number)row.get("IMPORTE");
			BigDecimal total=new BigDecimal(val.doubleValue());
			BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total);
			BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
			
			
			PolizaDet cargoBancos=poliza.agregarPartida();
			cargoBancos.setCuenta(getCuenta("102"));
			cargoBancos.setDebe(total);
			cargoBancos.setDescripcion((String)row.get("BANCO"));
			cargoBancos.setDescripcion2("Depositos por identificar");
			cargoBancos.setReferencia("NA");
			cargoBancos.setReferencia2("NA");
			cargoBancos.setAsiento(asiento);
			
			PolizaDet abonoAcredores=poliza.agregarPartida();
			abonoAcredores.setCuenta(getCuenta("203"));
			abonoAcredores.setHaber(importe);
			abonoAcredores.setDescripcion("ACREEDORES DIVERSOS");
			abonoAcredores.setDescripcion2("Depositos por Identificar");
			abonoAcredores.setReferencia("NA");
			abonoAcredores.setReferencia2("NA");
			abonoAcredores.setAsiento(asiento);
			
			PolizaDet abonoAIva=poliza.agregarPartida();
			abonoAIva.setCuenta(getCuenta("206"));
			abonoAIva.setHaber(iva);
			abonoAIva.setDescripcion(IVA_EN_DEPOSITOS_IDENTIFICAR);
			abonoAIva.setDescripcion2("Iva en Dep. por Identificar");
			abonoAIva.setReferencia("NA");
			abonoAIva.setReferencia2("NA");
			abonoAIva.setAsiento(asiento);
			
			PolizaDet cargoIETU=poliza.agregarPartida();
			cargoIETU.setCuenta(getCuenta("902"));
			cargoIETU.setDebe(importe);
			cargoIETU.setDescripcion("IETU ACUMULABLE");
			cargoIETU.setDescripcion2("IETU Acumulable Dep. por Identificar");
			cargoIETU.setReferencia("NA");
			cargoIETU.setReferencia2("NA");
			cargoIETU.setAsiento(asiento);
			
			PolizaDet abonoIETU=poliza.agregarPartida();
			abonoIETU.setCuenta(getCuenta("903"));
			abonoIETU.setHaber(importe);
			abonoIETU.setDescripcion("ACUMULABLE IETU");
			abonoIETU.setDescripcion2("IETU Acumulable Dep. por Identificar");
			abonoIETU.setReferencia("NA");
			abonoIETU.setReferencia2("NA");
			abonoIETU.setAsiento(asiento);			
		}		
	}	
	private void registrarMorralla(){
		String sql="select C.DESCRIPCION AS BANCO,B.* from sw_bcargoabono B JOIN SW_CUENTAS C ON(B.CUENTA_ID=C.ID )where fecha=? and concepto_id in (737294,737321)";
		List<Map<String, Object>> rows=ServiceLocator2
			.getJdbcTemplate()
			.queryForList(sql,
					new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())}
					);
		String asiento="Morralla";
		for(Map<String, Object> row:rows){
			Number val=(Number)row.get("IMPORTE");
			BigDecimal total=new BigDecimal(val.doubleValue()).abs();
			//BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total);
			//BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
			Number id=(Number)row.get("SUCURSAL_ID");
			System.out.println(getSucursal(id.longValue()));
			String sucursal=id!=null?getSucursal(id.longValue()).getNombre():null;
			String banco=(String)row.get("BANCO");
			PolizaDet cargoBancos=poliza.agregarPartida();
			cargoBancos.setCuenta(getCuenta("102"));
			cargoBancos.setDebe(total);
			cargoBancos.setDescripcion(banco);
			cargoBancos.setDescripcion2("");
			cargoBancos.setReferencia(banco);
			cargoBancos.setReferencia2(sucursal);
			cargoBancos.setAsiento(asiento);
			
			PolizaDet abonoAcredores=poliza.agregarPartida();
			abonoAcredores.setCuenta(getCuenta("101"));
			abonoAcredores.setHaber(total);
			abonoAcredores.setDescripcion("MORRALLA");
			abonoAcredores.setDescripcion2("");
			abonoAcredores.setReferencia(banco);
			abonoAcredores.setReferencia2(sucursal);
			abonoAcredores.setAsiento(asiento);
		}		
	}

	private void registrarFaltantes(){ 
		String sql="select C.DESCRIPCION AS BANCO,B.* from sw_bcargoabono B JOIN SW_CUENTAS C ON(B.CUENTA_ID=C.ID )where fecha=? and concepto_id in (737312)";
		List<Map<String, Object>> rows=ServiceLocator2
			.getJdbcTemplate()
			.queryForList(sql,
					new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())}
					);
		String asiento="Faltantes";
		for(Map<String, Object> row:rows){
			Number val=(Number)row.get("IMPORTE");
			BigDecimal total=new BigDecimal(val.doubleValue()).abs();
			//BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total);
			//BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
			Number id=(Number)row.get("SUCURSAL_ID");
			
			String sucursal=id!=null?getSucursal(id.longValue()).getNombre():null;
			String banco=(String)row.get("BANCO");
			PolizaDet abono=poliza.agregarPartida();
			abono.setCuenta(getCuenta("102"));
			abono.setHaber(total);
			abono.setDescripcion(banco);
			abono.setDescripcion2("");
			abono.setReferencia(banco);
			abono.setReferencia2(sucursal);
			abono.setAsiento(asiento);
			
			PolizaDet cargo=poliza.agregarPartida();
			cargo.setCuenta(getCuenta("110"));
			cargo.setDebe(total);
			cargo.setDescripcion("FALTANTE");
			cargo.setDescripcion2("");
			cargo.setReferencia(banco);
			cargo.setReferencia2(sucursal);
			cargo.setAsiento(asiento);
		}		
	}
	
	private void registrarSobrantes(){
		String sql="select C.DESCRIPCION AS BANCO,B.* from sw_bcargoabono B JOIN SW_CUENTAS C ON(B.CUENTA_ID=C.ID )where fecha=? and concepto_id in (737313)";
		List<Map<String, Object>> rows=ServiceLocator2
			.getJdbcTemplate()
			.queryForList(sql,
					new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())}
					);
		String asiento="Sobrantes";
		for(Map<String, Object> row:rows){
			Number val=(Number)row.get("IMPORTE");
			BigDecimal total=new BigDecimal(val.doubleValue()).abs();
			//BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total);
			//BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
			Number id=(Number)row.get("SUCURSAL_ID");
			
			String sucursal=id!=null?getSucursal(id.longValue()).getNombre():null;
			String banco=(String)row.get("BANCO");
			PolizaDet cargo=poliza.agregarPartida();
			cargo.setCuenta(getCuenta("102"));
			cargo.setDebe(total);
			cargo.setDescripcion(banco);
			cargo.setDescripcion2("");
			cargo.setReferencia(banco);
			cargo.setReferencia2(sucursal);
			cargo.setAsiento(asiento);
			
			PolizaDet abono=poliza.agregarPartida();
			abono.setCuenta(getCuenta("101"));
			abono.setHaber(total);
			abono.setDescripcion("SOBRANTE");
			abono.setDescripcion2("");
			abono.setReferencia(banco);
			abono.setReferencia2(sucursal);
			abono.setAsiento(asiento);
		}		
	}
	
	private void registrarRembolso(){
		final String asiento="Rembolso";
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				ScrollableResults rs=session.createQuery("from CargoAbono c where fecha=? and c.origen='GASTOS' and c.requisicion !=null" )
						.setParameter(0, poliza.getFecha(),Hibernate.DATE)
						.scroll();
				while(rs.next()){
					CargoAbono c=(CargoAbono)rs.get()[0];
					for(RequisicionDe det:c.getRequisicion().getPartidas()){
						if(det.getFacturaDeGasto()!=null){
							if(det.getFacturaDeGasto().getCompra().getTipo().equals(TipoDeCompra.REEMBOLSO)){
								String banco=c.getCuenta().getDescripcion();
								PolizaDet abono=poliza.agregarPartida();
								abono.setCuenta(getCuenta("102"));
								abono.setHaber(c.getImporte().abs());
								abono.setDescripcion(banco);
								abono.setDescripcion2("");
								abono.setReferencia(banco);
								abono.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
								abono.setAsiento(asiento);
								
								PolizaDet cargo=poliza.agregarPartida();
								cargo.setCuenta(getCuenta("101"));
								cargo.setDebe(c.getImporte().abs());
								cargo.setDescripcion("REMBOLSO");
								cargo.setDescripcion2("");
								cargo.setReferencia(banco);
								cargo.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
								cargo.setAsiento(asiento);
							}
						}
					}
				}
				return null;
			}
		});
	}
	
	public static void main(String[] args) {
		PolizaDeTesoreriaController model=new PolizaDeTesoreriaController();
		model.generarPoliza(DateUtil.toDate("03/08/2011"));
		
	}

	
}
