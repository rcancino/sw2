package com.luxsoft.sw3.contabilidad.polizas.ventas;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_AplicacionDeAnticiposMostrador implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		List<Pago> pagos=(List<Pago>)model.get("pagos");
		if(pagos==null) return;
		for(Pago pago:pagos){
			if(pago.isAnticipo()){
				
				String desc2=MessageFormat.format("Anticipo {0} de Cte: {1}",pago.getTipo(),pago.getNombre());
						
				String ref1=pago.getOrigenAplicacion();
				String ref2=pago.getSucursal().getNombre();
				String asiento="COBRANZA ANTICIPO";
				
				BigDecimal totalAplicado=pago.getAplicado(poliza.getFecha());
				BigDecimal importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);
				BigDecimal ivaAplicado=PolizaUtils.calcularImpuesto(importeAplicado);
				totalAplicado=PolizaUtils.redondear(totalAplicado);
				importeAplicado=PolizaUtils.redondear(importeAplicado);
				ivaAplicado=PolizaUtils.redondear(ivaAplicado);
				
				//Cargo a anticipos de clientes		
				PolizaDetFactory.generarPolizaDet(poliza, "204",pago.getClave(), true, importeAplicado, desc2, ref1, ref2, asiento+" "+pago.getOrigenAplicacion());
				//Cargo Iva en ventas por trasladar
	//			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV05", true, ivaAplicado, "IVA TRASLADADO ANTICIPOS", ref1, ref2, asiento+" "+pago.getOrigenAplicacion());
				
				//Abono a cliente credito
				
						
				//PolizaDetFactory.generarPolizaDet(poliza,"106", pago.getClave(), false, totalAplicado, "Clientes  aplic anticipo", ref1, ref2, asiento);
				
				
				//Cargo Iva en ventas por trasladar
	//			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", false, ivaAplicado, "IVA x TRASLADAR ANTICIPO", ref1, ref2, asiento+" "+pago.getOrigenAplicacion());
				//Abono Iva en ventas
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", true, ivaAplicado, "IVA TRASLADADO ANTICIPO", ref1, ref2, asiento+" "+pago.getOrigenAplicacion());
				
				//Cancelacion de IETU
				PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU05", false, importeAplicado, desc2, ref1, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA05", true, importeAplicado, desc2, ref1, ref2, asiento);
				
				// Correccion Para hacer el ingreso del IETU a Ventas
				PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU01", true, importeAplicado, desc2, ref1, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA01", false, importeAplicado, desc2, ref1, ref2, asiento);
				
			}
		
		}
	}

}
