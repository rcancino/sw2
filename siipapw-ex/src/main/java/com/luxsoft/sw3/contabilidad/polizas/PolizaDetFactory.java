package com.luxsoft.sw3.contabilidad.polizas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.hssf.record.formula.functions.Round;
import org.apache.poi.hssf.record.formula.functions.Value;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;

/**
 * Factory class para simplificar y centralizar la creacion de instancias de {@link PolizaDet}
 * 
 * @author Ruben Cancino
 *
 */
public class PolizaDetFactory {
	
	public static void registrarAnticipoDeClientes(Poliza poliza,Abono a,String asiento,boolean abono){
		Pago pago=(Pago)a;
	//	BigDecimal importe=MonedasUtils.calcularImporteDelTotal(a.getTotal());
		BigDecimal importe=round(MonedasUtils.calcularImporteDelTotal(a.getTotal(), 4));
		
		PolizaDet registro=poliza.agregarPartida();
		if(abono)
			registro.setHaber(importe);
		else
			registro.setDebe(importe);
		
		CuentaContable cuenta=getCuenta("204");
		registro.setCuenta(cuenta);
		ConceptoContable cc=cuenta.getConcepto(pago.getClave());
		if(cc==null){
			cc=new ConceptoContable();
			cc.setCuenta(cuenta);
			cc.setClave(pago.getClave());
			cc.setDescripcion(pago.getNombre());
			cuenta.getConceptos().add(cc);
			cc=(ConceptoContable)ServiceLocator2.getUniversalDao().save(cc);
			//cuenta=ServiceLocator2.getCuentasContablesManager().salvar(cuenta);
			//registro.setCuenta(cuenta);
			//cc=cuenta.getConcepto(pago.getClave());
			System.out.println("Concepto generado: "+cc);
		}
		registro.setConcepto(cc);
		
		String desc2="Ant {0} de cte: {1}";
		desc2=MessageFormat.format(desc2, pago.getTipo(),pago.getNombre());
		registro.setDescripcion2(desc2);
		registro.setReferencia(pago.getOrigen().toString().substring(0,3));
		registro.setReferencia2(pago.getSucursal().getNombre());
		registro.setAsiento(asiento);
		
	}
	
	public static ConceptoContable generarConceptoContable(String clave, String nombre,String cuentaC){
		try {
			CuentaContable cuenta=getCuenta(cuentaC);
			ConceptoContable cc=cuenta.getConcepto(clave);
			if(cc==null){
				cc=new ConceptoContable();
				cc.setCuenta(cuenta);
				cc.setClave(clave);
				cc.setDescripcion(nombre);
				cuenta.getConceptos().add(cc);
				cc=(ConceptoContable)ServiceLocator2.getUniversalDao().save(cc);
				//cuenta=ServiceLocator2.getCuentasContablesManager().salvar(cuenta);
				//registro.setCuenta(cuenta);
				//cc=cuenta.getConcepto(pago.getClave());
				System.out.println("Concentpo generado: "+cc);
			}
			return cc;
		} catch (Exception e) {
			throw new RuntimeException("Imposible generar el concepto Clave: "+clave+ " Nombre: "+nombre+ " Cuneta: "+cuentaC+ ExceptionUtils.getRootCauseMessage(e));
		}
		
	}
	
	
	public static  PolizaDet registrarIvaVentas(Poliza poliza,Abono a,String concepto,boolean abono){
	//	BigDecimal iva=MonedasUtils.calcularImpuesto((a.getTotal().divide(new BigDecimal(1).add(MonedasUtils.IVA), BigDecimal.ROUND_HALF_EVEN)).setScale(6,BigDecimal.ROUND_HALF_EVEN));
	//	BigDecimal iva=round(MonedasUtils.calcularImpuestoDelTotal(a.getTotal()));
		BigDecimal iva=round(MonedasUtils.calcularImpuesto((MonedasUtils.calcularImporteDelTotal(a.getTotal(), 4))));
		PolizaDet registro=poliza.agregarPartida();
		if(abono)
			registro.setHaber(iva);
		else
			registro.setDebe(iva);
		registro.setCuenta(getCuenta("206"));
		registro.setConcepto(registro.getCuenta().getConcepto(concepto));
		registro.setReferencia(a.getOrigen().toString().substring(0,3));
		registro.setReferencia2(a.getSucursal().getNombre());
		return registro;
	}
	
	public static void registrarIetuVentas(Poliza poliza,Abono a,String conceptoCargo,String conceptoAbono,String ref1,String asiento){
		BigDecimal importe=MonedasUtils.calcularImporteDelTotal(a.getImporte().abs());
		
		PolizaDet cargo=poliza.agregarPartida();
		cargo.setDebe(importe);
		cargo.setCuenta(getCuenta("902"));
		cargo.setConcepto(cargo.getCuenta().getConcepto(conceptoCargo));
		cargo.setDescripcion2("IETU ACUMULABLE "+asiento);
		cargo.setReferencia(ref1);
		cargo.setReferencia2(a.getSucursal().getNombre());
		cargo.setAsiento(asiento);
		
		PolizaDet abono=poliza.agregarPartida();
		abono.setHaber(importe);
		abono.setCuenta(getCuenta("903"));
		abono.setConcepto(abono.getCuenta().getConcepto(conceptoAbono));
		abono.setDescripcion2("ACUMULABLE IETU"+asiento);
		abono.setReferencia(ref1);
		abono.setReferencia2(a.getSucursal().getNombre());
		abono.setAsiento(asiento);
	}
	
	public static void registrarCancelacionIetuVentas(Poliza poliza,Abono a,String conceptoCargo,String conceptoAbono,String ref1){
		BigDecimal importe=MonedasUtils.calcularImporteDelTotal(a.getImporte().abs());
		
		PolizaDet cargo=poliza.agregarPartida();
		cargo.setDebe(importe);
		cargo.setCuenta(getCuenta("903"));
		cargo.setConcepto(cargo.getCuenta().getConcepto(conceptoCargo));
		cargo.setReferencia(ref1);
		cargo.setReferencia2(a.getSucursal().getNombre());
		
		PolizaDet abono=poliza.agregarPartida();
		abono.setHaber(importe);
		abono.setCuenta(getCuenta("902"));
		abono.setConcepto(abono.getCuenta().getConcepto(conceptoAbono));
		abono.setReferencia(ref1);
		abono.setReferencia2(a.getSucursal().getNombre());
	}
	
	/**
	 * Metodo generico para simplificar la creacion de instancias de PolizaDet
	 * @param poliza 
	 * @param cuenta
	 * @param concepto
	 * @param cargo
	 * @param importe
	 * @param desc2
	 * @param ref1
	 * @param ref2
	 * @param asiento
	 * @return
	 */
	public static PolizaDet generarPolizaDet(Poliza poliza,String cuenta,String concepto,boolean cargo,BigDecimal importe,String desc2
			,String ref1,String ref2,String asiento){
		PolizaDet det=poliza.agregarPartida();
		det.setDescripcion2(desc2);
		det.setReferencia(ref1);
		det.setReferencia2(ref2);
		det.setAsiento(asiento);
		if(cargo)
			det.setDebe(importe);
		else
			det.setHaber(importe);
		det.setCuenta(getCuenta(cuenta));
		det.setConcepto(det.getCuenta().getConcepto(concepto));
		return det;
	}
	
	
	/**
	 * Metodo generico para simplificar la creacion de instancias de PolizaDet
	 * @param poliza 
	 * @param cuenta
	 * @param concepto
	 * @param cargo
	 * @param importe
	 * @param desc2
	 * @param ref1
	 * @param ref2
	 * @param asiento
	 * @return
	 */
	public static PolizaDet generarPolizaDetPorDescripcionDeConcepto(Poliza poliza,String cuenta,String descripcionConcepto,boolean cargo,BigDecimal importe,String desc2
			,String ref1,String ref2,String asiento){
		PolizaDet det=poliza.agregarPartida();
		det.setDescripcion2(desc2);
		det.setReferencia(ref1);
		det.setReferencia2(ref2);
		det.setAsiento(asiento);
		if(cargo)
			det.setDebe(importe);
		else
			det.setHaber(importe);
		det.setCuenta(getCuenta(cuenta));
		det.setConcepto(det.getCuenta().getConceptoPorDescripcion(descripcionConcepto));
		return det;
	}
	
	
	/**
	 * Metodo para simplificar la generacion de regostros para saldo a favor
	 * 
	 * @param poliza
	 * @param deposito
	 * @param ref1
	 * @param asiento
	 */
	public static void generarSaldoAFavor(Poliza poliza,Abono deposito,String ref1,String asiento){
		
		Pago pag=(Pago) deposito;
		
		if(!pag.isAnticipo())
		{
			
		
		String ref2=deposito.getSucursal().getNombre();
		BigDecimal disponible=deposito.getDisponibleAlCorte(poliza.getFecha()).amount();
		if(disponible.doubleValue()>0){
			BigDecimal diferencia=deposito.getDiferencia();
			if(    (deposito.getDirefenciaFecha()==null) 
				|| (poliza.getFecha().compareTo(deposito.getDirefenciaFecha())<0)
					){
				diferencia=BigDecimal.ZERO;
			}
			if(diferencia.doubleValue()==0){
				BigDecimal importeDisponible=PolizaUtils.calcularImporteDelTotal(disponible);
				BigDecimal ivaDisponible=PolizaUtils.calcularImpuesto(importeDisponible);
				importeDisponible=PolizaUtils.redondear(importeDisponible);
				ivaDisponible=PolizaUtils.redondear(ivaDisponible);
				
				String sufix="01";
				if(ref1.equals("MOS"))
					sufix="01";
				if(ref1.equals("CAM"))
					sufix="03";
				if(ref1.equals("CRE"))
					sufix="02";
				if(ref1.equals("CHE"))
					sufix="02";
				
				//Saldo a favor
				
				//Abono a saldos a favor
				PolizaDetFactory.generarPolizaDet(poliza, "203", "DIVR"+sufix, false, importeDisponible,"SAF: "+deposito.getInfo()+" "+deposito.getNombre(), ref1, ref2, asiento+" SAF "+ref1 );
				
				/*
				 * El iva en saldo a Favor se manda directamente a ventas
				 */
				
				//Cargo a IVA x trasladar
	//			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", true, ivaDisponible,"IVA EN VENTAS POR TRASLADAR "+deposito.getInfo(), ref1, ref2, asiento+" SAF");
				//Abono a IVA en ventas
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", false, ivaDisponible,"IVA EN VENTAS "+deposito.getInfo(), ref1, ref2, asiento+" SAF");
			
				// Ajuste IETU Juridico Entrada de saldo a Favor  
				//PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU06", true, importeDisponible, "ACUMULABLE IETU SAF", ref1, ref2, asiento);
				//PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA06", false, importeDisponible,"IETU ACUMULABLE SAF", ref1, ref2, asiento);
				
				
			}
			}
		}
	}
	
	/**
	 * Metodo para simplificar la generacion de regostros para otros ingresos
	 * 
	 * @param poliza
	 * @param abono
	 * @param ref1
	 * @param asiento
	 */
	public static void generarOtrosIngresos(Poliza poliza,Abono abono,String ref1,String asiento){
		String ref2=abono.getSucursal().getNombre();
		BigDecimal diferencia=abono.getDiferencia();
		if(diferencia.doubleValue()>0 && DateUtils.isSameDay(abono.getDirefenciaFecha(), poliza.getFecha())){
			if(abono.getDiferencia().doubleValue()>0){
				BigDecimal importeDiferencia=PolizaUtils.calcularImporteDelTotal(diferencia);
				BigDecimal ivaDiferencia=PolizaUtils.calcularImpuesto(importeDiferencia);
				importeDiferencia=PolizaUtils.redondear(importeDiferencia);
				ivaDiferencia=PolizaUtils.redondear(ivaDiferencia);
				String sufix="01";
				if(ref1.equals("MOS"))
					sufix="01";
				if(ref1.equals("CAM"))
					sufix="03";
				if(ref1.equals("CRE"))
					sufix="02";
				if(ref1.equals("CHE"))
					sufix="04";
				if(DateUtils.isSameDay(abono.getDirefenciaFecha(), abono.getPrimeraAplicacion())){
					//Abono Otros ingresos
					PolizaDetFactory.generarPolizaDet(poliza, "702", "OING"+sufix, false, diferencia,"OI AJUSTE Menor a $10: "+abono.getInfo(), ref1, ref2, asiento+" "+ref1);
					

					if(abono instanceof PagoConDeposito){
						PagoConDeposito dep= (PagoConDeposito) abono;
						BigDecimal importeDif=MonedasUtils.calcularImporteDelTotal(diferencia, 2);						
						BigDecimal ivaDif=MonedasUtils.calcularImpuestoDelTotal(diferencia);
						String desc2=MessageFormat.format(" {0} Ref:{1}  F.D:"
								, dep.getBanco(),dep.getReferenciaBancaria())
								+new SimpleDateFormat("dd/MM/yyyy").format(dep.getFechaDeposito());
						
						if(!DateUtil.isSameMonth(abono.getPrimeraAplicacion(), dep.getFechaDeposito())){
							PolizaDetFactory.generarPolizaDet(poliza, "203", "DEPI01", true, diferencia,"OI AJUSTE Menor a $10: "+abono.getInfo(), ref1, ref2, asiento+" "+ref1);
					//		PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAD01", true, ivaDif, desc2, ref1, ref2, asiento);
						}
						
						
						
						if(diferencia.doubleValue()>10){
							if(!DateUtil.isSameMonth(dep.getPrimeraAplicacion(), dep.getFechaDeposito())){
								PolizaDetFactory.generarPolizaDet(poliza, "203", "DEPI01", true, importeDif,desc2, ref1, ref2, asiento+" "+ref1);
						//		PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAD01", true, ivaDif, desc2, ref1, ref2, asiento);
							}	
						}
						
					}
				}else{
					//Abono a Otros ingresos
					PolizaDetFactory.generarPolizaDet(poliza, "702", "OING"+sufix, false, diferencia,"OI AJUSTE < $10 "+abono.getInfo(), ref1, ref2, asiento+" "+ref1);
					
					// Cargo a Acredores diversos
					PolizaDetFactory.generarPolizaDet(poliza, "203", "DIVR"+sufix, true, diferencia,"OI AJUSTE < $10 "+abono.getInfo(), ref1, ref2, asiento);
					
					//PolizaDetFactory.generarPolizaDet(poliza, "902", "IETUA07", false, importeAcumulado, cuentaAIETUDesc +"ACUMULABLE IETU OI", ref1, ref2, asiento);
					//PolizaDetFactory.generarPolizaDet(poliza, "903", "AIETU07", true, importeAcumulado,cuentaIETUADesc +"IETU ACUMULABLE OI", ref1, ref2, asiento);
				
				}
				
			}
		}
	}
	

	public static ConceptoContable altasDeConcepto(CuentaContable cuenta,String clave,String descripcion){
		ConceptoContable c=new ConceptoContable();
		c.setCuenta(cuenta);
		c.setClave(clave);
		c.setDescripcion(descripcion);
		return (ConceptoContable)ServiceLocator2.getHibernateTemplate().merge(c);
	}
	
	
	
	public static CuentaContable getCuenta(String clave){
		return ServiceLocator2.getCuentasContablesManager().buscarPorClave(clave);
	}
	
	public static BigDecimal round(BigDecimal v){
		return CantidadMonetaria.pesos(v).amount();
	}
	
}
