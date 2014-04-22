package com.luxsoft.sw3.contabilidad.polizas.compras;

import java.math.BigDecimal;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.aspectj.apache.bcel.verifier.TransitiveHull;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.Poliza.Tipo;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.utils.LoggerHelper;

/**
 * Procesador especial para la generación de la poliza de Compras - Almacen
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class Proc_CxPCompras {
	
	Logger logger=LoggerHelper.getLogger();
	
	private EventList<PagoCXP> pagos;
	private GroupingList<PagoCXP> pagosPorGrupo;
	
	
	protected List<Poliza> generaPoliza(final Periodo periodo) {
		List<Poliza> res=new ArrayList<Poliza>();
		List<Date> fechas=periodo.getListaDeDias();
		for(Date fecha:fechas){
			res.addAll(generaPoliza(fecha));
		}
		return res;
	}
	
	protected List<Poliza> generaPoliza(final Date fecha) {
		final List<Poliza> polizas=new ArrayList<Poliza>(0);
		cargarDatos(fecha);
		for(List<PagoCXP> grupo:pagosPorGrupo){
			Poliza poliza=new Poliza();
			poliza.setFecha(fecha);
			poliza.setTipo(Poliza.Tipo.EGRESO);
			poliza.setClase("PAGOS");
			poliza.setReferencia(String.valueOf(grupo.get(0).getCARGOABONO_ID()) );
			for(Iterator<PagoCXP> it=grupo.iterator();it.hasNext();){
				PagoCXP pago=it.next();
				if(!it.hasNext()){					
					//poliza.setClase("CXP COMPRAS");			
										
					String monedaPago=pago.getMONEDA();
					if(pago.getTC_PAGO().doubleValue()>1.00){
						monedaPago="USD";
					}
										
					String descripcion="COMPRAS: {0} {1} {2} {3}";					
					poliza.setDescripcion(MessageFormat.format(descripcion
							,pago.getBANCO()+", "
							,pago.getFORMAPAGO()+" "+pago.getREFERENCIA()
							,monedaPago+",  "	
							,pago.getNOMBRE()
							)
							);
				}
			}
			//PagoCXP pag=grupo.get(0);
			//poliza.setDescripcion(pag.getNOMBRE()+"  ("+pag.getMONEDA()+")");
			try {
				generarPartidas(poliza, grupo);
			} catch (Exception e) {
				e.printStackTrace();
				poliza.setDescripcion(poliza.getDescripcion()+ " Error: "+ExceptionUtils.getRootCauseMessage(e));
			}
			
			poliza.actualizar();
			polizas.add(poliza);
		}
		List<PagoCXP> cancelados=cargarCancelados(fecha);
		for(PagoCXP pago:cancelados){
			
			String monedaPago=pago.getMONEDA();
//			if(pago.getTC_PAGO().doubleValue()>1.00){
//				monedaPago="USD";
//			}
			
			Poliza poliza=new Poliza();
			poliza.setFecha(fecha);
			poliza.setTipo(Poliza.Tipo.EGRESO);
			poliza.setClase("PAGOS");
			poliza.setReferencia(String.valueOf(pago.getCARGOABONO_ID()) );
			String descripcion="COMPRAS: {0} {1} {2} {3}";					
			poliza.setDescripcion(MessageFormat.format(descripcion
					,pago.getBANCO()+", "
					,pago.getFORMAPAGO()+" "+pago.getREFERENCIA()+" "
					,monedaPago+",  "					
					,pago.getNOMBRE()
					)
					);
			poliza.actualizar();
			polizas.add(poliza);
		}
		return polizas;
	}
	
	/**
	 * Carga todos los registros
	 * 
	 * @param fecha
	 */
	private void cargarDatos(final Date fecha){
		String sql="SELECT b.CARGOABONO_ID,B.fecha,B.REFERENCIA,B.CUENTA_ID,(SELECT X.DESCRIPCION FROM SW_CUENTAS X WHERE X.ID=B.CUENTA_ID) AS BANCO"
			+" ,(SELECT X.NUMERO FROM SW_CUENTAS X WHERE X.ID=B.CUENTA_ID) AS CONCEPTO"
			+" ,CASE WHEN T.FORMADEPAGO=0 THEN 'T.' ELSE 'CH' END AS FORMAPAGO,C.CLAVE AS CLAVEPROV,C.NOMBRE,-B.IMPORTE as IMPORTE,B.TC AS TC_PAGO,T.TC AS TC_REQ"
			+" ,C.CXP_ID,C.DOCUMENTO,C.FECHA as FECHA_DOCTO,C.MONEDA,C.TC,c.TOTAL,C.IMPORTE,C.IMPUESTO,C.FLETE,C.FLETE_IVA,C.FLETE_RET"
			+" ,ROUND(C.TOTAL*C.TC,2) AS TOT_MN,D.TOTAL*t.TC AS APAGAR" 
			+" ,B.FECHA_COBRO AS fechaCobro" 
			+" ,T.CONCEPTO_ID as conceptoRequisicion "
			+" ,IFNULL((SELECT SUM(N.IMPORTE) FROM sx_cxp_aplicaciones N WHERE N.TIPO_ABONO NOT IN('PAGO','ANTICIPO') AND DATE(N.FECHA)<=B.FECHA AND N.CARGO_ID=C.CXP_ID),0)*C.TC AS BONIFICACIONES"
			+" ,IFNULL((SELECT SUM(N.IMPORTE) FROM sx_cxp_aplicaciones N JOIN SX_CXP X ON(X.CXP_ID=N.ABONO_ID) WHERE X.CONCEPTO_NOTA ='DESCUENTO' AND DATE(N.FECHA)<=B.FECHA AND N.CARGO_ID=C.CXP_ID),0)*C.TC AS DESCTO"
			+" FROM SX_CXP C" 
			+" JOIN sw_trequisiciondet D ON(C.CXP_ID=D.CXP_ID)"
			+" JOIN sw_trequisicion T ON(T.REQUISICION_ID=D.REQUISICION_ID)"
			+" JOIN sw_bcargoabono B ON(T.CARGOABONO_ID=B.CARGOABONO_ID)"
			//+" WHERE  T.CONCEPTO_ID=201136 AND B.fecha =?";
			+" WHERE  B.fecha =?";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,fecha)
		};	
		System.out.println(sql);
		pagos=GlazedLists.eventList(ServiceLocator2.getJdbcTemplate().query(sql, params, new BeanPropertyRowMapper(PagoCXP.class)));
		Comparator c=GlazedLists.beanPropertyComparator(PagoCXP.class, "CARGOABONO_ID");
		pagosPorGrupo=new GroupingList<PagoCXP>(pagos,c);
	}
	
	/**
	 * Carga todos los registros
	 * 
	 * @param fecha
	 */
	private List<PagoCXP> cargarCancelados(final Date fecha){
		String sql="SELECT b.CARGOABONO_ID,B.fecha,B.REFERENCIA,B.CUENTA_ID,(SELECT X.DESCRIPCION FROM SW_CUENTAS X WHERE X.ID=B.CUENTA_ID) AS BANCO "
			 +" ,(SELECT X.NUMERO FROM SW_CUENTAS X WHERE X.ID=B.CUENTA_ID) AS CONCEPTO,'CH' AS FORMAPAGO,' ' AS MONEDA,B.AFAVOR AS NOMBRE,-B.IMPORTE as IMPORTE "
			 +" FROM sw_bcargoabono B LEFT JOIN sw_trequisicion T ON(T.CARGOABONO_ID=B.CARGOABONO_ID) "
			 +" WHERE  B.fecha=? AND T.REQUISICION_ID IS NULL AND B.IMPORTE=0 AND B.ORIGEN='COMPRAS'";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,fecha)
		};	
		System.out.println(sql);
		return ServiceLocator2.getJdbcTemplate().query(sql, params, new BeanPropertyRowMapper(PagoCXP.class));
		
	}
	
	private void generarPartidas(final Poliza poliza,List<PagoCXP> pagos){
		
			BigDecimal flete= new BigDecimal(0);	
			BigDecimal fleteIva= new BigDecimal(0);
			BigDecimal fleteRet= new BigDecimal(0);
		
			
		String asiento="PAGO";

		//Abono a bancos UNA VEZ
		for (PagoCXP p:pagos){
			flete=flete.add(p.getFLETE());
			fleteIva=fleteIva.add(p.getFLETE_IVA());
			fleteRet=fleteRet.add(p.getFLETE_RET());
		}
			
		PagoCXP pag=pagos.get(0);
		
		boolean transito=false;
		if(pag.getFechaCobro()==null){
			 
		}else if(!DateUtil.isSameMonth(pag.getFecha(),pag.getFechaCobro())){
			 transito = true;
			 asiento="TRANSITO";
		}		
		
		
		if(pag.getFORMAPAGO().startsWith("T"))
			poliza.setTipo(Tipo.DIARIO);
		String conceptoClave=pag.getCONCEPTO();
				
		BigDecimal impPago=pag.getIMPORTE().multiply(pag.getTC_PAGO());		
		
		PolizaDet abonoBancos=poliza.agregarPartida();
		abonoBancos.setCuenta(getCuenta("102"));
		abonoBancos.setHaber(impPago);
		abonoBancos.setAsiento(asiento);
		//abonoBancos.setDescripcion(pag.getCONCEPTO());
		abonoBancos.setConcepto(abonoBancos.getCuenta().getConcepto(conceptoClave));
		abonoBancos.setDescripcion2(MessageFormat.format("{0} {1}"+" Cta.Destino "+"{2} ({3})"
				,pag.getFORMAPAGO()
				,pag.getREFERENCIA()
				,pag.getCONCEPTO()
				,pag.getMONEDA())
				);
		abonoBancos.setReferencia(pag.getNOMBRE());
		abonoBancos.setReferencia2("");	
		
		if(transito==false){
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC01", true
					, round(MonedasUtils.calcularImpuesto(
							MonedasUtils.calcularImporteDelTotal(impPago.subtract(flete.add(fleteIva).subtract(fleteRet)))
							.add(flete)).subtract(fleteRet))
					, MessageFormat.format("{0} {1}"
							,pag.getFORMAPAGO()
							,pag.getREFERENCIA())
					, pag.getNOMBRE(), "", asiento);
		}

		
		
		for(Iterator<PagoCXP> it=pagos.iterator();it.hasNext();){
			PagoCXP pago=it.next();
			//Cargo a proveedor
			BigDecimal importe=pago.getAPAGAR();
			if(pag.getMONEDA().equals("MXN") ){
				importe=pago.getAPAGAR().divide(pag.getTC_PAGO());
			}
				
			
			String desc2=MessageFormat.format("Fac: {0} {1,date,short} ({2}) ", pago.getDOCUMENTO(),pago.getFECHA_DOCTO(),pago.getMONEDA());
			if(pago.getMONEDA().equals("USD")){
			//	importe=pago.getIMPORTE();
				System.out.println("importe sin Bonificacion "+importe +" fac: "+pago.getDOCUMENTO() );
				Date fechaDeTc=pago.getFECHA_DOCTO();
				//BigDecimal tc=BigDecimal.valueOf(getTipoDeCambioDelMes(fechaDeTc));
				BigDecimal tc=BigDecimal.valueOf(pago.getTC());
				if(!DateUtil.isSameMonth(pago.getFecha(), pago.getFECHA_DOCTO())){
					fechaDeTc=pago.getFecha();
					fechaDeTc=DateUtils.addMonths(fechaDeTc, -1);
					tc=BigDecimal.valueOf(getTipoDeCambioDelMes(fechaDeTc));
					if(pago.getConceptoRequisicion().equals(737332L)){
						tc= BigDecimal.valueOf(pago.getTC());
					}
					BigDecimal totalFactura=pago.getTOTAL();
			//		importe=round(totalFactura.subtract(pago.getBONIFICACIONES()));
					//importe=round(totalFactura.subtract(pago.getBONIFICACIONES()).multiply(tc));
					importe=round(totalFactura.multiply(tc).subtract(pago.getBONIFICACIONES()));
					desc2+=MessageFormat.format("T.C.: {0, number, ##.####} ({1,date,short})", tc,getPenultimoDiaDelMes(fechaDeTc));
				}else{
					tc=BigDecimal.valueOf(pago.getTC());
			//		importe=new BigDecimal(pago.getTOT_MN()).subtract(pago.getBONIFICACIONES().multiply(pago.getTC()) );		
					importe=new BigDecimal(pago.getTOT_MN()).subtract(pago.getBONIFICACIONES());		
					System.out.println("totfac importe sin Bonificacion "+importe +" fac: "+pago.getDOCUMENTO() );
				}
				
				
			}			
			
			String cta="200";
			
			if(pagos.get(0).getMONEDA().equals("USD")){
				cta="201";
			}				
			
			BigDecimal variacionTransito=BigDecimal.ZERO;
			
			int mesFactura=Periodo.obtenerMes(pago.getFECHA_DOCTO())+1;
			int mesPago=Periodo.obtenerMes(pago.getFecha())+1;
			int diff=Math.abs(mesPago-mesFactura);
			if(diff>1 && diff<11){
				if(pago.getDESCTO().doubleValue()>0){
					BigDecimal importeDesctoFac=MonedasUtils.calcularImporteDelTotal(pago.getDESCTO());
					BigDecimal importeDesctoProvision=new BigDecimal(importeDesctoFac.doubleValue()/pago.getTC());
					importeDesctoProvision=importeDesctoProvision.multiply(pago.getTC_REQ());
					variacionTransito=importeDesctoFac.subtract(importeDesctoProvision);
//					variacionTransito=MonedasUtils.calcularImpuesto(variacionTransito);
				}
			}			
				PolizaDetFactory.generarPolizaDet(poliza, cta, pago.getCLAVEPROV(), true, importe.add(variacionTransito), desc2,pago.getNOMBRE(), "", asiento);
			
				if(transito==false){					
					
					if(pag.getMONEDA().equals("MXN") ){
						importe=importe;
					}else{
						importe=pago.getAPAGAR();
					}
					
					//Abono a IVA 
					PolizaDet abonoIvaPorAcreditar=poliza.agregarPartida();
					abonoIvaPorAcreditar.setCuenta(getCuenta("117"));
					
					abonoIvaPorAcreditar.setHaber(round(MonedasUtils.calcularImpuesto(							
							MonedasUtils.calcularImporteDelTotal(importe.subtract(pago.getFLETE().add(pago.getFLETE_IVA()).subtract(pago.getFLETE_RET())))							
	//						MonedasUtils.calcularImporteDelTotal(pago.getAPAGAR().subtract(pago.getFLETE().add(pago.getFLETE_IVA()).subtract(pago.getFLETE_RET())))
							
							.add(pago.getFLETE())).subtract(pago.getFLETE_RET())));
					
					abonoIvaPorAcreditar.setAsiento(asiento);
					abonoIvaPorAcreditar.setConcepto(abonoIvaPorAcreditar.getCuenta().getConcepto("IVAC02"));
					abonoIvaPorAcreditar.setDescripcion2(desc2);
					abonoIvaPorAcreditar.setReferencia(pago.getNOMBRE());
					abonoIvaPorAcreditar.setReferencia2("");		
				}							
		}
		
		BigDecimal mpIETU=BigDecimal.ZERO;
		BigDecimal fleteIETU=BigDecimal.ZERO;
		for(PagoCXP pago1:pagos){
			
			BigDecimal importe=pago1.getAPAGAR();
			if(pag.getMONEDA().equals("MXN")){
				importe=pago1.getAPAGAR().divide(pag.getTC_PAGO());
			}
			
			mpIETU=mpIETU.add(round(
					MonedasUtils.calcularImporteDelTotal(importe.subtract(pago1.getFLETE().add(pago1.getFLETE_IVA()).subtract(pago1.getFLETE_RET())))
					));
			fleteIETU=fleteIETU.add(pago1.getFLETE());
			System.out.println("IETU MP " +mpIETU);
			System.out.println("IETU flete "+fleteIETU);
			
		}
		if(transito==false){
			PolizaDet cargoIETU=poliza.agregarPartida();
			cargoIETU.setCuenta(getCuenta("900"));
			cargoIETU.setDebe(mpIETU.add(fleteIETU));
			cargoIETU.setAsiento(asiento);
			cargoIETU.setConcepto(cargoIETU.getCuenta().getConcepto("IETUD01"));
			cargoIETU.setDescripcion2(MessageFormat.format("{0} {1}"
					,pag.getFORMAPAGO()
					,pag.getREFERENCIA())
					);
			cargoIETU.setReferencia(pag.getNOMBRE());
			cargoIETU.setReferencia2("");
			
			PolizaDet abonoIETU=poliza.agregarPartida();
			abonoIETU.setCuenta(getCuenta("901"));		
			abonoIETU.setHaber(mpIETU.add(fleteIETU));
			abonoIETU.setAsiento(asiento);
			abonoIETU.setConcepto(abonoIETU.getCuenta().getConcepto("DIETU01"));
			abonoIETU.setDescripcion2(MessageFormat.format("{0} {1}"
					,pag.getFORMAPAGO()
					,pag.getREFERENCIA())
					);
			abonoIETU.setReferencia(pag.getNOMBRE());
			abonoIETU.setReferencia2("");
		}
		generarDiferenciaCambiaria(poliza, pagos);
		generarRetenciones(poliza, pagos);
	}
	
	private void generarDiferenciaCambiaria(final Poliza poliza, List<PagoCXP> pagos){
		String asiento="PAGO";
		Boolean transito=false;
		if (pagos.get(0).getFechaCobro()==null){
			
		}else if(!DateUtil.isSameMonth(pagos.get(0).getFecha(),pagos.get(0).getFechaCobro())){
			transito = true;
			asiento="TRANSITO";
		}
			
		if(!pagos.get(0).getMONEDA().equals("USD"))
			return;
		
		BigDecimal diferencia=BigDecimal.ZERO;
		BigDecimal importePagado=pagos.get(0).getIMPORTE();
		BigDecimal importeFacturado=BigDecimal.ZERO;
		BigDecimal importeFacturadoActualizado=BigDecimal.ZERO;
		BigDecimal diferenciaIva=BigDecimal.ZERO;
		for(PagoCXP pago:pagos){
			BigDecimal importe=BigDecimal.valueOf(pago.getTOT_MN());	
			Date fechaTc=DateUtils.addMonths(pago.getFecha(), -1);
			if(!DateUtil.isSameMonth(pago.getFecha(), pago.getFECHA_DOCTO())){
				fechaTc=DateUtils.addMonths(pago.getFecha(), -1);
				BigDecimal tc=BigDecimal.valueOf(getTipoDeCambioDelMes(pago.getFECHA_DOCTO()));
				BigDecimal totalFactura=pago.getTOTAL();
				
				importe=totalFactura.multiply(tc);
				
				BigDecimal totFac=pago.getTOTAL().multiply(BigDecimal.valueOf(getTipoDeCambioDelMes(fechaTc)));
				totFac=totFac.subtract(pago.getBONIFICACIONES());							
				
				importeFacturadoActualizado=importeFacturadoActualizado.add(totFac);
				
			}else{
			//	importeFacturadoActualizado=importeFacturadoActualizado.add(BigDecimal.valueOf(pago.getTOT_MN()) );
			//	importeFacturadoActualizado=BigDecimal.valueOf(pago.getTOT_MN().pago.getBONIFICACIONES());
				importeFacturadoActualizado=new BigDecimal(pago.getTOT_MN()).subtract(pago.getBONIFICACIONES());	
				
			}
			importeFacturado=importeFacturado.add(importe);
			
			//Definicion de tipo de cambio
			BigDecimal tc=BigDecimal.valueOf(pago.getTC());
			if(!DateUtil.isSameMonth(pago.getFecha(), pago.getFECHA_DOCTO())){
				fechaTc=DateUtils.addMonths(pago.getFecha(), -1);
				tc=BigDecimal.valueOf(getTipoDeCambioDelMes(fechaTc));
			}
			
			if(pago.getConceptoRequisicion().equals(737332L)){
				tc= BigDecimal.valueOf(pago.getTC());
			}
			
			BigDecimal totalPago=pago.getTOTAL().multiply(tc).subtract(pago.getBONIFICACIONES());
			
			
			diferencia=totalPago.subtract(pago.getAPAGAR()) ;
			
			BigDecimal ivaDiferencia=MonedasUtils.calcularImpuestoDelTotal( BigDecimal.valueOf(pago.getTOT_MN()).subtract(pago.getBONIFICACIONES()).subtract(pago.getAPAGAR()) );
			diferenciaIva=diferenciaIva.add(ivaDiferencia);
			
			if(transito==false){
				//IVA por acreditar en compras 
				PolizaDet abonoIvaPorAcreditar=poliza.agregarPartida();
				abonoIvaPorAcreditar.setCuenta(getCuenta("117"));
				abonoIvaPorAcreditar.setConcepto(abonoIvaPorAcreditar.getCuenta().getConcepto("IVAC02"));
			//	if(diferenciaIva.doubleValue()>0)
				if(ivaDiferencia.doubleValue()>0)		
					abonoIvaPorAcreditar.setHaber(round(ivaDiferencia));
				else
					abonoIvaPorAcreditar.setDebe(round(ivaDiferencia).abs());
				abonoIvaPorAcreditar.setAsiento(asiento);
				//abonoIvaPorAcreditar.setDescripcion(PolizaContableManager.IVA_POR_ACREDITAR_COMPRAS);
				String descripcion2=MessageFormat.format("Fac: {0} {1,date,short} ({2} T.C:{3,number,##.####})"
						, pago.getDOCUMENTO(),pago.getFECHA_DOCTO(),pago.getMONEDA(),pago.getTC());
				abonoIvaPorAcreditar.setDescripcion2(descripcion2);
				abonoIvaPorAcreditar.setReferencia(pago.getNOMBRE());
				abonoIvaPorAcreditar.setReferencia2("");
				
				//Variacion Cambiaria de IVA por Acred en compras 
				PolizaDet variacionCambiariaIvaPorAcreditar=poliza.agregarPartida();
			//	if(diferenciaIva.doubleValue()<0){
				if(ivaDiferencia.doubleValue()<0){	
					
					variacionCambiariaIvaPorAcreditar.setCuenta(getCuenta("701"));
					variacionCambiariaIvaPorAcreditar.setConcepto(variacionCambiariaIvaPorAcreditar.getCuenta().getConcepto("PRFN04"));
					variacionCambiariaIvaPorAcreditar.setHaber(round(ivaDiferencia).abs());
				}else{
					variacionCambiariaIvaPorAcreditar.setCuenta(getCuenta("705"));
					variacionCambiariaIvaPorAcreditar.setConcepto(variacionCambiariaIvaPorAcreditar.getCuenta().getConcepto("GSTF01"));
					variacionCambiariaIvaPorAcreditar.setDebe(round(ivaDiferencia));
				}	
				variacionCambiariaIvaPorAcreditar.setAsiento(asiento);
				//abonoIvaPorAcreditar.setDescripcion(PolizaContableManager.IVA_POR_ACREDITAR_COMPRAS);
				descripcion2=MessageFormat.format("VARIACION IVA Fac: {0} {1,date,short} ({2} )"
						, pago.getDOCUMENTO(),pago.getFECHA_DOCTO(),pago.getMONEDA());
				variacionCambiariaIvaPorAcreditar.setDescripcion2(descripcion2);
				variacionCambiariaIvaPorAcreditar.setReferencia(pago.getNOMBRE());
				variacionCambiariaIvaPorAcreditar.setReferencia2("");	
			}
			
			
			BigDecimal variacionTransito=BigDecimal.ZERO;
			
			
			int mesFactura=Periodo.obtenerMes(pago.getFECHA_DOCTO())+1;
			int mesPago=Periodo.obtenerMes(pago.getFecha())+1;
			int diff=Math.abs(mesPago-mesFactura);
			if(diff>1 && diff<11){
				if(pago.getDESCTO().doubleValue()>0){
					BigDecimal importeDesctoFac=MonedasUtils.calcularImporteDelTotal(pago.getDESCTO());
					BigDecimal importeDesctoProvision=new BigDecimal(importeDesctoFac.doubleValue()/pago.getTC());
					importeDesctoProvision=importeDesctoProvision.multiply(pago.getTC_REQ());
					variacionTransito=importeDesctoFac.subtract(importeDesctoProvision);
			//		variacionTransito=MonedasUtils.calcularImpuesto(variacionTransito);
					
					System.err.println("*******/////////+++++++++++variacionTransito "+variacionTransito+" ++++++++++++++///////////////**********************");
				}
			}
			
			
			//Variacion Cambiaria de Pago
			PolizaDet variacionCambiariaPago=poliza.agregarPartida();
			
			if(diferencia.doubleValue()>0){
				variacionCambiariaPago.setCuenta(getCuenta("701"));
				variacionCambiariaPago.setConcepto(variacionCambiariaPago.getCuenta().getConcepto("PRFN04"));
				variacionCambiariaPago.setHaber(round(diferencia.add(variacionTransito)));				
			}else{
				variacionCambiariaPago.setCuenta(getCuenta("705"));
				variacionCambiariaPago.setConcepto(variacionCambiariaPago.getCuenta().getConcepto("GSTF01"));
				variacionCambiariaPago.setDebe(round(diferencia.add(variacionTransito)).abs());
			}	
			variacionCambiariaPago.setAsiento(asiento);
			
			String descripcion2=MessageFormat.format("VARIACION PAGO Fac: {0} {1,date,short} ({2} T.C act:{3,number,##.####})"
					, pago.getDOCUMENTO(),pago.getFECHA_DOCTO(),pago.getMONEDA(),tc);
			variacionCambiariaPago.setDescripcion2(descripcion2);
			variacionCambiariaPago.setReferencia(pago.getNOMBRE());
			variacionCambiariaPago.setReferencia2("");			
			
		}
		
		//diferencia=importePagado.subtract(importeFacturado);
//		diferencia=importePagado.subtract(importeFacturadoActualizado);
		// Aplicamos la diferencia cambiara acumulada
	/*	
		PolizaDet difCambiaria=poliza.agregarPartida();
		if(diferencia.doubleValue()>0){
			difCambiaria.setCuenta(getCuenta("705"));
			difCambiaria.setConcepto(difCambiaria.getCuenta().getConcepto("GSTF01"));
			difCambiaria.setDebe(round(diferencia.abs()));
		}
		else if(diferencia.doubleValue()<0){
			difCambiaria.setCuenta(getCuenta("701"));
			difCambiaria.setConcepto(difCambiaria.getCuenta().getConcepto("PRFN04"));
			difCambiaria.setHaber(round(diferencia.abs()));
		}
		difCambiaria.setAsiento(asiento);
		//difCambiaria.setDescripcion("VARIACION CAMBIARIA");
		difCambiaria.setDescripcion2(MessageFormat.format("VARIACION PAGO "+"{0} {1}"
				,pagos.get(0).getFORMAPAGO()
				,pagos.get(0).getREFERENCIA())
				);
		difCambiaria.setReferencia(pagos.get(0).getNOMBRE());
		difCambiaria.setReferencia2("");
		*/
			
	/*
		PolizaDet ivaDiferenciaCambiaria=poliza.agregarPartida();
		if(diferenciaIva.doubleValue()>0){
			ivaDiferenciaCambiaria.setCuenta(getCuenta("705"));
			ivaDiferenciaCambiaria.setConcepto(ivaDiferenciaCambiaria.getCuenta().getConcepto("GSTF01"));
			ivaDiferenciaCambiaria.setDebe(round(diferenciaIva.abs()));
		}
		else if(diferenciaIva.doubleValue()<0){
			ivaDiferenciaCambiaria.setCuenta(getCuenta("701"));
			ivaDiferenciaCambiaria.setConcepto(ivaDiferenciaCambiaria.getCuenta().getConcepto("PRFN04"));
			ivaDiferenciaCambiaria.setHaber(round(diferenciaIva.abs()));
		}
		ivaDiferenciaCambiaria.setAsiento(asiento);
		//ivaDiferenciaCambiaria.setDescripcion("VARIACION CAMBIARIA IVA");
		ivaDiferenciaCambiaria.setDescripcion2(MessageFormat.format("VARIACION IVA "+"{0} {1}"
				,pagos.get(0).getFORMAPAGO()
				,pagos.get(0).getREFERENCIA())
				);
		ivaDiferenciaCambiaria.setReferencia(pagos.get(0).getNOMBRE());
		ivaDiferenciaCambiaria.setReferencia2("");
		*/
	}
	
	
	
	private void generarRetenciones(final Poliza poliza,List<PagoCXP> pagos){
		BigDecimal retencion=BigDecimal.ZERO;
		for(PagoCXP pago:pagos){
			retencion=retencion.add(pago.getFLETE_RET());
		}
		
		String asiento="PAGO";
		Boolean transito=false;
	if(pagos.get(0).getFechaCobro()==null){
		 
	}else if(!DateUtil.isSameMonth(pagos.get(0).getFecha(),pagos.get(0).getFechaCobro())){
		 transito = true;
	}
	
	if(transito==false){ 
		if(retencion.doubleValue()>0){
			PagoCXP pago=pagos.get(0);
			String desc2=MessageFormat.format("{0} {1}"
					,pago.getFORMAPAGO()
					,pago.getREFERENCIA());
			
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAR01", true, retencion, desc2, pago.getNOMBRE(), "", asiento);
			
			if(pago.getConceptoRequisicion()==259471L){
				PolizaDetFactory.generarPolizaDet(poliza, "205", "IMPR02", false, retencion, desc2, pago.getNOMBRE(), "", asiento);
			}
			
			
		}
		
		for(Iterator<PagoCXP> it=pagos.iterator();it.hasNext();){
			PagoCXP pago=it.next();
			
			BigDecimal imp=pago.getFLETE_RET();
			
			PolizaDet abonoIvaPorAcreditarRet=poliza.agregarPartida();
			abonoIvaPorAcreditarRet.setCuenta(getCuenta("117"));
			abonoIvaPorAcreditarRet.setHaber(imp);
			abonoIvaPorAcreditarRet.setAsiento(asiento);
			final String descripcion2=MessageFormat.format("Fac: {0} {1,date,short} ({2})", pago.getDOCUMENTO(),pago.getFECHA_DOCTO(),pago.getMONEDA());				
			abonoIvaPorAcreditarRet.setDescripcion2(descripcion2);
			abonoIvaPorAcreditarRet.setReferencia(pago.getNOMBRE());
			abonoIvaPorAcreditarRet.setReferencia2("IVAR02");
			abonoIvaPorAcreditarRet.setConcepto(abonoIvaPorAcreditarRet.getCuenta().getConcepto("IVAR02"));
			
			PolizaDet cargoIvaRetenidoPendiente=poliza.agregarPartida();
			cargoIvaRetenidoPendiente.setCuenta(getCuenta("205"));
			cargoIvaRetenidoPendiente.setDebe(imp);
			cargoIvaRetenidoPendiente.setAsiento(asiento);
			cargoIvaRetenidoPendiente.setDescripcion2(descripcion2);
			cargoIvaRetenidoPendiente.setReferencia(pago.getNOMBRE());
			cargoIvaRetenidoPendiente.setReferencia2("IMPR03");
			cargoIvaRetenidoPendiente.setConcepto(cargoIvaRetenidoPendiente.getCuenta().getConcepto("IMPR03"));
			
			if(pago.getFLETE().doubleValue()>0){
				PolizaDet abonoIvaRetenido=poliza.agregarPartida();
				abonoIvaRetenido.setCuenta(getCuenta("205"));
				abonoIvaRetenido.setHaber(imp);
				abonoIvaRetenido.setAsiento(asiento);				
				abonoIvaRetenido.setDescripcion2(descripcion2);
				abonoIvaRetenido.setReferencia(pago.getNOMBRE());
				abonoIvaRetenido.setReferencia2("IMPR02");
				abonoIvaRetenido.setConcepto(abonoIvaRetenido.getCuenta().getConcepto("IMPR02"));
			}
			
		}
		
	}else{
		asiento="TRANSITO";
	}
		

	}
	
	
	public CuentaContable getCuenta(String clave){
		return ServiceLocator2.getCuentasContablesManager().buscarPorClave(clave);
	}
	
	private double getTipoDeCambioDelMes(final Date fecha){
		Periodo p=Periodo.getPeriodoEnUnMes(fecha);
		Date fechaX=DateUtils.addDays(p.getFechaFinal(), -1);
		
		System.out.println("Fecha X "+fechaX + " Fecha de que : "+fecha);
		
	// Ver con Ruben tipo cambio de factura con pago en el mismo mes	
	//	if(!DateUtil.isSameMonth(p.getFecha(), fecha))
		
		
		
		try {
			
			
				
			String sql="select factor from sx_tipo_de_cambio where fecha=?";
			Double res=(Double)ServiceLocator2.getJdbcTemplate().queryForObject(sql, new Object[]{fechaX},Double.class);
			Assert.notNull(res,MessageFormat.format("No encontro T.C para la fecha: {0,date,short} Mes: {1} ",fechaX));
			return res.doubleValue();
			
		} catch (EmptyResultDataAccessException de) {
			
			de.printStackTrace();
			throw new RuntimeException("No existe tipo de cambio para la fecha: "+fechaX);
			
		}
	}
	
	public static Date getPenultimoDiaDelMes(final Date fecha){
		Periodo p=Periodo.getPeriodoEnUnMes(fecha);
		return DateUtils.addDays(p.getFechaFinal(), -1);
	}
	
			
	
	
	public static class PagoCXP{
		
		private long CARGOABONO_ID;
		private Date fecha;
		private String REFERENCIA;
		private long CUENTA_ID;
		private String BANCO;
		private String FORMAPAGO;
		private String CLAVEPROV;
		private String NOMBRE;
		private BigDecimal IMPORTE;
		private BigDecimal TC_PAGO;
		private BigDecimal TC_REQ;
		private BigDecimal BONIFICACIONES;
		private BigDecimal DESCTO;
		private long CXP_ID;
		private String DOCUMENTO;
		private Date FECHA_DOCTO;
		private String MONEDA;
		private double TC;
		private BigDecimal APAGAR;
		private BigDecimal TOTAL;
		private BigDecimal IMPUESTO;
		private BigDecimal FLETE;
		private BigDecimal FLETE_IVA;
		private BigDecimal FLETE_RET;
		private double TOT_MN;
		private String CONCEPTO;
		private Date fechaCobro;
		private Long conceptoRequisicion;
		
		public long getCARGOABONO_ID() {
			return CARGOABONO_ID;
		}
		public void setCARGOABONO_ID(long cargoabono_id) {
			CARGOABONO_ID = cargoabono_id;
		}
		/**
		 * Fecha del cheque o transferencia para el pago de la factura del proveedor
		 * @return
		 */
		public Date getFecha() {
			return fecha;
		}
		public void setFecha(Date fecha) {
			this.fecha = fecha;
		}
		public String getREFERENCIA() {
			return REFERENCIA;
		}
		public void setREFERENCIA(String referencia) {
			REFERENCIA = referencia;
		}
		public long getCUENTA_ID() {
			return CUENTA_ID;
		}
		public void setCUENTA_ID(long cuenta_id) {
			CUENTA_ID = cuenta_id;
		}
		
		
		public String getBANCO() {
			return BANCO;
		}
		public void setBANCO(String formapago) {
			BANCO = formapago;
		}
		
		
		
		public String getFORMAPAGO() {
			return FORMAPAGO;
		}
		public void setFORMAPAGO(String formapago) {
			FORMAPAGO = formapago;
		}
		
		public String getCLAVEPROV() {
			return CLAVEPROV;
		}
		public void setCLAVEPROV(String cLAVEPROV) {
			CLAVEPROV = cLAVEPROV;
		}
		public String getNOMBRE() {
			return NOMBRE;
		}
		public void setNOMBRE(String nombre) {
			NOMBRE = nombre;
		}
		public BigDecimal getIMPORTE() {
			return IMPORTE;
		}
		
		public void setIMPORTE(BigDecimal importe) {
			IMPORTE = importe;
		}
		public long getCXP_ID() {
			return CXP_ID;
		}
		public void setCXP_ID(long cxp_id) {
			CXP_ID = cxp_id;
		}
		public String getDOCUMENTO() {
			return DOCUMENTO;
		}
		public void setDOCUMENTO(String documento) {
			DOCUMENTO = documento;
		}
		
		public String getCONCEPTO() {
			return CONCEPTO;
		}
		public void setCONCEPTO(String cONCEPTO) {
			CONCEPTO = cONCEPTO;
		}
		public String getMONEDA() {
			return MONEDA;
		}
		public void setMONEDA(String moneda) {
			MONEDA = moneda;
		}
		public double getTC() {
			return TC;
		}
		public void setTC(double tc) {
			TC = tc;
		}
		public BigDecimal getTOTAL() {
			return TOTAL;
		}
		public void setTOTAL(BigDecimal total) {
			TOTAL = total;
		}
		public BigDecimal getIMPUESTO() {
			return IMPUESTO;
		}
		public void setIMPUESTO(BigDecimal impuesto) {
			IMPUESTO = impuesto;
		}
		public BigDecimal getFLETE() {
			return FLETE;
		}
		public void setFLETE(BigDecimal flete) {
			FLETE = flete;
		}
		public BigDecimal getFLETE_IVA() {
			return FLETE_IVA;
		}
		public void setFLETE_IVA(BigDecimal flete_iva) {
			FLETE_IVA = flete_iva;
		}
		public BigDecimal getFLETE_RET() {
			return FLETE_RET;
		}
		public void setFLETE_RET(BigDecimal flete_ret) {
			FLETE_RET = flete_ret;
		}
		public double getTOT_MN() {
			return TOT_MN;
		}
		public void setTOT_MN(double tot_mn) {
			TOT_MN = tot_mn;
		}
		public BigDecimal getAPagarMonedaNacional(){
			return CantidadMonetaria.pesos(getAPAGAR()).amount();
		}
		public Date getFECHA_DOCTO() {
			return FECHA_DOCTO;
		}
		public void setFECHA_DOCTO(Date fecha_docto) {
			FECHA_DOCTO = fecha_docto;
		}
		public BigDecimal getAPAGAR() {
			return APAGAR;
		}
		public void setAPAGAR(BigDecimal apagar) {
			APAGAR = apagar;
		}
		public Date getFechaCobro() {
			return fechaCobro;
		}
		public void setFechaCobro(Date fechaCobro) {
			this.fechaCobro = fechaCobro;
		}
		public Long getConceptoRequisicion() {
			return conceptoRequisicion;
		}
		public void setConceptoRequisicion(Long conceptoRequisicion) {
			this.conceptoRequisicion = conceptoRequisicion;
		}
		public BigDecimal getBONIFICACIONES() {
			if(BONIFICACIONES==null)
				BONIFICACIONES=BigDecimal.ZERO;
			return BONIFICACIONES;
		}
		public void setBONIFICACIONES(BigDecimal bONIFICACIONES) {
			
			BONIFICACIONES = bONIFICACIONES;
		}
		
		public BigDecimal getImporteSinBonificaciones(){
			return getIMPORTE()
					.subtract(getBONIFICACIONES());
		}
		public BigDecimal getTC_PAGO() {
			return TC_PAGO;
		}
		public void setTC_PAGO(BigDecimal tC_PAGO) {
			TC_PAGO = tC_PAGO;
		}
		public BigDecimal getTC_REQ() {
			return TC_REQ;
		}
		public void setTC_REQ(BigDecimal tC_REQ) {
			TC_REQ = tC_REQ;
		}
		public BigDecimal getDESCTO() {
			return DESCTO;
		}
		public void setDESCTO(BigDecimal dESCTO) {
			DESCTO = dESCTO;
		}		
	}
	
	
	
	public static BigDecimal round(BigDecimal v){
		return CantidadMonetaria.pesos(v).amount();
	}
	
}

