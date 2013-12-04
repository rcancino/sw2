package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.math.BigDecimal;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.services.AbstractPolizaManager;
import com.luxsoft.sw3.contabilidad.services.PolizaContableManager;
import com.luxsoft.utils.LoggerHelper;



/**
 * Implementacion de {@link PolizaContableManager} para la generación y mantenimiento de la poliza de Compras - Almacen
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDeAlmacenController extends AbstractPolizaManager{
	
	Logger logger=LoggerHelper.getLogger();
	
	protected void inicializarPoliza(final Date fecha){
		poliza=new Poliza();
		poliza.setFecha(fecha);
		poliza.setClase("COMPRAS ALMACEN");
		poliza.setTipo(Poliza.Tipo.DIARIO);
		poliza.setDescripcion(MessageFormat.format("Entradas de almacén para el: {0,date,short}",fecha));
	}
	
	protected void inicializarDatos(){}
	
	@Override
	protected void procesarPoliza() {
		cargoAlmacen();
		registrarProvision();
		registrarProveedorCompras();
	//	registrarNotasCxP();
	}	
	
	private void cargoAlmacen(){
		
		final String asiento="Compras";
		
		String SQL="SELECT 'COMPRAS' AS TIPO,CONCAT(C.CLAVE,' ',C.NOMBRE) AS PROVEEDOR,S.NOMBRE AS SUCURSAL"+
		//",ROUND(SUM(CASE WHEN MONTH(C.FECHA)<MONTH(IC.FECHA) THEN (A.CANTIDAD/IC.FACTORU*A.COSTO)*C.TC ELSE 0 END),2) AS IMP_ANT"+
		//",ROUND(SUM(CASE WHEN MONTH(C.FECHA)=MONTH(IC.FECHA) THEN (A.CANTIDAD/IC.FACTORU*A.COSTO)*C.TC  ELSE 0 END),2) AS IMP_PRD"+
		//",ROUND(SUM(CASE WHEN MONTH(C.FECHA)>MONTH(IC.FECHA) THEN (A.CANTIDAD/IC.FACTORU*A.COSTO)*C.TC ELSE 0 END),2) AS IMP_PST"+
		",SUM(ROUND((A.CANTIDAD/IC.FACTORU*A.COSTO)*C.TC,2)) AS TOTAL "+
		" FROM sx_inventario_com IC "+
		" JOIN sx_cxp_analisisdet A ON(IC.INVENTARIO_ID=A.ENTRADA_ID)"+
		" JOIN sx_cxp C ON(C.CXP_ID=A.CXP_ID)"+
		" JOIN sw_sucursales S ON(S.SUCURSAL_ID=IC.SUCURSAL_ID)"+
		" WHERE  DATE(IC.FECHA)=? "+
		" GROUP BY C.CLAVE,C.NOMBRE,IC.SUCURSAL_ID"+
		" ORDER BY C.CLAVE";
		Object[] params=new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())};
		List<Map<String, Object>> rows=getJdbcTemplate().queryForList(SQL,params);		
		 
		for(Map<String,Object> row:rows){
			
			//BigDecimal IMP_ANT=new BigDecimal( ((Number)row.get("IMP_ANT")).doubleValue() );
			//BigDecimal IMP_PRD=new BigDecimal( ((Number)row.get("IMP_PRD")).doubleValue() );
			//BigDecimal IMP_PST=new BigDecimal( ((Number)row.get("IMP_PST")).doubleValue() );
			//BigDecimal[] IMPORTES={IMP_ANT,IMP_PRD,IMP_PST};
			BigDecimal TOTAL=new BigDecimal( ((Number)row.get("TOTAL")).doubleValue()) ;
			BigDecimal impuesto=calcularImpuesto(TOTAL);
			
			String sucursal=(String)row.get("SUCURSAL");
			String proveedor=(String)row.get("PROVEEDOR");
			
			PolizaDet cargoAlamcen=poliza.agregarPartida();
			cargoAlamcen.setCuenta(getCuenta("119"));
			cargoAlamcen.setConcepto(cargoAlamcen.getCuenta().getConcepto("INVC1"));
			//cargoAlamcen.setDescripcion(descripcion);
			cargoAlamcen.setDebe(TOTAL);
			//cargoAlamcen.setDescripcion();
			cargoAlamcen.setDescripcion2("Inv Entradas x Compra");
			cargoAlamcen.setReferencia(proveedor);
			cargoAlamcen.setReferencia2(sucursal);
			cargoAlamcen.setAsiento(asiento);
			
			/*
			for(int index=0;index<IMPORTES.length;index++){
				BigDecimal importe=IMPORTES[index];
				String descripcion="NA";
				String descripcion2="NA";
				switch (index) {
				case 0:
					descripcion="INVENTARIO TRANSITO ANTERIOR";
					descripcion2="Inv Entradas x Compra Transito Ant";
					break;
				case 1:
					descripcion="INVENTARIO";
					descripcion2="Inventario Entradas x Compra ";
					break;
				case 2:
					descripcion="INVENTARIO TRANSITO POSTERIOR";
					descripcion2="Inv Entradas x Compra Transito Post";
					break;
				default:
					break;
				}
				if(importe.doubleValue()>0){
					
					//Cargo  a Compras
					
					PolizaDet cargoAlamcen=poliza.agregarPartida();
					cargoAlamcen.setCuenta(getCuenta("119"));
					cargoAlamcen.setConcepto(cargoAlamcen.getCuenta().getConcepto(descripcion));
					cargoAlamcen.setDescripcion(descripcion);
					cargoAlamcen.setDebe(importe);
					//cargoAlamcen.setDescripcion();
					cargoAlamcen.setDescripcion2(descripcion2);
					cargoAlamcen.setReferencia(proveedor);
					cargoAlamcen.setReferencia2(sucursal);
					cargoAlamcen.setAsiento(asiento);
					
				}
			}*/
			
			
			PolizaDet cargoIva=poliza.agregarPartida();
			cargoIva.setCuenta(getCuenta("117"));
			cargoIva.setDebe(impuesto);
			cargoIva.setDescripcion(IVA_POR_ACREDITAR_COMPRAS);
			cargoIva.setDescripcion2("IVA por Acreditar Entradas x Compra");
			cargoIva.setReferencia(proveedor);
			cargoIva.setReferencia2("TODAS");
			cargoIva.setAsiento(asiento);
		}
	}
	
	private  void registrarProvision(){		
		final String asiento="Compras";
				
		String sql="SELECT C.CLAVE AS PROVEEDOR,S.NOMBRE AS SUCURSAL" +
				",C.CXP_ID,CONCAT('Fac:',C.DOCUMENTO,' ',C.FECHA) AS CONCEPTO" +
				",SUM( ROUND((A.CANTIDAD/IC.FACTORU*(A.PRECIO))*C.TC,2)) AS IMPORTE " +
				",SUM( ROUND((A.CANTIDAD/IC.FACTORU*(A.COSTO))*C.TC,2)) AS COSTO " +
				",SUM( ROUND((A.CANTIDAD/IC.FACTORU*(A.PRECIO-A.COSTO))*C.TC,2)) AS PROVISION " +
				//",SUM( ROUND((A.CANTIDAD/IC.FACTORU*(A.PRECIO-A.COSTO))*C.TC)*100,2) / SUM( ROUND((A.CANTIDAD/IC.FACTORU*(A.PRECIO))*C.TC,2)) ) AS PORC " +
				" FROM sx_inventario_com IC JOIN sx_cxp_analisisdet A ON(IC.INVENTARIO_ID=A.ENTRADA_ID) " +
				" JOIN sx_cxp C ON(C.CXP_ID=A.CXP_ID) JOIN sw_sucursales S ON(S.SUCURSAL_ID=IC.SUCURSAL_ID) " +
				" WHERE  DATE(IC.FECHA)=? " +
				" GROUP BY C.CLAVE,IC.SUCURSAL_ID,C.DOCUMENTO,C.FECHA " +
				" HAVING SUM((A.CANTIDAD/IC.FACTORU*((CASE WHEN C.PROVEEDOR_ID IN(140,197,193,55) THEN A.PRECIO-A.COSTO ELSE 0 END)))*C.TC)>0 " +
				" ORDER BY C.CLAVE";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
		};	
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		
		for(Map<String,Object> row:rows){
			
			BigDecimal total=calcularTotal(new BigDecimal( ((Number)row.get("PROVISION")).doubleValue()));
			BigDecimal importe=calcularImporteDelTotal(total);
			BigDecimal impuesto=calcularImpuesto(importe);
			importe=redondear(importe);
			impuesto=redondear(impuesto);
			String proveedorClave=(String)row.get("PROVEEDOR");
			//String concepto=(String)row.get("CONCEPTO");
	
			//Cargo  a Provision
			PolizaDet cargoAProvisionEnCompras=poliza.agregarPartida();
			cargoAProvisionEnCompras.setCuenta(getCuenta("200"));
			cargoAProvisionEnCompras.setDebe(total);
			ConceptoContable concepto=cargoAProvisionEnCompras.getCuenta().getConcepto(proveedorClave);
			if(concepto==null)
				concepto=this.generarConcepto(proveedorClave);
			cargoAProvisionEnCompras.setConcepto(concepto);
			//cargoAProvisionEnCompras.setDescripcion("PROVEEDOR (DESCUENTO PENDIENTE)");
			cargoAProvisionEnCompras.setDescripcion2("Proveedor Descto Pendiente "+concepto);
			cargoAProvisionEnCompras.setReferencia(proveedorClave);
			cargoAProvisionEnCompras.setReferencia2((String)row.get("SUCURSAL"));
			cargoAProvisionEnCompras.setAsiento(asiento);
			
/*			//Cargo  a IVA de Provision
			PolizaDet cargoaIvaProvisionCompras=poliza.agregarPartida();
			cargoaIvaProvisionCompras.setCuenta(getCuenta("117"));
			cargoaIvaProvisionCompras.setDescripcion(IVA_POR_ACREDITAR_COMPRAS);
			cargoaIvaProvisionCompras.setDescripcion2("IVA por Acreditar Provision Inventario");
			cargoaIvaProvisionCompras.setDebe(impuesto);
			cargoaIvaProvisionCompras.setReferencia(proveedor);
			cargoaIvaProvisionCompras.setReferencia2((String) ("TODAS"));
			cargoaIvaProvisionCompras.setAsiento(asiento);		*/
		}		
	}	
	
	private  void registrarProveedorCompras(){
		
		final String asiento="Compras";
				
		String sql="SELECT 'PROVEEDORES' AS TIPO,C.CXP_ID AS ORIGEN_ID,IC.FECHA,S.NOMBRE AS SUCURSAL" +
				",C.CXP_ID,C.CLAVE AS PROVEEDOR,CONCAT('Fac:',C.DOCUMENTO,' ',C.FECHA) AS CONCEPTO" +
				",C.TC,C.MONEDA,ROUND(C.IMPORTE*C.TC,2) AS IMPORTE,ROUND(C.TOTAL*C.TC,2) AS TOTAL" +
				",ROUND(SUM((A.CANTIDAD/IC.FACTORU*((CASE WHEN C.PROVEEDOR_ID IN(140,197,193,55) THEN A.PRECIO ELSE A.COSTO END)))*C.TC),2) AS IMP_PRECIO" +
				",ROUND(SUM(((A.CANTIDAD/IC.FACTORU*((CASE WHEN C.PROVEEDOR_ID IN(140,197,193,55) THEN A.PRECIO ELSE A.COSTO END)))*C.TC)*1.16)+FLETE+FLETE_IVA-FLETE_RET,2) AS TOT_PRECIO" +
				",C.FLETE*C.TC AS FLETE,(FLETE_IVA-FLETE_RET)*C.TC AS FLETE_IVA_R" +
				",C.FLETE_RET*C.TC AS FLETE_RET,ROUND(C.TOTAL*C.TC,2)-ROUND(SUM(((A.CANTIDAD/IC.FACTORU*((CASE WHEN C.PROVEEDOR_ID IN(140,197,193,55) THEN A.PRECIO ELSE A.COSTO END)))*C.TC)*1.16)+FLETE+FLETE_IVA-FLETE_RET,2) AS DIF" +
				",CASE   WHEN MONTH(IC.FECHA)>MONTH(C.FECHA) THEN 'TRANSITO_ANT'  " +
				"        WHEN MONTH(IC.FECHA)<MONTH(C.FECHA) THEN 'TRANSITO_PST' " +
				"        WHEN TRUNCATE(C.TOTAL*C.TC,0)=TRUNCATE(SUM(((A.CANTIDAD/IC.FACTORU*((CASE WHEN C.PROVEEDOR_ID IN(140,197,193,55) THEN A.PRECIO ELSE A.COSTO END)))*C.TC)*1.16)+FLETE+FLETE_IVA-FLETE_RET,0) AND ROUND(C.TOTAL*C.TC,2)<>ROUND(SUM(((A.CANTIDAD/IC.FACTORU*((CASE WHEN C.PROVEEDOR_ID IN(140,197,193,55) THEN A.PRECIO ELSE A.COSTO END)))*C.TC)*1.16)+FLETE+FLETE_IVA-FLETE_RET,2) THEN 'DIFERENCIA' " +
				"        WHEN TRUNCATE(C.TOTAL*C.TC,0)<>TRUNCATE(SUM(((A.CANTIDAD/IC.FACTORU*((CASE WHEN C.PROVEEDOR_ID IN(140,197,193,55) THEN A.PRECIO ELSE A.COSTO END)))*C.TC)*1.16)+FLETE+FLETE_IVA-FLETE_RET,0)     THEN 'TRANSITO' ELSE 'ACTUAL' END AS STATUS " +
				",C.ANTICIPO " +
				" FROM sx_inventario_com IC JOIN sx_cxp_analisisdet A ON(IC.INVENTARIO_ID=A.ENTRADA_ID) " +
				" JOIN sx_cxp C ON(C.CXP_ID=A.CXP_ID) JOIN sw_sucursales S ON(S.SUCURSAL_ID=IC.SUCURSAL_ID) " +
				" WHERE  DATE(IC.FECHA)=? " +
				" GROUP BY  C.CXP_ID,C.CLAVE,C.NOMBRE ,C.DOCUMENTO,C.FECHA,C.TC,C.MONEDA,C.IMPORTE,C.TOTAL,C.FLETE,C.FLETE_IVA,C.FLETE_RET " +
				" ORDER BY C.CLAVE";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
		};	
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);		
		
		for(Map<String,Object> row:rows){
			
			String descripcion2=(String)row.get("CONCEPTO");
			
			BigDecimal total= new BigDecimal( ((Number)row.get("TOTAL")).doubleValue());
			BigDecimal flete= new BigDecimal( ((Number)row.get("FLETE")).doubleValue());
			BigDecimal fleteIvaR= new BigDecimal( ((Number)row.get("FLETE_IVA_R")).doubleValue());
			BigDecimal fleteRet= new BigDecimal( ((Number)row.get("FLETE_RET")).doubleValue());
			BigDecimal dif= new BigDecimal( ((Number)row.get("DIF")).doubleValue());
			Object ant=row.get("ANTICIPO");
			
			Boolean anticipo=(Boolean)ant;
			/*
			String sql2="SELECT COUNT(0) FROM SX_INVENTARIO_COM WHERE INVENTARIO_ID IN(SELECT ENTRADA_ID FROM sx_cxp_analisisdet WHERE CXP_ID=?) AND FECHA<?";
			int existentes=ServiceLocator2.getJdbcTemplate().queryForInt(sql2, new Object[]{row.get("CXP_ID"),poliza.getFecha()});

			if(existentes>0){
				PolizaDet abonoAProveedoresCompras=poliza.agregarPartida();
				abonoAProveedoresCompras.setCuenta(getCuenta("119"));
				Number abono=(Number)row.get("TOT_PRECIO");
				abonoAProveedoresCompras.setHaber(BigDecimal.valueOf(abono.doubleValue()));
				abonoAProveedoresCompras.setDescripcion("INVENTARIO TRANSITO ANTERIOR");
				abonoAProveedoresCompras.setDescripcion2((String)row.get("CONCEPTO"));
				abonoAProveedoresCompras.setReferencia((String)row.get("PROVEEDOR"));
				abonoAProveedoresCompras.setReferencia2("TODAS");
				abonoAProveedoresCompras.setAsiento(asiento);
			}*/
			String proveedorClave=(String)row.get("PROVEEDOR");
			if(anticipo){
				PolizaDet abonoAProveedoresCompras=poliza.agregarPartida();
				abonoAProveedoresCompras.setCuenta(getCuenta("119"));
				abonoAProveedoresCompras.setHaber(total);
				abonoAProveedoresCompras.setDescripcion("ANTICIPO A PROVEEDORES");
				abonoAProveedoresCompras.setDescripcion2((String)row.get("CONCEPTO"));
				abonoAProveedoresCompras.setReferencia((String)row.get("PROVEEDOR"));
				abonoAProveedoresCompras.setReferencia2("TODAS");
				abonoAProveedoresCompras.setAsiento(asiento);
			}else{
				PolizaDet abonoAProveedoresCompras=poliza.agregarPartida();
				abonoAProveedoresCompras.setCuenta(getCuenta("200"));
				abonoAProveedoresCompras.setHaber(total);
				ConceptoContable concepto=abonoAProveedoresCompras.getCuenta().getConcepto(proveedorClave);
				if(concepto==null)
					concepto=this.generarConcepto(proveedorClave);
				abonoAProveedoresCompras.setConcepto(concepto);
				//abonoAProveedoresCompras.setDescripcion("PROVEEDORES");
				abonoAProveedoresCompras.setDescripcion2(descripcion2);
				abonoAProveedoresCompras.setReferencia(proveedorClave);
				abonoAProveedoresCompras.setReferencia2("TODAS");
				abonoAProveedoresCompras.setAsiento(asiento);
				
				if(fleteRet.abs().doubleValue()>0){
				PolizaDet abonoIvaRetenido=poliza.agregarPartida();
				abonoIvaRetenido.setCuenta(getCuenta("117"));
				abonoIvaRetenido.setHaber(fleteRet);
				abonoIvaRetenido.setDescripcion(IVA_RETENIDO_PENDIENTE);
				abonoIvaRetenido.setDescripcion2("IVA Retenido Pendiente ".concat((String)row.get("CONCEPTO")));
				abonoIvaRetenido.setReferencia(proveedorClave);
				abonoIvaRetenido.setReferencia2("TODAS");
				abonoIvaRetenido.setAsiento(asiento);
				}
				
				if(flete.abs().doubleValue()>0){
				PolizaDet cargoAFlete=poliza.agregarPartida();
				cargoAFlete.setCuenta(getCuenta("119"));
				cargoAFlete.setDebe(flete);
				cargoAFlete.setDescripcion("FLETES PROVEEDOR");
				cargoAFlete.setDescripcion2("Flete ".concat((String)row.get("CONCEPTO")));
				cargoAFlete.setReferencia((String)row.get("PROVEEDOR"));
				cargoAFlete.setReferencia2("TODAS");
				cargoAFlete.setAsiento(asiento);
				}
				
				if(fleteIvaR.abs().doubleValue()>0){
				PolizaDet cargoAFleteIvaR=poliza.agregarPartida();
				cargoAFleteIvaR.setCuenta(getCuenta("117"));
				cargoAFleteIvaR.setDebe(fleteIvaR);
				cargoAFleteIvaR.setDescripcion(IVA_POR_ACREDITAR_COMPRAS);
				cargoAFleteIvaR.setDescripcion2("IVA por Acreditar ".concat((String)row.get("CONCEPTO")));
				cargoAFleteIvaR.setReferencia((String)row.get("PROVEEDOR"));
				cargoAFleteIvaR.setReferencia2("TODAS");
				cargoAFleteIvaR.setAsiento(asiento);
				}
				
				if(fleteRet.abs().doubleValue()>0){
				PolizaDet cargoAFleteIvaRet=poliza.agregarPartida();
				cargoAFleteIvaRet.setCuenta(getCuenta("117"));
				cargoAFleteIvaRet.setDebe(fleteRet);
				cargoAFleteIvaRet.setDescripcion(IVA_POR_ACREDITAR_RETENIDO);
				cargoAFleteIvaRet.setDescripcion2("IVA por Acreditar Ret. ".concat((String)row.get("CONCEPTO")));
				cargoAFleteIvaRet.setReferencia((String)row.get("PROVEEDOR"));
				cargoAFleteIvaRet.setReferencia2("TODAS");
				cargoAFleteIvaRet.setAsiento(asiento);
				}
				if(dif.abs().doubleValue()>0){
					
					//Si la diferencia esta entre >=-5<0 Se afecta OTROS PRODUCTOS
					if(dif.doubleValue()>=-5d && dif.doubleValue()<0){
						PolizaDet cargoACompraEnTransito=poliza.agregarPartida();
						cargoACompraEnTransito.setCuenta(getCuenta("702"));
						cargoACompraEnTransito.setHaber(dif.abs());
						cargoACompraEnTransito.setDescripcion("OTROS INGRESOS COMPRAS");
						cargoACompraEnTransito.setDescripcion2("Otros ingresos por compra");
						cargoACompraEnTransito.setReferencia((String)row.get("PROVEEDOR"));
						cargoACompraEnTransito.setReferencia2("TODAS");
						cargoACompraEnTransito.setAsiento(asiento);
					}else if(dif.doubleValue()>0d && dif.doubleValue()<=5){
						PolizaDet cargoACompraEnTransito=poliza.agregarPartida();
						cargoACompraEnTransito.setCuenta(getCuenta("704"));
						cargoACompraEnTransito.setDebe(dif.abs());
						cargoACompraEnTransito.setDescripcion("OTROS GASTOS COMPRAS");
						cargoACompraEnTransito.setDescripcion2("Otros gastos por compra");
						cargoACompraEnTransito.setReferencia((String)row.get("PROVEEDOR"));
						cargoACompraEnTransito.setReferencia2("TODAS");
						cargoACompraEnTransito.setAsiento(asiento);
					}else{
						PolizaDet cargoACompraEnTransito=poliza.agregarPartida();
						cargoACompraEnTransito.setCuenta(getCuenta("200"));
						cargoACompraEnTransito.setDebe(dif);
						if(concepto==null)
							concepto=this.generarConcepto(proveedorClave);
						cargoACompraEnTransito.setConcepto(concepto);
						//cargoACompraEnTransito.setDescripcion("PROVEEDOR (DESCUENTO PENDIENTE)");
						cargoACompraEnTransito.setDescripcion2("Descuento Pendiente ".concat((String)row.get("CONCEPTO")));
						cargoACompraEnTransito.setReferencia(proveedorClave);
						cargoACompraEnTransito.setReferencia2("TODAS");
						cargoACompraEnTransito.setAsiento(asiento);
					}
				}
			}		
		}		
	}
	
	private ConceptoContable generarConcepto(String proveedorClave){
		System.out.println("Generando concepto para proveedor: "+proveedorClave);
		Proveedor proveedor=ServiceLocator2.getProveedorManager().buscarPorClave(proveedorClave);
		CuentaContable cuenta=getCuenta("200");
		ConceptoContable concepto=new ConceptoContable();
		concepto.setCuenta(cuenta);
		cuenta.getConceptos().add(concepto);
		concepto.setClave(proveedorClave);
		concepto.setDescripcion(proveedor.getNombreRazon());
		concepto=(ConceptoContable)ServiceLocator2.getUniversalDao().save(concepto);
		return concepto;
	}
	
/*	*/
	
	

	public static void main(String[] args) {
		PolizaDeAlmacenController model=new PolizaDeAlmacenController();
		model.generarPoliza(DateUtil.toDate("27/02/2010"));
		
	}

	
}
