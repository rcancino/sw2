package com.luxsoft.sw3.contabilidad.polizas.ventas;

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
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_PagosChequeEfectivoMostrador implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		
		String asiento="COBRANZA FICHA";
		
		List<Ficha> fichas=(List<Ficha>)model.get("fichas");
		if(fichas==null) return;
		for(Ficha ficha:fichas){
			String desc2=MessageFormat.format("Ficha - {0}",ficha.getTipoDeFicha()+ " Folio: "+ficha.getFolio());
			PolizaDetFactory.generarPolizaDet(poliza,"102",ficha.getCuenta().getNumero().toString(), true, ficha.getTotal(), desc2, ficha.getOrigen().name(), ficha.getSucursal().getNombre(),asiento+" "+ficha.getOrigen().name());
		}
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");
		if(pagos==null) return;
		
	/*	for(Pago pag:pagos){
			System.out.println("el pago" + pag.getId()+"       " + pag.getTotal() );
			if(pag.isAnticipo()){
				System.out.println("el pago" + pag.getId()+"       " + pag.getTotal() +"es anticipomjjjjjjjjjjjjjjjjjjjj");
				BigDecimal totalAnticipo=pag.getTotal();
				BigDecimal importeAnticipo=MonedasUtils.calcularImporteDelTotal(totalAnticipo);
				BigDecimal impuestoAnticipo= MonedasUtils.calcularImpuestoDelTotal(totalAnticipo);
				PolizaDetFactory.generarPolizaDet(poliza,"204",pag.getClave(), false,importeAnticipo,"ANTICIPO "+ pag.getInfo(), pag.getOrigen().name(),pag.getSucursal().getNombre(),asiento+" "+pag.getOrigen().name());	
				PolizaDetFactory.generarPolizaDet(poliza,"206","IVAA01", false,impuestoAnticipo,"ANTICIPO "+ pag.getInfo(), pag.getOrigen().name(),pag.getSucursal().getNombre(),asiento+" "+pag.getOrigen().name());
				PolizaDetFactory.generarPolizaDet(poliza,"902","AIETU04", true,importeAnticipo,"ANTICIPO "+ pag.getInfo(), pag.getOrigen().name(),pag.getSucursal().getNombre(),asiento+" "+pag.getOrigen().name());
				PolizaDetFactory.generarPolizaDet(poliza,"903","IETUA04", false,importeAnticipo,"ANTICIPO "+ pag.getInfo(), pag.getOrigen().name(),pag.getSucursal().getNombre(),asiento+" "+pag.getOrigen().name());
				
			}
		}*/
		
		Comparator<Pago> c=GlazedLists.beanPropertyComparator(Pago.class, "sucursal.id");
		GroupingList<Pago> pagosPorSucursal=new GroupingList<Pago>(pagos,c);
		
		// Aculuar la cobranza PagoConCheque y PagoConEfectivo
		for(List<Pago> lpagos:pagosPorSucursal){
			
			BigDecimal totalAplicado=BigDecimal.ZERO;
			BigDecimal importeAplicado=BigDecimal.ZERO;
			BigDecimal ivaAplicado=BigDecimal.ZERO;
			
			Pago pago=lpagos.get(0);
			for(Pago p:lpagos){
				
				if(p.isAnticipo())
				{
				continue;
				}
				
				if((p instanceof PagoConCheque) || (p instanceof PagoConEfectivo)){
					System.out.println("----------  "+p.getId());
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
			//Abono a cliente camioneta
			//PolizaDetFactory.generarPolizaDet(poliza,"106", "", false, totalAplicado, "Clientes CAM cobranza", ref1, ref2, asiento);
			//Cargo Iva en ventas por trasladar
			//PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", true, ivaAplicado, "", ref1, ref2, asiento);
			//Abono Iva en ventas
			//PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", false, ivaAplicado, "", ref1, ref2, asiento);
			
		}	
	}

}
