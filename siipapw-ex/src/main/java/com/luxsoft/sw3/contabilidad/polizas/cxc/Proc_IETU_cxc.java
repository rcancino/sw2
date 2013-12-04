package com.luxsoft.sw3.contabilidad.polizas.cxc;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.Matcher;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;
import com.luxsoft.sw3.contabilidad.polizas.cobranza.CobranzaUtils;

public class Proc_IETU_cxc implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model){
		depositables(poliza,model);
		depositosPorIdentificar(poliza, model);
		diferencias(poliza,model);
		anticipos(poliza, model);
		anticiposAplicacion(poliza, model); 
		disponiblesAplicacion(poliza, model);
		saldosAFavor(poliza, model);
		//cobranzaTarjeta(poliza,model);
	}
	
	
	public void depositables(Poliza poliza, ModelMap model) {
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");
		
		
		Matcher<Pago> matcher=new Matcher<Pago>() {
			public boolean matches(Pago p) {	
				if(!p.isAnticipo()){
					return CobranzaUtils.isDepositable(p);
				}
				return false;
			}			
		};
		FilterList<Pago> depositables=new FilterList<Pago>(pagos,matcher);
		
		BigDecimal importeAcumulado=obtenerIetuAcumulado(depositables, poliza.getFecha());
		
		for(final Pago pago:pagos){
			if(pago.isAnticipo())
				continue;			
			if(!DateUtils.isSameDay(poliza.getFecha(), pago.getPrimeraAplicacion())){				
				Aplicacion pAplicacion=(Aplicacion)CollectionUtils.find(pago.getAplicaciones(), new Predicate() {					
					public boolean evaluate(Object object) {
						Aplicacion aa=(Aplicacion)object;
						return DateUtils.isSameDay(aa.getFecha(), pago.getPrimeraAplicacion());
					}
				});				
				Assert.notNull(pAplicacion,"La fecha de la primera aplicacion del pago: "+pago.getId()+ " No es correcta, no existe una aplicacion con esa fecha");
				
				BigDecimal totalAplicado=pago.getAplicado(poliza.getFecha());
				BigDecimal importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);
				
				totalAplicado=PolizaUtils.redondear(totalAplicado);
				importeAplicado=PolizaUtils.redondear(importeAplicado);
				importeAcumulado=importeAcumulado.add(importeAplicado);
			}		
		}
		
		//Cobranza Tarjeta
		for(final Pago pago:pagos){
			if((pago instanceof PagoConTarjeta) && (!pago.isAnticipo()) ){
				if(DateUtils.isSameDay(poliza.getFecha(), pago.getPrimeraAplicacion())){				
					Aplicacion pAplicacion=(Aplicacion)CollectionUtils.find(pago.getAplicaciones(), new Predicate() {					
						public boolean evaluate(Object object) {
							Aplicacion aa=(Aplicacion)object;
							return DateUtils.isSameDay(aa.getFecha(), pago.getPrimeraAplicacion());
						}
					});				
					Assert.notNull(pAplicacion,"La fecha de la primera aplicacion del pago: "+pago.getId()+ " No es correcta, no existe una aplicacion con esa fecha");
					
					BigDecimal totalAplicado=pago.getAplicado(poliza.getFecha());
					BigDecimal importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);
					
					totalAplicado=PolizaUtils.redondear(totalAplicado);
					importeAplicado=PolizaUtils.redondear(importeAplicado);
					importeAcumulado=importeAcumulado.add(importeAplicado);
					System.out.println("Acumulado cobranza pago tar: "+importeAplicado);
				}
			}		
		}
		
		//IETU Camioneta
		PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU03", true, importeAcumulado, "ACUMULABLE IETU ","CRE", "OFICINAS", "IETU X");
		PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA03", false, importeAcumulado,"IETU ACUMULABLE ", "CRE", "OFICINAS", "X IETU");
	}
	
	private void depositosPorIdentificar(final Poliza poliza, ModelMap model) {
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");		
		String asiento="IETU por identificar";
		Matcher<Pago> matcher=new Matcher<Pago>() {
			public boolean matches(Pago entidad) {	
				if(entidad instanceof PagoConDeposito){
					PagoConDeposito pago=(PagoConDeposito)entidad;
					if(!pago.isAnticipo()){
						if(DateUtils.isSameDay(pago.getPrimeraAplicacion(), poliza.getFecha())){
							return !DateUtil.isSameMonth(pago.getPrimeraAplicacion(), pago.getFechaDeposito());
						}
					}
				}
				return false;
			}			
		};
		FilterList<Pago> porIdentificar=new FilterList<Pago>(pagos,matcher);
		//System.out.println("Depopsitos por identificar: "+porIdentificar.size());
		BigDecimal importeAcumulado=obtenerIetuAcumulado(porIdentificar, poliza.getFecha());
		//IETU Camioneta
		PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU04", false, importeAcumulado, "ACUMULABLE IETU "+asiento, "CRE", "OFICINAS", asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA04", true, importeAcumulado,"IETU ACUMULABLE "+asiento, "CRE", "OFICINAS", asiento);
		
	}
	
	private void diferencias(final Poliza poliza, ModelMap model) {
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");
		String asiento="IETU de Diferencias";
		Matcher<Pago> matcher=new Matcher<Pago>() {
			public boolean matches(Pago entidad) {	
				BigDecimal diferencia=entidad.getDiferencia();
				if(diferencia.doubleValue()>0 && DateUtils.isSameDay(entidad.getDirefenciaFecha(), poliza.getFecha())){
					return true;
				}
				return false;
			}			
		};
		FilterList<Pago> diferencias=new FilterList<Pago>(pagos,matcher);
		BigDecimal importeAcumuladoNormal=BigDecimal.ZERO;
		BigDecimal importeAcumuladoSAF=BigDecimal.ZERO;
		
		for(Pago pago:diferencias){
			
			BigDecimal diferencia=pago.getDiferencia();
			BigDecimal importeDiferencia=PolizaUtils.calcularImporteDelTotal(diferencia);
			importeDiferencia=PolizaUtils.redondear(importeDiferencia);
			
			if(DateUtils.isSameDay(pago.getDirefenciaFecha(), pago.getPrimeraAplicacion())){				
				importeAcumuladoNormal=importeAcumuladoNormal.add(importeDiferencia);
						
				
			}else{
				importeAcumuladoSAF=importeAcumuladoSAF.add(importeDiferencia);
			}
		}
		//Normal
		PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU07", true, importeAcumuladoNormal, "ACUMULABLE IETU "+asiento, "CRE", "OFICINAS", asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA07", false,importeAcumuladoNormal,"IETU ACUMULABLE "+asiento, "CRE", "OFICINAS", asiento);
		//DAF
		PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU06", false, importeAcumuladoSAF, "ACUMULABLE IETU "+asiento, "CRE", "OFICINAS", asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA06", true,importeAcumuladoSAF,"IETU ACUMULABLE "+asiento, "CRE", "OFICINAS", asiento);
		
		PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU07", true, importeAcumuladoSAF, "ACUMULABLE IETU "+asiento, "CRE", "OFICINAS", asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA07", false,importeAcumuladoSAF,"IETU ACUMULABLE "+asiento, "CRE", "OFICINAS", asiento);
	}
	
	private void anticipos(final Poliza poliza, ModelMap model) {
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");		
		String asiento="IETU anticipos";
		Matcher<Pago> matcher=new Matcher<Pago>() {
			public boolean matches(Pago entidad) {	
				if(CobranzaUtils.aplicaIetu(entidad)){
					return entidad.isAnticipo();
				}
				return false;
			}			
		};
		FilterList<Pago> anticipos=new FilterList<Pago>(pagos,matcher);
		
		BigDecimal importeAcumulado=BigDecimal.ZERO;
		for(Pago pago:anticipos){
			BigDecimal importe=MonedasUtils.calcularImporteDelTotal(pago.getImporte().abs());
			importeAcumulado=importeAcumulado.add(importe);
		}
		
		PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU05", true, importeAcumulado, "ACUMULABLE IETU "+asiento, "CRE", "OFICINAS", asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA05", false, importeAcumulado,"IETU ACUMULABLE "+asiento, "CRE", "OFICINAS", asiento);
		
	}
	
	private void anticiposAplicacion(final Poliza poliza, ModelMap model) {
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");		
		String asiento="IETU anticipos aplic";
		Matcher<Pago> matcher=new Matcher<Pago>() {
			public boolean matches(Pago entidad) {	
				if(CobranzaUtils.aplicaIetu(entidad)){
					return entidad.isAnticipo();
				}
				return false;
			}			
		};
		FilterList<Pago> porIdentificar=new FilterList<Pago>(pagos,matcher);
		
		BigDecimal importeAcumulado=obtenerIetuAcumulado(porIdentificar, poliza.getFecha());
		String ref1="CRE";
		String ref2="OFICINAS";
		
		//Cancelacion de IETU
		PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU05", false, importeAcumulado, "ACUMULABLE IETU ANTICIPO", ref1, ref2, asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA05", true, importeAcumulado,"IETU ACUMULABLE ANTICIPO", ref1, ref2, asiento);
		//IETU Credito
		PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU03", true, importeAcumulado, "ACUMULABLE IETU ", ref1, ref2, asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA03", false, importeAcumulado,"IETU ACUMULABLE ", ref1, ref2, asiento);
		
	}
	
	private void disponiblesAplicacion(final Poliza poliza ,ModelMap model){
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");
		String asiento="COBRANZA APLICACION SAF ";
		BigDecimal importeAcumulado=BigDecimal.ZERO;
		for(final Pago pago:pagos){
			if(pago.isAnticipo())
				continue;			
			if(!DateUtils.isSameDay(poliza.getFecha(), pago.getPrimeraAplicacion())){				
				Aplicacion pAplicacion=(Aplicacion)CollectionUtils.find(pago.getAplicaciones(), new Predicate() {					
					public boolean evaluate(Object object) {
						Aplicacion aa=(Aplicacion)object;
						return DateUtils.isSameDay(aa.getFecha(), pago.getPrimeraAplicacion());
					}
				});				
				Assert.notNull(pAplicacion,"La fecha de la primera aplicacion del pago: "+pago.getId()+ " No es correcta, no existe una aplicacion con esa fecha");
				
				BigDecimal totalAplicado=pago.getAplicado(poliza.getFecha());
				BigDecimal importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);
				
				totalAplicado=PolizaUtils.redondear(totalAplicado);
				importeAplicado=PolizaUtils.redondear(importeAplicado);
				importeAcumulado=importeAcumulado.add(importeAplicado);
			}
		
		}
		String ref1="CRE";
		String ref2="OFICINAS";
		//Cancelacion de IETU
		PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU06", false, importeAcumulado, "ACUMULABLE IETU SAF ", ref1, ref2, asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA06", true, importeAcumulado,"IETU ACUMULABLE SAF", ref1, ref2, asiento);
		//IETU Credito
		//PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU03", true, importeAcumulado, "ACUMULABLE IETU ", ref1, ref2, asiento);
		//PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA03", false, importeAcumulado,"IETU ACUMULABLE ", ref1, ref2, asiento);
		
	}
	
	private void saldosAFavor(final Poliza poliza,ModelMap model){
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");
		String asiento="IETU  Disponibles ";
		BigDecimal importeAcumulado=BigDecimal.ZERO;
		for(final Pago pago:pagos){
			if(pago.isAnticipo())
				continue;			
			if(CobranzaUtils.aplicaIetu(pago)){				
				BigDecimal disponible=pago.getDisponibleAlCorte(poliza.getFecha()).amount();
				if(disponible.doubleValue()>0){
					if(pago.getDiferencia().doubleValue()==0){
						BigDecimal importeDisponible=PolizaUtils.calcularImporteDelTotal(disponible);
						importeDisponible=PolizaUtils.redondear(importeDisponible);
						importeAcumulado=importeAcumulado.add(importeDisponible);
					
					}
				}
			}
		
		}
		String ref1="CRE";
		String ref2="OFICINAS";
		PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU06", true, importeAcumulado, "ACUMULABLE IETU "+asiento, ref1, ref2, asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA06", false, importeAcumulado,"IETU ACUMULABLE "+asiento, ref1, ref2, asiento);
	}
	
	
	
	private BigDecimal obtenerIetuAcumulado(List<Pago> pagoss,Date fecha){
		BigDecimal importeAcumulado=BigDecimal.ZERO;
		for(Pago pago:pagoss){			
			
			BigDecimal totalAbono=pago.getTotal();
			BigDecimal importeAbono=PolizaUtils.calcularImporteDelTotal(totalAbono);		
			BigDecimal impuestoAbono=PolizaUtils.calcularImpuesto(importeAbono);
			
			importeAbono=PolizaUtils.redondear(importeAbono);
			impuestoAbono=PolizaUtils.redondear(impuestoAbono);
			
			
			BigDecimal totalAplicado=pago.getAplicado(fecha);
			BigDecimal importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);
			
			totalAplicado=PolizaUtils.redondear(totalAplicado);
			importeAplicado=PolizaUtils.redondear(importeAplicado);
			importeAcumulado=importeAcumulado.add(importeAplicado);
					
		}
		return importeAcumulado;
	}
	
	 

}
