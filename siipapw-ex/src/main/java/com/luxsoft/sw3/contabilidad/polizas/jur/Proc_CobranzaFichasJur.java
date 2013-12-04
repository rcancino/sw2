package com.luxsoft.sw3.contabilidad.polizas.jur;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_CobranzaFichasJur implements IProcesador{
	
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
		Comparator<Pago> c=GlazedLists.beanPropertyComparator(Pago.class, "sucursal.id");
		GroupingList<Pago> pagosPorSucursal=new GroupingList<Pago>(pagos,c);
		// Aculuar la cobranza PagoConCheque y PagoConEfectivo
		for(List<Pago> lpagos:pagosPorSucursal){
			BigDecimal totalAplicado=BigDecimal.ZERO;
			BigDecimal importeAplicado=BigDecimal.ZERO;
			BigDecimal ivaAplicado=BigDecimal.ZERO;
			
			Pago pago=lpagos.get(0);
			for(Pago p:lpagos){
				if( (p instanceof PagoConCheque) || (p instanceof PagoConEfectivo)){
					if(DateUtils.isSameDay(p.getPrimeraAplicacion(), poliza.getFecha())){
						totalAplicado=totalAplicado.add(p.getAplicado(poliza.getFecha()));
						PolizaDetFactory.generarSaldoAFavor(poliza, p, p.getOrigenAplicacion(), asiento);
						PolizaDetFactory.generarOtrosIngresos(poliza, p, p.getOrigenAplicacion(), asiento);
					}
				}
			}
			importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);		
			ivaAplicado=PolizaUtils.calcularImpuesto(importeAplicado);
			importeAplicado=PolizaUtils.redondear(importeAplicado);
			ivaAplicado=PolizaUtils.redondear(ivaAplicado);
			
			
			String ref2=pago.getSucursal().getNombre();
			String ref1=pago.getOrigenAplicacion();
			//Abono a cliente Credito
			
			
			PolizaDetFactory.generarPolizaDet(poliza,"114", pago.getClave(), false, totalAplicado, "Aplicacion de Cobranza JUR", ref1, ref2, asiento);
			//Cargo Iva en ventas por trasladar
			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", true, ivaAplicado, "", ref1, ref2, asiento);
			//Abono Iva en ventas
			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", false, ivaAplicado, "", ref1, ref2, asiento);
					
			//IETU Camioneta
			PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU03", true, importeAplicado, "ACUMULABLE IETU ", ref1, ref2, asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA03", false, importeAplicado,"IETU ACUMULABLE ", ref1, ref2, asiento);
		}	
		
	}
	
	
	

}
