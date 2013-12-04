package com.luxsoft.sw3.contabilidad.polizas.jur;

import java.math.BigDecimal;
import java.text.MessageFormat;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_AplicacionDePagoTarjetaJur implements IProcesador{
	
	
	public void procesar(Poliza poliza, ModelMap model) {
		
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");
		for(final Pago pago:pagos){
			if(pago.isAnticipo())
				continue;
			if(!(pago instanceof PagoConTarjeta))
				continue;
			//PagoConTarjeta pt=(PagoConTarjeta)pago;
			if(DateUtils.isSameDay(poliza.getFecha(), pago.getPrimeraAplicacion())){
				
				Aplicacion pAplicacion=(Aplicacion)CollectionUtils.find(pago.getAplicaciones()
						, new Predicate() {					
					public boolean evaluate(Object object) {
						Aplicacion aa=(Aplicacion)object;
						return DateUtils.isSameDay(aa.getFecha(), pago.getPrimeraAplicacion());
					}
				});
				
				Assert.notNull(pAplicacion,"La fecha de la primera aplicacion del pago: "+pago.getId()+ " No es correcta, no existe una aplicacion con esa fecha");
				
				
				
				String desc2=MessageFormat.format("{0} de Cte: {1}",pago.getTipo(),pago.getNombre());
						
				String ref1=pago.getOrigenAplicacion();
				String ref2=pago.getSucursal().getNombre();
				String asiento="COBRANZA TARJETA";
				
				BigDecimal totalAplicado=pago.getAplicado(poliza.getFecha());
				BigDecimal importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);
				BigDecimal ivaAplicado=PolizaUtils.calcularImpuesto(importeAplicado);
				totalAplicado=PolizaUtils.redondear(totalAplicado);
				importeAplicado=PolizaUtils.redondear(importeAplicado);
				ivaAplicado=PolizaUtils.redondear(ivaAplicado);
				
				//Cargo acredores diversos		
				PolizaDetFactory.generarPolizaDet(poliza, "203","DIVR03", true, totalAplicado, desc2, ref1, ref2, asiento);
				//Abono Clientes
				PolizaDetFactory.generarPolizaDet(poliza,"114", pago.getClave(), false, totalAplicado, "Aplic cobranza JUR", ref1, ref2, asiento);
				
				//Cargo Iva en ventas por trasladar
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", true, ivaAplicado, desc2, ref1, ref2, asiento);
				//Abono Iva en ventas
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", false, ivaAplicado, desc2, ref1, ref2, asiento);
				
				//IETU Juridico
				PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU03", true, importeAplicado, "ACUMULABLE IETU ", ref1, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA03", false, importeAplicado,"IETU ACUMULABLE ", ref1, ref2, asiento);
			
				
			}
		
		}
		
	}
	
	
	

}
