package com.luxsoft.sw3.contabilidad.polizas.cobranza;

import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

/**
 * 
 * @author RUBEN
 * 
 *
 */ 
public class Proc_AjustesAutomaticosDeDisponibles implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		List<Pago> pagos=(List<Pago>)model.get("pagos");
		for(Pago pago:pagos){
			if(pago.isAnticipo())
				continue;
			if(pago.getDirefenciaFecha()==null)
				continue;
			if(!DateUtils.isSameDay(poliza.getFecha(), pago.getPrimeraAplicacion())){
				if(DateUtils.isSameDay(poliza.getFecha(), pago.getDirefenciaFecha())){
					String ref1=pago.getOrigenAplicacion();
					String ref2=pago.getSucursal().getNombre();
					String asiento="COBRANZA DIFERENCIAS OG";
					String sufix="01";
					if(ref1.equals("MOS"))
						sufix="01";
					if(ref1.equals("CAM"))
						sufix="03";
					if(ref1.equals("CRE"))
						sufix="02";
					if(ref1.equals("CHE"))
						sufix="04";
					//Abono a Otros ingresos
					PolizaDetFactory.generarPolizaDet(poliza, "702", "OING"+sufix, false, pago.getDiferencia(),"AJUSTE < $10 X ", ref1, ref2, asiento);
					// Cargo a Acredores diversos
					PolizaDetFactory.generarPolizaDet(poliza, "203", "DIVR"+sufix, true, pago.getDiferencia(),"AJUSTE < $10 X", ref1, ref2, asiento);
				}
			}
			
		}
	}

}
