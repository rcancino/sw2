package com.luxsoft.sw3.contabilidad.polizas.compras;

import static com.luxsoft.sw3.contabilidad.polizas.PolizaUtils.calcularImporteDelTotal;
import static com.luxsoft.sw3.contabilidad.polizas.PolizaUtils.calcularImpuesto;
import static com.luxsoft.sw3.contabilidad.polizas.PolizaUtils.redondear;

import java.math.BigDecimal;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.compras.Proc_CxPCompras.PagoCXP;
import com.luxsoft.sw3.contabilidad.services.PolizaContableManager;
import com.luxsoft.utils.LoggerHelper;


/**
 * Implementacion de {@link PolizaContableManager} para la generación y mantenimiento 
 * de la poliza de descuentos en compras
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class Proc_DescuentosEnCompras {
	
	Logger logger=LoggerHelper.getLogger();
	
	
	
	protected void inicializarDatos(){}
	
	
	public void procesarPoliza(Poliza poliza) {
		registrarNotasDeDescuento(poliza);
		
	}
	
	private  void registrarNotasDeDescuento(Poliza poliza){		
		final String asiento="Notas";
		String sql="SELECT 'DESCUENTO' AS TIPO,C.CONCEPTO_NOTA,C.CXP_ID AS ORIGEN_ID,C.FECHA,DATE(C.CREADO) AS FECHA_REG,C.CLAVE,C.NOMBRE AS PROVEEDOR,C.DOCUMENTO,C.MONEDA,X.FECHA AS FECHA_DOCTO,C.TC,X.TC AS TC_DOCTO" +
				",CONCAT('Nota:',C.DOCUMENTO,' ',C.FECHA,' ',C.CONCEPTO_NOTA) AS CONCEPTO" +
				",CONCAT('Nota:',C.DOCUMENTO,' Fac:',X.DOCUMENTO,' ',X.FECHA,' ',C.CONCEPTO_NOTA) AS CONCEPTO_DET,C.TOTAL,ROUND((X.TOTAL*C.TC),2) AS TOT_MN,A.IMPORTE AS APLICADO,ROUND((A.IMPORTE*X.TC),2) AS APLIC_DOCTO,ROUND((A.IMPORTE*C.TC),2) AS APLIC_NOTA" +
	//			",CONCAT('Nota:',C.DOCUMENTO,' ',C.CONCEPTO_NOTA,' ',C.FECHA) AS CONCEPTO,ROUND(C.TOTAL*C.TC,2) AS TOTAL " +
				" FROM SX_CXP C JOIN sx_cxp_aplicaciones A ON(A.ABONO_ID=C.CXP_ID) JOIN SX_CXP X ON(X.CXP_ID=A.CARGO_ID) " +
	//			" FROM SX_CXP C " +
				" WHERE DATE(C.CREADO)=? AND C.TIPO='NOTA' " 
				//+"AND CONCEPTO_NOTA IN('DESCUENTO','DESCUENTO_FINANCIERO')"
				; 
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
		};
		
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);

		EventList<Map<String,Object>> source=GlazedLists.eventList(rows);
		Comparator<Map<String,Object>> comparator=new Comparator<Map<String,Object>>(){
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				Long id1=(Long)o1.get("ORIGEN_ID");
				Long id2=(Long)o2.get("ORIGEN_ID");
				return id1.compareTo(id2);
			}
		};
		GroupingList<Map<String,Object>> registrosAgrupados=new GroupingList<Map<String,Object>>(source, comparator);
//		int i=0;
//		for(List<Map<String,Object>> nota:registrosAgrupados){
//			System.err.println("nota: "+nota.get(i));
//			i=i+1;
//		}
		
		
		for(List<Map<String,Object>> nota:registrosAgrupados){
			
			
			
			Map<String,Object> primerRegistro=nota.get(0);
			
			Boolean descto_fin=(Boolean)primerRegistro.get("CONCEPTO_NOTA").equals("DESCUENTO_FINANCIERO");
			Boolean devolucion=(Boolean)primerRegistro.get("CONCEPTO_NOTA").equals("DEVLUCION");
			Boolean bonificacion=(Boolean)primerRegistro.get("CONCEPTO_NOTA").equals("BONIFICACION");
			Boolean descto_anticipo=(Boolean)primerRegistro.get("CONCEPTO_NOTA").equals("ANTICIPO_DESCUENTO");
			Boolean descuento=(Boolean)primerRegistro.get("CONCEPTO_NOTA").equals("DESCUENTO");
			String clave=(String)primerRegistro.get("CLAVE");
			String claveProv=(String)primerRegistro.get("CLAVE");
			String desc2=(String)primerRegistro.get("CONCEPTO");
			String ref1=(String)primerRegistro.get("PROVEEDOR");
			String ref2="TODAS";
			BigDecimal total=new BigDecimal( ((Number)primerRegistro.get("TOTAL")).doubleValue());
			BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total, 2);
			//BigDecimal impuesto=calcularImpuesto(importe);
			
			System.out.println("Docto : "+desc2+"Total nota : "+total+" Importe nota : "+importe);
			
			//Abono  a Cancelacion de Provision
			if(descto_fin){
				PolizaDetFactory.generarPolizaDet(poliza, "701", "PRFN03", false, importe, desc2, ref1, ref2, asiento);
			}
			
		/*	else if(devolucion){
				PolizaDetFactory.generarPolizaDet(poliza, "119", "DCOM01", false, importe, desc2, ref1, ref2, asiento);
			}else
				PolizaDetFactory.generarPolizaDet(poliza, "119", "ITNS04", false, importe, desc2, ref1, ref2, asiento);
			*/
			for(Map<String,Object> partida:nota){
				desc2=(String)partida.get("CONCEPTO_DET");
				BigDecimal aplicado=new BigDecimal( ((Number)partida.get("APLICADO")).doubleValue());
				BigDecimal importeApl=MonedasUtils.calcularImporteDelTotal(aplicado, 2);
				BigDecimal impuestoApl=MonedasUtils.calcularImpuestoDelTotal(aplicado, 2);
				
				Date fechaTc=DateUtils.addMonths((Date)partida.get("FECHA_DOCTO"), -1);
				Date fechaTc2=DateUtils.addMonths((Date)partida.get("FECHA_REG"), -1);
				Double totNota=(Double)partida.get("APLIC_NOTA");
				Double totAplic=(Double)partida.get("APLIC_DOCTO");
				Double totAplic3=0.00;
								
				//Definicion de tipo de cambio
				Double tcNota1=(Double)partida.get("TC");
				Double tc1=(Double) partida.get("TC_DOCTO");
				BigDecimal tc2=BigDecimal.ZERO;
				
				BigDecimal tcNota= BigDecimal.valueOf(tcNota1);
				BigDecimal tc= BigDecimal.valueOf(tc1);
				
				if(!DateUtil.isSameMonth((Date)partida.get("FECHA_REG"), (Date)partida.get("FECHA_DOCTO"))){
					fechaTc=DateUtils.addMonths((Date)partida.get("FECHA_DOCTO"), -1);
					fechaTc2=DateUtils.addMonths((Date)partida.get("FECHA_REG"), -1);
			//		tc=BigDecimal.valueOf(getTipoDeCambioDelMes(fechaTc));
					tc=BigDecimal.valueOf(getTipoDeCambioDelMes(fechaTc));
					tc2=BigDecimal.valueOf(getTipoDeCambioDelMes(fechaTc2));
					
					Double aplicadoD = aplicado.doubleValue();  
					
					if(partida.get("MONEDA").equals("USD")){
						totAplic=aplicadoD*tc2.doubleValue();
						
						totAplic3=aplicadoD*tc1.doubleValue();
						
						System.err.println("Docto : "+desc2+" MON:"+partida.get("MONEDA")+" TC1 "+tcNota1+" totAplic "+totAplic);
						
					}else{
						totAplic=aplicadoD;	
					}
					
				}
				
				 BigDecimal totAplic2 = BigDecimal.valueOf(totAplic);
				 BigDecimal totNota2 = BigDecimal.valueOf(totNota);
				 BigDecimal importeNota2=MonedasUtils.calcularImporteDelTotal(totNota2);
				 BigDecimal importeNota3=BigDecimal.valueOf(totAplic3);
				 importeNota3=MonedasUtils.calcularImporteDelTotal(importeNota3);
				 BigDecimal ivaNota2=MonedasUtils.calcularImpuestoDelTotal(totNota2);

				
				System.out.println("Docto : "+desc2+" totAplic "+totAplic+" Aplicado : "+aplicado+" ImporteApli : "+importeNota2+" ImpuestoApl : "+impuestoApl+" tc "+getTipoDeCambioDelMes(fechaTc)+" TC nota "+tcNota1+ " MON "+partida.get("MONEDA"));
				
				//Abono  a Cancelacion de Provision
				if(devolucion){
					PolizaDetFactory.generarPolizaDet(poliza, "119", "DCOM01", false, importeNota2, desc2, ref1, ref2, asiento);
				}else if(bonificacion){
					PolizaDetFactory.generarPolizaDet(poliza, "701", "PRFN03", false, importeNota2, desc2, ref1, ref2, asiento);
				}else if(descto_anticipo){
					PolizaDetFactory.generarPolizaDet(poliza, "119", "ITNS04", false, importeNota2, desc2, ref1, ref2, asiento);
				}else if(descuento){
					
					if(partida.get("MONEDA").equals("USD")){
						BigDecimal difNota=importeNota2.subtract(importeNota3);
						
						PolizaDetFactory.generarPolizaDet(poliza, "119", "ITNS04", false, importeNota3, desc2, ref1, ref2, asiento);			
						
						if(difNota.doubleValue()>0)
							PolizaDetFactory.generarPolizaDet(poliza, "701", "PRFN04", false, difNota.abs(), "VARIACION TRANSITO "+desc2, ref1, ref2, asiento);
						else
							PolizaDetFactory.generarPolizaDet(poliza, "705", "GSTF01", true, difNota.abs(), "VARIACION TRANSITO "+desc2, ref1, ref2, asiento);
					}else{
						PolizaDetFactory.generarPolizaDet(poliza, "119", "ITNS04", false, importeNota2, desc2, ref1, ref2, asiento);
					}
					
				}
				


				String cta="200";
				if(partida.get("MONEDA").equals("USD")){
					cta="201";
				}
				
				//Cargo a Descuentos sobre Compra
				if(descto_fin){
					PolizaDetFactory.generarPolizaDet(poliza, cta,  claveProv, true, totAplic2, desc2, ref1, ref2, asiento);
					
				}else if(descto_anticipo) {
					PolizaDetFactory.generarPolizaDet(poliza, "119", "INVA01", true, totAplic2, desc2, ref1, ref2, asiento);
					
				}else if(devolucion){
					PolizaDetFactory.generarPolizaDet(poliza, cta, claveProv, true, totAplic2, desc2, ref1, ref2, asiento);
					
				}else if(bonificacion){
					PolizaDetFactory.generarPolizaDet(poliza, cta, claveProv, true, totAplic2, desc2, ref1, ref2, asiento);
						
				}else {					
					PolizaDetFactory.generarPolizaDet(poliza, cta, claveProv, true, totAplic2, desc2, ref1, ref2, asiento);
					
				}
				
				//Abono  a IVA por Acreditar en Compras
				PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC02", false, ivaNota2,desc2, ref1, ref2, asiento);
	
				/*
				 * Para el TC de la nota, se debe considerar el que se utilizo con el pago correspondiente en las facs o el determinado por Contabilidad
				 * en el registro de la nota en sx_cxp  2013/06/17
				*/
				double aplicProv=0.00;
				aplicProv=aplicado.doubleValue()*tc1;
				
				
				if(partida.get("MONEDA").equals("USD")){
					BigDecimal diferenciaIva=BigDecimal.ZERO;
					BigDecimal ivaDiferencia=BigDecimal.ZERO;
					BigDecimal diferencia=BigDecimal.ZERO;
					diferencia=totNota2.subtract(totAplic2) ;				
					
					System.err.println("Docto : "+desc2+" tc2 "+tc2+" diferencia "+diferencia+" totAplic "+totAplic);
					
					if(!DateUtil.isSameMonth((Date)partida.get("FECHA_REG"), (Date)partida.get("FECHA_DOCTO"))){
						ivaDiferencia=MonedasUtils.calcularImpuestoDelTotal(totNota2.subtract(BigDecimal.valueOf(aplicProv))) ;
						tc=BigDecimal.valueOf(tc1);
					}else{
						ivaDiferencia=MonedasUtils.calcularImpuestoDelTotal( diferencia);						
					}	
						
					
					diferenciaIva=diferenciaIva.add(ivaDiferencia);			
					
					System.err.println("TotNota "+totNota2+" aplic Prov "+aplicProv+" aplic normal "+totAplic+"IvaDif "+ivaDiferencia+" Diferencia "+diferencia);
					

					
													
			

				//IVA por acreditar en compras 
					clave="IVAC02";
					desc2=MessageFormat.format("{0} ({1} T.C:{2,number,##.####})" ,(String)partida.get("CONCEPTO_DET"),(String)partida.get("MONEDA"),tc);					
			//		ivaDiferencia=round(ivaDiferencia).abs();
					
					ivaDiferencia=round(ivaDiferencia);
					
					Boolean res=false;					
					if(ivaDiferencia.doubleValue()<0) res=res; else res=true;					
					
					PolizaDetFactory.generarPolizaDet(poliza, "117", clave, res, ivaDiferencia.abs(), desc2, ref1, ref2, asiento);
					
				//Variacion Cambiaria de IVA por Acred en compras 					
					String cuenta="";					
					if(ivaDiferencia.doubleValue()>0){ cuenta="701"; clave="PRFN04"; }else{ cuenta="705"; clave="GSTF01";}					
					desc2="VARIACION IVA "+desc2;					
					ivaDiferencia=round(ivaDiferencia);
					res=false;					
					if(ivaDiferencia.doubleValue()>0) res=res; else res=true;					
					
					PolizaDetFactory.generarPolizaDet(poliza, cuenta, clave, res, ivaDiferencia.abs(), desc2, ref1, ref2, asiento);				

				//Variacion Cambiaria de Pago					
					cuenta="";					
					if(diferencia.doubleValue()<0){ cuenta="701"; clave="PRFN04"; }else{ cuenta="705"; clave="GSTF01";}	
					desc2=MessageFormat.format("VARIACION NOTA {0} ({1} T.C:{2,number,##.####})"
							,(String)partida.get("CONCEPTO"),(String)partida.get("MONEDA"),tcNota);
					diferencia=round(diferencia);
					res=false;					
					if(diferencia.doubleValue()<0) res=res; else res=true;	
					
					PolizaDetFactory.generarPolizaDet(poliza, cuenta, clave, res, diferencia.abs(), desc2, ref1, ref2, asiento);					
				}				
			}
		}	
	}
	
	public double getTipoDeCambioDelMes(final Date fecha){
		Periodo p=Periodo.getPeriodoEnUnMes(fecha);
		Date fechaX=DateUtils.addDays(p.getFechaFinal(), -1);
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
	public static BigDecimal round(BigDecimal v){
		return CantidadMonetaria.pesos(v).amount();
	}	
}
