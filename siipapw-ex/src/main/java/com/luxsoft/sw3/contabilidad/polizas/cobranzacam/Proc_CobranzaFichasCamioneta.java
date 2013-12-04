package com.luxsoft.sw3.contabilidad.polizas.cobranzacam;

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

public class Proc_CobranzaFichasCamioneta  implements IProcesador{
	
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
				if( (!p.isAnticipo()) && ((p instanceof PagoConCheque) || (p instanceof PagoConEfectivo))  ){
					if(DateUtils.isSameDay(p.getPrimeraAplicacion(), poliza.getFecha())){
						totalAplicado=totalAplicado.add(p.getAplicado(poliza.getFecha()));
						PolizaDetFactory.generarSaldoAFavor(poliza, p, p.getOrigenAplicacion(), asiento);
						PolizaDetFactory.generarOtrosIngresos(poliza, p, p.getOrigenAplicacion(), asiento);
						
						System.out.println("APLICADO : " + totalAplicado + "IS ANTICIPO :" + pago.isAnticipo() );
					}
				}
				System.out.println(" TOTAL APLICADO : " + totalAplicado + "IS ANTICIPO :" + pago.isAnticipo() );
			}
			importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);		
			ivaAplicado=PolizaUtils.calcularImpuesto(importeAplicado);
			importeAplicado=PolizaUtils.redondear(importeAplicado);
			ivaAplicado=PolizaUtils.redondear(ivaAplicado);
			
			
			String ref2=pago.getSucursal().getNombre();
			String ref1=pago.getOrigenAplicacion();
			//Abono a cliente camioneta
			PolizaDetFactory.generarPolizaDet(poliza,"105", pago.getSucursal().getId().toString(), false, totalAplicado, "Clientes CAM cobranza", ref1, ref2, asiento);
			//Cargo Iva en ventas por trasladar
			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", true, ivaAplicado, "", ref1, ref2, asiento);
			//Abono Iva en ventas
			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", false, ivaAplicado, "", ref1, ref2, asiento);
					
			
		}		
	}

}
