package com.luxsoft.sw3.contabilidad.polizas.ventas;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.cobranza.CobranzaUtils;

public class Proc_AnticiposTarjeta implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		List<Pago> pagos=(List<Pago>)model.get("pagos");
		for(Pago pago:pagos){
			procesar(poliza, pago);
			System.out.println("Evaluando Anticipo Tar MOS: "+pago+" "+pago.isAnticipo() +" Es depositable:  " +CobranzaUtils.isDepositable(pago));
		}
				
	}

/*	private boolean evaluar(Abono entidad,Poliza poliza) {
		
		if(!CobranzaUtils.isDepositable(entidad)){
			Pago pago=(Pago)entidad;
		//	System.out.println("Evaluando Anticipo Tar MOS: "+entidad+" "+pago.isAnticipo());
			return pago.isAnticipo();
		}
		return false;
	}*/

	private void procesar(Poliza poliza, Abono entidad) {
		
		Pago pago=(Pago)entidad;
		
		if(! (pago.isAnticipo() && pago instanceof PagoConTarjeta && pago.getFecha().equals(poliza.getFecha())))
			return;
		
		
		
		
		String asiento="ANTICIPO MOS";
		String desc2=MessageFormat.format("{0} {1} ", pago.getInfo(),pago.getBanco());
		String ref1=pago.getOrigenAplicacion();
		String ref2=pago.getSucursal().getNombre();
		
		//Cargo a bancos
		//PolizaDetFactory.generarPolizaDet(poliza, "102", pago.getCuenta().getNumero().toString(), true, pago.getTotal(),desc2 , ref1, ref2, asiento);
		
		//Abono a anticipos de clientes
		PolizaDetFactory.registrarAnticipoDeClientes(poliza, entidad,asiento, true);
		
		PolizaDet ivaAnticipos=PolizaDetFactory.registrarIvaVentas(poliza, entidad, "IVAV05", true);
		ivaAnticipos.setDescripcion2("IVA EN ANTICIPOS");
		ivaAnticipos.setAsiento(asiento);
		
		
		//PolizaDetFactory.registrarIetuVentas(poliza, entidad, "AIETU05", "IETUA05", "CAM",asiento);
		
	}

}
