package com.luxsoft.sw3.contabilidad.polizas.cxc;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_CobranzaFichasCxC implements IProcesador{
	
	/**
	 * -- Cargo a Bancos utilizando las Fichas de deposito de camioneta	 * 
	 * -- Abono a Clientes camioneta mediante la cobranza
	 * -- Cargo Iva en ventas por trasladar
	 * -- Abono Iva en ventas
	 * -- IETU Camioneta
	 * 
	 * 
	 */
	public void procesar(Poliza poliza, ModelMap model) {
		final String asiento= "COBRANZA FICHA";
		List<Ficha> fichas=(List<Ficha>)model.get("fichas");
		for(Ficha ficha:fichas){
			String desc2=MessageFormat.format("Ficha - {0}",ficha.getTipoDeFicha()+ " Folio: "+ficha.getFolio());
			PolizaDetFactory.generarPolizaDet(poliza,"102",ficha.getCuenta().getNumero().toString(), true, ficha.getTotal(), desc2, ficha.getOrigen().name(), ficha.getSucursal().getNombre(),asiento);
		}
		
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");
		Comparator<Pago> c=GlazedLists.beanPropertyComparator(Pago.class, "cliente.id");
		GroupingList<Pago> pagosPorSucursal=new GroupingList<Pago>(pagos,c);
		
		// Aculuar la cobranza PagoConCheque y PagoConEfectivo
		for(List<Pago> lpagos:pagosPorSucursal){
			BigDecimal totalAplicado=BigDecimal.ZERO;
			BigDecimal importeAplicado=BigDecimal.ZERO;
			BigDecimal ivaAplicado=BigDecimal.ZERO;
			BigDecimal aplFac=BigDecimal.ZERO;
			BigDecimal aplCar=BigDecimal.ZERO;
			
			Pago pago=lpagos.get(0);
			for(Pago p:lpagos){
				
				if( (p instanceof PagoConCheque) || (p instanceof PagoConEfectivo)){
					
					if(DateUtils.isSameDay(p.getPrimeraAplicacion(), poliza.getFecha()) && p.getOrigenAplicacion().equals("CRE")){
						String ref1="CRE";
						totalAplicado=totalAplicado.add(p.getAplicado(poliza.getFecha()));
						String origenAplicacion= p.getOrigenAplicacion();
						if(p.getAplicaciones().isEmpty()){
							origenAplicacion=p.getOrigen().toString();
							PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU05", true, MonedasUtils.calcularImporteDelTotal(p.getTotal()), "ACUMULABLE IETU ANTICIPO", ref1,p.getSucursal().getNombre(), asiento);
							PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA05", false,  MonedasUtils.calcularImporteDelTotal(p.getTotal()),"IETU ACUMULABLE ANTICIPO", ref1,p.getSucursal().getNombre(), asiento);
						}
						PolizaDetFactory.generarSaldoAFavor(poliza, p,  p.getOrigenAplicacion(), asiento);
						PolizaDetFactory.generarOtrosIngresos(poliza, p, p.getOrigenAplicacion(), asiento);
					}
				}
				
				List<Aplicacion> aplicaciones=p.getAplicaciones();
				
				if(!p.getAplicaciones().isEmpty()){
					if( (p instanceof PagoConCheque) || (p instanceof PagoConEfectivo)){
						if(DateUtils.isSameDay(p.getPrimeraAplicacion(), poliza.getFecha())){
							for(Aplicacion ap:aplicaciones){
								if(ap.getCargo() instanceof NotaDeCargo){
									aplCar=aplCar.add(ap.getImporte());
								}
								if(ap.getCargo() instanceof Venta){
									aplFac=aplFac.add(ap.getImporte());
								}
							}
						}
					}
				}
				
				
				importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);		
				ivaAplicado=PolizaUtils.calcularImpuesto(importeAplicado);
				importeAplicado=PolizaUtils.redondear(importeAplicado);
				ivaAplicado=PolizaUtils.redondear(ivaAplicado);
				
							
			}
			
			String ref2=pago.getSucursal().getNombre();
			String ref1=pago.getOrigenAplicacion();
			String desc2=pago.getNombre();
			BigDecimal ivaFac=MonedasUtils.calcularImpuestoDelTotal(aplFac);
			BigDecimal ivaCar=MonedasUtils.calcularImpuestoDelTotal(aplCar);
			
			//Abono a cliente Credito		
			PolizaDetFactory.generarPolizaDet(poliza,"106", pago.getClave(), false, totalAplicado, desc2, ref1, ref2, asiento);
			//Cargo Iva en ventas por trasladar  
	//		PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", true, ivaAplicado, desc2, ref1, ref2, asiento);
			//Abono Iva en ventas
	//		PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", false, ivaAplicado, desc2, ref1, ref2, asiento);
			
			//Cargo Iva en ventas por trasladar
			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", true, ivaFac, desc2, ref1, ref2, asiento);
			//Abono Iva en ventas
			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", false, ivaFac, desc2, ref1, ref2, asiento);
			
			//Cargo Iva en Otros Ingresos por trasladar
			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAO01", true, ivaCar, desc2, ref1, ref2, asiento);
			//Abono Iva en ventas
			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", false, ivaCar, desc2, ref1, ref2, asiento);
		
			
			
			
		}	
		
	}
	
	
	

}
