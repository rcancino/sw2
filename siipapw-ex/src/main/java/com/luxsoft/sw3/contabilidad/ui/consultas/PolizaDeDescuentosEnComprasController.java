package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.math.BigDecimal;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.services.AbstractPolizaManager;
import com.luxsoft.sw3.contabilidad.services.PolizaContableManager;
import com.luxsoft.utils.LoggerHelper;



/**
 * Implementacion de {@link PolizaContableManager} para la generación y mantenimiento 
 * de la poliza de descuentos en compras
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDeDescuentosEnComprasController extends AbstractPolizaManager{
	
	Logger logger=LoggerHelper.getLogger();
	
	protected void inicializarPoliza(final Date fecha){
		poliza=new Poliza();
		poliza.setFecha(fecha);
		poliza.setTipo(Poliza.Tipo.DIARIO);
		poliza.setDescripcion(MessageFormat
				.format("Notas procesadas: {0,date,short}",fecha));
		poliza.setClase("COMPRAS DESCUENTOS");
	}
	
	protected void inicializarDatos(){}
	
	@Override
	protected void procesarPoliza() {
		registrarNotasDeDescuento();
		//registrarAnticipo();
		//registrarAplicacionDeAnticipo();
		
	}
	
	private  void registrarNotasDeDescuento(){		
		final String asiento="Notas";
				
		String sql="SELECT 'DESCUENTO' AS TIPO,C.CONCEPTO_NOTA,C.CXP_ID AS ORIGEN_ID,C.FECHA,CONCAT(C.CLAVE,' ',C.NOMBRE) AS PROVEEDOR" +
				",CONCAT('Nota:',C.DOCUMENTO,' ',C.CONCEPTO_NOTA,' ',C.FECHA) AS CONCEPTO,ROUND(C.TOTAL*C.TC,2) AS TOTAL " +
				" FROM SX_CXP C " +
				" WHERE FECHA=? AND TIPO='NOTA' " 
				//+"AND CONCEPTO_NOTA IN('DESCUENTO','DESCUENTO_FINANCIERO')"
				;
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
		};	
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);

		for(Map<String,Object> row:rows){
			BigDecimal total=new BigDecimal( ((Number)row.get("TOTAL")).doubleValue());
			BigDecimal importe=calcularImporteDelTotal(total);
			BigDecimal impuesto=calcularImpuesto(importe);
			//iva=MonedasUtils.calcularImpuestoDelTotal(total);
			importe=redondear(importe);
			impuesto=redondear(impuesto);

			Boolean descto_fin=(Boolean)row.get("CONCEPTO_NOTA").equals("DESCUENTO_FINANCIERO");
			Boolean devolucion=(Boolean)row.get("CONCEPTO_NOTA").equals("DEVLUCION");
			Boolean bonificacion=(Boolean)row.get("CONCEPTO_NOTA").equals("BONIFICACION");
			Boolean descto_anticipo=(Boolean)row.get("CONCEPTO_NOTA").equals("ANTICIPO_DESCUENTO");
				
			//Abono  a Cancelacion de Provision
			PolizaDet abonoACancProvisionEnCompras=poliza.agregarPartida();
			abonoACancProvisionEnCompras.setCuenta(getCuenta("200"));
			abonoACancProvisionEnCompras.setHaber(importe);
			abonoACancProvisionEnCompras.setDescripcion("PROVEEDORES (DESCUENTO PENDIENTE)");
			abonoACancProvisionEnCompras.setDescripcion2((String)row.get("CONCEPTO"));
			abonoACancProvisionEnCompras.setReferencia((String)row.get("PROVEEDOR"));
			abonoACancProvisionEnCompras.setReferencia2("TODAS");
			abonoACancProvisionEnCompras.setAsiento(asiento);
			
			//Abono  a IVA de Cancelacion de Provision
			PolizaDet abonoaIvaCancProvisionCompras=poliza.agregarPartida();
			abonoaIvaCancProvisionCompras.setCuenta(getCuenta("117"));
			abonoaIvaCancProvisionCompras.setDescripcion(IVA_POR_ACREDITAR_COMPRAS);
			abonoaIvaCancProvisionCompras.setDescripcion2("IVA x Acreditar Canc. Prov. ".concat((String)row.get("CONCEPTO")));
			abonoaIvaCancProvisionCompras.setHaber(impuesto);
			abonoaIvaCancProvisionCompras.setReferencia((String)row.get("PROVEEDOR"));
			abonoaIvaCancProvisionCompras.setReferencia2((String) ("TODAS"));
			abonoaIvaCancProvisionCompras.setAsiento(asiento);
			
			//Cargo a Descuentos sobre Compra
			if(descto_fin){
				PolizaDet cargoAProveedorNCDescuento=poliza.agregarPartida();
				cargoAProveedorNCDescuento.setCuenta(getCuenta("701"));
				cargoAProveedorNCDescuento.setDebe(total);
				cargoAProveedorNCDescuento.setDescripcion("PRODUCTOS FINANCIEROS");
				cargoAProveedorNCDescuento.setDescripcion2("".concat((String)row.get("CONCEPTO")));
				cargoAProveedorNCDescuento.setReferencia((String)row.get("PROVEEDOR"));
				cargoAProveedorNCDescuento.setReferencia2("TODAS");
				cargoAProveedorNCDescuento.setAsiento(asiento);
			}else if(descto_anticipo) {
				PolizaDet cargoAProveedorNCDescuento=poliza.agregarPartida();
				cargoAProveedorNCDescuento.setCuenta(getCuenta("119"));
				cargoAProveedorNCDescuento.setDebe(total);
				cargoAProveedorNCDescuento.setDescripcion("ANTICIPO A PROVEEDORES");
				cargoAProveedorNCDescuento.setDescripcion2("".concat((String)row.get("CONCEPTO")));
				cargoAProveedorNCDescuento.setReferencia((String)row.get("PROVEEDOR"));
				cargoAProveedorNCDescuento.setReferencia2("TODAS");
				cargoAProveedorNCDescuento.setAsiento(asiento);
			}else if(devolucion){
				PolizaDet cargoAProveedorNCDescuento=poliza.agregarPartida();
				cargoAProveedorNCDescuento.setCuenta(getCuenta("200"));
				cargoAProveedorNCDescuento.setDebe(total);
				cargoAProveedorNCDescuento.setDescripcion("PROVEEDORES DEVOLUCION");
				cargoAProveedorNCDescuento.setDescripcion2("".concat((String)row.get("CONCEPTO")));
				cargoAProveedorNCDescuento.setReferencia((String)row.get("PROVEEDOR"));
				cargoAProveedorNCDescuento.setReferencia2("TODAS");
				cargoAProveedorNCDescuento.setAsiento(asiento);		
			}else if(bonificacion){
				PolizaDet cargoAProveedorNCDescuento=poliza.agregarPartida();
				cargoAProveedorNCDescuento.setCuenta(getCuenta("200"));
				cargoAProveedorNCDescuento.setDebe(total);
				cargoAProveedorNCDescuento.setDescripcion("PROVEEDORES BONIFICACION");
				cargoAProveedorNCDescuento.setDescripcion2("".concat((String)row.get("CONCEPTO")));
				cargoAProveedorNCDescuento.setReferencia((String)row.get("PROVEEDOR"));
				cargoAProveedorNCDescuento.setReferencia2("TODAS");
				cargoAProveedorNCDescuento.setAsiento(asiento);	
			}else {
				PolizaDet cargoAProveedorNCDescuento=poliza.agregarPartida();
				cargoAProveedorNCDescuento.setCuenta(getCuenta("200"));
				cargoAProveedorNCDescuento.setDebe(total);
				cargoAProveedorNCDescuento.setDescripcion("PROVEEDORES DESCUENTO");
				cargoAProveedorNCDescuento.setDescripcion2("".concat((String)row.get("CONCEPTO")));
				cargoAProveedorNCDescuento.setReferencia((String)row.get("PROVEEDOR"));
				cargoAProveedorNCDescuento.setReferencia2("TODAS");
				cargoAProveedorNCDescuento.setAsiento(asiento);
			}
				
		}
	}
	
	
	/*private  void registrarAnticipo(){		
		final String asiento="Anticipo";
				
		String sql="SELECT 'ANTICIPO' AS TIPO,C.CXP_ID AS ORIGEN,C.FECHA,CONCAT(C.CLAVE,' ',C.NOMBRE) AS PROVEEDOR,X.DESCRIPCION AS BANCO,ROUND(C.TOTAL*C.TC,2) AS TOTAL " +
					",CONCAT('Anticipo: ',CASE WHEN R.FORMADEPAGO=1 THEN 'CH ' ELSE 'T ' END,B.REFERENCIA,' ',B.FECHA,' ',C.COMENTARIO) AS CONCEPTO " +
					" FROM sw_bcargoabono B JOIN SW_CUENTAS X ON(B.CUENTA_ID=X.id) JOIN sw_trequisicion R ON(R.CARGOABONO_ID=B.CARGOABONO_ID) JOIN sx_cxp C ON(C.REQUISICION_ID=R.REQUISICION_ID) " +
					" WHERE R.CONCEPTO_ID=201136 AND B.FECHA=?"
				;
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
		};	
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);

		for(Map<String,Object> row:rows){
			BigDecimal total=new BigDecimal( ((Number)row.get("TOTAL")).doubleValue());
			BigDecimal importe=calcularImporteDelTotal(total);
			BigDecimal impuesto=calcularImpuesto(importe);
			//iva=MonedasUtils.calcularImpuestoDelTotal(total);
			importe=redondear(importe);
			impuesto=redondear(impuesto);

			//Cargo a Anticipo
			PolizaDet cargoAAnticipoProveedores=poliza.agregarPartida();
			cargoAAnticipoProveedores.setCuenta(getCuenta("119"));
			cargoAAnticipoProveedores.setDebe(importe);
			cargoAAnticipoProveedores.setDescripcion("ANTICIPOS A PROVEEDORES");
			cargoAAnticipoProveedores.setDescripcion2((String)row.get("CONCEPTO"));
			cargoAAnticipoProveedores.setReferencia((String)row.get("PROVEEDOR"));
			cargoAAnticipoProveedores.setReferencia2("TODAS");
			cargoAAnticipoProveedores.setAsiento(asiento);
			
			//Cargo a IVA de Anticipo
			PolizaDet cargoAIvaAnticipoProveedores=poliza.agregarPartida();
			cargoAIvaAnticipoProveedores.setCuenta(getCuenta("117"));
			cargoAIvaAnticipoProveedores.setDescripcion(IVA_EN_COMPRAS);
			cargoAIvaAnticipoProveedores.setDescripcion2("IVA Compras".concat((String)row.get("CONCEPTO")));
			cargoAIvaAnticipoProveedores.setDebe(impuesto);
			cargoAIvaAnticipoProveedores.setReferencia((String)row.get("PROVEEDOR"));
			cargoAIvaAnticipoProveedores.setReferencia2((String) ("TODAS"));
			cargoAIvaAnticipoProveedores.setAsiento(asiento);
			
			//Abono a Bancos

				PolizaDet abonoABancosAnticipo=poliza.agregarPartida();
				abonoABancosAnticipo.setCuenta(getCuenta("102"));
				abonoABancosAnticipo.setHaber(total);
				abonoABancosAnticipo.setDescripcion((String)row.get("BANCO"));
				abonoABancosAnticipo.setDescripcion2("".concat((String)row.get("CONCEPTO")));
				abonoABancosAnticipo.setReferencia((String)row.get("PROVEEDOR"));
				abonoABancosAnticipo.setReferencia2("TODAS");
				abonoABancosAnticipo.setAsiento(asiento);
							
		}
	}
	*/

	/*private  void registrarAplicacionDeAnticipo(){		
		final String asiento="Anticipo";
				
		String sql="SELECT 'APL_ANT' AS TIPO,C.CXP_ID AS ORIGEN,A.FECHA,CONCAT(C.CLAVE,' ',C.NOMBRE) AS PROVEEDOR " + 
				",CONCAT('Aplic Ant: Req. ',C.REQUISICION_ID,' ',C.FECHA,' ',C.COMENTARIO) AS CONCEPTO " +
				",SUM(ROUND(A.IMPORTE*C.TC,2)) AS TOTAL  FROM sx_cxp C JOIN sx_cxp_aplicaciones A ON(C.CXP_ID=A.ABONO_ID) " +
				" WHERE C.TIPO='ANTICIPO' AND date(a.FECHA)=? " +
				"GROUP BY C.CXP_ID,A.FECHA,C.CLAVE,C.NOMBRE,C.REQUISICION_ID,C.FECHA,C.COMENTARIO";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
		};	
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);

		for(Map<String,Object> row:rows){
			BigDecimal total=new BigDecimal( ((Number)row.get("TOTAL")).doubleValue());
			BigDecimal importe=calcularImporteDelTotal(total);
			BigDecimal impuesto=calcularImpuesto(importe);
			//iva=MonedasUtils.calcularImpuestoDelTotal(total);
			importe=redondear(importe);
			impuesto=redondear(impuesto);

			//Cargo a Anticipo
			PolizaDet cargoAAnticipoProveedores=poliza.agregarPartida();
			cargoAAnticipoProveedores.setCuenta(getCuenta("119"));
			cargoAAnticipoProveedores.setDebe(importe);
			cargoAAnticipoProveedores.setDescripcion("INVENTARIO");
			cargoAAnticipoProveedores.setDescripcion2((String)row.get("CONCEPTO"));
			cargoAAnticipoProveedores.setReferencia((String)row.get("PROVEEDOR"));
			cargoAAnticipoProveedores.setReferencia2("TODAS");
			cargoAAnticipoProveedores.setAsiento(asiento);
			
			//Cargo a IVA de Anticipo
			PolizaDet cargoAIvaAnticipoProveedores=poliza.agregarPartida();
			cargoAIvaAnticipoProveedores.setCuenta(getCuenta("117"));
			cargoAIvaAnticipoProveedores.setDescripcion(IVA_EN_COMPRAS);
			cargoAIvaAnticipoProveedores.setDescripcion2("IVA Compras".concat((String)row.get("CONCEPTO")));
			cargoAIvaAnticipoProveedores.setDebe(impuesto);
			cargoAIvaAnticipoProveedores.setReferencia((String)row.get("PROVEEDOR"));
			cargoAIvaAnticipoProveedores.setReferencia2((String) ("TODAS"));
			cargoAIvaAnticipoProveedores.setAsiento(asiento);
			
			
			//Abono a Bancos

				PolizaDet abonoABancosAnticipo=poliza.agregarPartida();
				abonoABancosAnticipo.setCuenta(getCuenta("119"));
				abonoABancosAnticipo.setHaber(importe);
				abonoABancosAnticipo.setDescripcion("ANTICIPOS A PROVEEDORES");
				abonoABancosAnticipo.setDescripcion2("".concat((String)row.get("CONCEPTO")));
				abonoABancosAnticipo.setReferencia((String)row.get("PROVEEDOR"));
				abonoABancosAnticipo.setReferencia2("TODAS");
				abonoABancosAnticipo.setAsiento(asiento);
							
		}
	}*/
	
	public static void main(String[] args) {
		PolizaDeDescuentosEnComprasController model=new PolizaDeDescuentosEnComprasController();
		model.generarPoliza(DateUtil.toDate("27/02/2010"));
		
	}

	
}
