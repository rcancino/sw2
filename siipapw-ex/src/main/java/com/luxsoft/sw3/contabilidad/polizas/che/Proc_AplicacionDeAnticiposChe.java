package com.luxsoft.sw3.contabilidad.polizas.che;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_AplicacionDeAnticiposChe implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		List<Pago> pagos=(List<Pago>)model.get("pagos");
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
				
				//System.out.println("Procesando anticipo: "+pago+ " Aplicado: "+importeAplicado);
				//Cargo a anticipos de clientes		
				PolizaDetFactory.generarPolizaDet(poliza, "204",pago.getClave(), true, importeAplicado, desc2, ref1, ref2, asiento);
				//Cargo Iva en ventas por trasladar
		//		PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV05", true, ivaAplicado, "IVA TRASLADADO ANTICIPOS", ref1, ref2, asiento);
				
				//Abono a cliente credito
				
				/*
				 * La Cuenta 106 se debe registrar por Cliente   "CPG"
				 */
				
				PolizaDetFactory.generarPolizaDet(poliza,"113", pago.getClave(), false, totalAplicado, "Clientes Aplicacion de anticipo", ref1, ref2, asiento);
				
				
				//Cargo Iva en ventas por trasladar
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", true, ivaAplicado, "IVA x TRASLADAR ANTICIPO", ref1, ref2, asiento);
				//Abono Iva en ventas
		//		PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", false, ivaAplicado, "IVA TRASLADADO ANTICIPO", ref1, ref2, asiento);
				
				//Cancelacion de IETU
				PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU05", false, importeAplicado, "ACUMULABLE IETU ANTICIPO", ref1, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA05", true, importeAplicado,"IETU ACUMULABLE ANTICIPO", ref1, ref2, asiento);
				//IETU Credito
				PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU03", true, importeAplicado, "ACUMULABLE IETU ", ref1, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA03", false, importeAplicado,"IETU ACUMULABLE ", ref1, ref2, asiento);
				
			}
		
		}
	}

}
