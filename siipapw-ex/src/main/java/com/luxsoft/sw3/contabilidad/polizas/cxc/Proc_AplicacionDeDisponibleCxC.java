package com.luxsoft.sw3.contabilidad.polizas.cxc;

import java.awt.PageAttributes;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_AplicacionDeDisponibleCxC implements IProcesador{
	
	
	public void procesar(Poliza poliza, ModelMap model) {
		final Date fechaPol= poliza.getFecha();
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");
		for(final Pago pago:pagos){
			if(pago.isAnticipo())
				continue;
			
			if(!DateUtils.isSameDay(poliza.getFecha(), pago.getPrimeraAplicacion())){
				
				
				
				Aplicacion pAplicacion=(Aplicacion)CollectionUtils.find(pago.getAplicaciones(), new Predicate() { 
					
					public boolean evaluate(Object object) {
						Aplicacion aa=(Aplicacion)object;
					
						return DateUtils.isSameDay( fechaPol,pago.getPrimeraAplicacion());
											
					
					}
				});
				
			//	Assert.notNull(pAplicacion,"La fecha de la primera aplicacion del pago: "+pago.getId()+ " No es correcta, no existe una aplicacion con esa fecha");
				
				
				
				String desc2=MessageFormat.format("SAF: {0} {1}",pago.getInfo(),pago.getNombre());
						
				String ref1="CRE";
				String ref2=pago.getSucursal().getNombre();
				String asiento="COBRANZA APLICACION SAF";
				
				BigDecimal totalAplicado=pago.getAplicado(poliza.getFecha());
				BigDecimal importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);
				BigDecimal ivaAplicado=PolizaUtils.calcularImpuesto(importeAplicado);
				totalAplicado=PolizaUtils.redondear(totalAplicado);
				importeAplicado=PolizaUtils.redondear(importeAplicado);
				ivaAplicado=PolizaUtils.redondear(ivaAplicado);
				
				//System.out.println("Procesando SAF: "+pago+ " Aplicado: "+importeAplicado);
				
				/*if(!pAplicacion.getDetalle().getOrigen().equals("CRE")){
					System.out.println("Detectando intercambio de cartera: "+pAplicacion);
					//PolizaDetFactory.generarPolizaDet(poliza, "203","DIVR03", true, totalAplicado, "INTERCAMBIO DE CARTERA CAM", pAplicacion.getDetalle().getOrigen(), pAplicacion.getDetalle().getSucursal(),asiento);
					//PolizaDetFactory.generarPolizaDet(poliza, "203","DIVR03", false, totalAplicado,"INTERCAMBIO DE CARTERA "+pAplicacion.getDetalle().getOrigen(), "CAM", pAplicacion.getDetalle().getSucursal(),asiento);					
				}*/
				
				List<Aplicacion> aplicaciones=pago.getAplicaciones();
				BigDecimal aplFac=BigDecimal.ZERO;
				BigDecimal aplCar=BigDecimal.ZERO;
				if(!pago.getAplicaciones().isEmpty()){
					for(Aplicacion ap:aplicaciones){
						if(ap.getCargo() instanceof NotaDeCargo && ap.getFecha().equals(fechaPol)){
							aplCar=aplCar.add(ap.getImporte());
							
						}
						if(ap.getCargo() instanceof Venta && ap.getFecha().equals(fechaPol)){
							aplFac=aplFac.add(ap.getImporte());
						}						
					}					
				}				
				BigDecimal ivaFac=MonedasUtils.calcularImpuestoDelTotal(aplFac);
				BigDecimal ivaCar=MonedasUtils.calcularImpuestoDelTotal(aplCar);
				
				//Cargo acredores diversos		
				PolizaDetFactory.generarPolizaDet(poliza, "203","DIVR02", true, importeAplicado, desc2, ref1, ref2, asiento);
				// Cargo Iva Pendiente de Trasladar  
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", true, ivaAplicado.subtract(ivaCar),desc2, ref1, ref2, asiento);
				
				//Cargo Iva en ventas por trasladar
	//			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", true, ivaFac, desc2, ref1, ref2, asiento);
				
				//Cargo Iva en Otros Ingresos por trasladar
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAO01", true, ivaCar, desc2, ref1, ref2, asiento   );
				
				//Abono a cliente credito
				PolizaDetFactory.generarPolizaDet(poliza,"106", pago.getClave(), false, totalAplicado, desc2, ref1, ref2, asiento);
				
				//Cancelacion de IETU
				//PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU06", false, importeAplicado, "ACUMULABLE IETU SAF", ref1, ref2, asiento);
				//PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA06", true, importeAplicado,"IETU ACUMULABLE SAF", ref1, ref2, asiento);
				//IETU Credito
				//PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU03", true, importeAplicado, "ACUMULABLE IETU ", ref1, ref2, asiento);
				//PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA03", false, importeAplicado,"IETU ACUMULABLE ", ref1, ref2, asiento);
			}
		
		}
		
	}
	
	
	

}
