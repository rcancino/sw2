package com.luxsoft.sw3.contabilidad.polizas.jur;

import java.math.BigDecimal;

import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoDeDiferencias;
import com.luxsoft.siipap.cxc.model.PagoEnEspecie;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_PagoEnEspecieJur implements IProcesador{
	
	
	public void procesar(Poliza poliza, ModelMap model) {
		
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");
		for(Pago pago:pagos){
			if(pago instanceof PagoEnEspecie){
				String ref1=pago.getOrigenAplicacion();
				String ref2=pago.getSucursal().getNombre();
				String asiento="COBRANZA EN ESPECIE";
				BigDecimal totalAplicado=pago.getAplicado(poliza.getFecha());
				
				//Cargo a Otros gastos
				PolizaDetFactory.generarPolizaDet(poliza, "115", pago.getClave(), true, totalAplicado, "Cuenta incobrable Pago en Especie", ref1, ref2, asiento);
				//Abono a clientes
				
				/*
				 * La Cuenta 106 se debe registrar por Cliente   "CPG"
				 */
				
				PolizaDetFactory.generarPolizaDet(poliza, "114", pago.getClave(), false, totalAplicado, "Cuenta incobrable Pago en Especie", ref1, ref2, asiento);
			}
		}	
		
	}
	
	
	

}
