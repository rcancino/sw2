package com.luxsoft.sw3.contabilidad.polizas.cobranzacam;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoDeDiferencias;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_CobranzaPagosConDiferenciasCamioneta implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		List<Pago> pagos=(List<Pago>)model.get("pagos");
		for(Pago pago:pagos){
			if(pago instanceof PagoDeDiferencias){
				String ref1=pago.getOrigenAplicacion();
				String ref2=pago.getSucursal().getNombre();
				String asiento="COBRANZA DIFERENCIAS OI";
				
				BigDecimal totalAplicado=pago.getAplicado(poliza.getFecha());
				
				//Cargo a Otros gastos
				PolizaDetFactory.generarPolizaDet(poliza, "704", "OGST01", true, totalAplicado, "Ajuste automatico <$1", ref1, ref2, asiento);
				//Abono a clientes
				PolizaDetFactory.generarPolizaDet(poliza, "105", pago.getSucursal().getId().toString(), false, totalAplicado, "Ajuste automatico <$1", ref1, ref2, asiento);
			}
		}
	}

}
