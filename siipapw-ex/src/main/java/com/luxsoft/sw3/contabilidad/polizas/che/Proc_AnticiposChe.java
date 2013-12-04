package com.luxsoft.sw3.contabilidad.polizas.che;

import java.text.MessageFormat;
import java.util.List;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.cobranza.CobranzaUtils;

public class Proc_AnticiposChe implements IProcesador{
	
	public void procesar(Poliza poliza, org.springframework.ui.ModelMap model) {
		List<Pago> pagos=(List<Pago>)model.get("pagos");
		for(Pago pago:pagos){
			procesar(poliza,pago);
		}
	};

	boolean evaluar(Abono entidad,Poliza poliza) {
		
		if(CobranzaUtils.isDepositable(entidad)){
			Pago pago=(Pago)entidad;
			//System.out.println("Evaluando: "+entidad);
			return pago.isAnticipo();
		}
		return false;
	}

	void procesar(Poliza poliza, Abono entidad) {
		if(!evaluar(entidad,poliza))
			return;
		Pago pago=(Pago)entidad;
		
		
		
		String asiento="ANTICIPO";
		String desc2=MessageFormat.format("{0} {1} }", pago.getInfo(),pago.getBanco());
		String ref1=pago.getOrigenAplicacion();
		String ref2=pago.getSucursal().getNombre();
		
		PolizaDetFactory.generarPolizaDet(poliza, "102", pago.getCuenta().getNumero().toString(), true, pago.getTotal(),desc2 , ref1, ref2, asiento);
		PolizaDetFactory.registrarAnticipoDeClientes(poliza, entidad,asiento, true);
		
		PolizaDet ivaAnticipos=PolizaDetFactory.registrarIvaVentas(poliza, entidad, "IVAV05", true);
		ivaAnticipos.setDescripcion2("IVA EN ANTICIPOS");
		ivaAnticipos.setAsiento(asiento);
		
		
		//PolizaDetFactory.registrarIetuVentas(poliza, entidad, "AIETU05", "IETUA05", "CRE",asiento);
		
	}

}
