package com.luxsoft.sw3.contabilidad.polizas.cobranzacam;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_AplicacionDeDisponiblesCamioneta implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		List<Pago> pagos=(List<Pago>)model.get("pagos");
		for(final Pago pago:pagos){
			if(pago.isAnticipo())
				continue;
			if(!DateUtils.isSameDay(poliza.getFecha(), pago.getPrimeraAplicacion())){	
				for(Aplicacion a:pago.getAplicaciones()){
					if(DateUtils.isSameDay(a.getFecha(),poliza.getFecha())  && a.getCargo().getOrigen().equals(OrigenDeOperacion.CAM) ){
						String desc2=MessageFormat.format("SAF: {0} {1}",pago.getInfo(),pago.getNombre());					
						String ref1=a.getDetalle().getOrigen();
						String ref2=a.getDetalle().getSucursal();
						String asiento="COBRANZA APLICACION SAF";
						
						BigDecimal totalAplicado=a.getImporte();
						BigDecimal importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);
						BigDecimal ivaAplicado=PolizaUtils.calcularImpuesto(importeAplicado);
						totalAplicado=PolizaUtils.redondear(totalAplicado);
						importeAplicado=PolizaUtils.redondear(importeAplicado);
						ivaAplicado=PolizaUtils.redondear(ivaAplicado);
						
						//Cargo acredores diversos		
						PolizaDetFactory.generarPolizaDet(poliza, "203","DIVR03", true, importeAplicado, desc2, ref1, ref2, asiento);
						PolizaDetFactory.generarPolizaDet(poliza, "206","IVAV02", true, ivaAplicado, desc2, ref1, ref2, asiento);
						
						
						//Abono a cliente camioneta
						PolizaDetFactory.generarPolizaDet(poliza,"105", a.getCargo().getSucursal().getId().toString(), false, totalAplicado,desc2, ref1, ref2, asiento);
						
					}
				}
			}
		
		}
	}

}
