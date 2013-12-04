package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.tesoreria.model.Clasificacion;

public class Proc_Transferencias implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		List<CargoAbono> transferencias=(List<CargoAbono>)model.get("transferencias");
		String asiento="TRANSFERENCIAS";
		for(CargoAbono ca:transferencias){
			BigDecimal importe=ca.getImporte().abs();			
			BigDecimal tc=ca.getTc().abs();
			importe=new BigDecimal(importe.doubleValue()*tc.doubleValue());
			
			String concepto= ca.getCuenta().getNumero().toString();
			String desc2=ca.getTraspaso().getDescripcion()+": "+ca.getTraspaso().getId()+" Ref: "+ca.getTraspaso().getReferenciaOrigen();
		//	String ref1=ca.getTraspaso().get.getBanco().getNombre();
			String ref=ca.getTraspaso().getCuentaOrigen().getBanco().getNombre();
			String ref1=ca.getTraspaso().getCuentaDestino().getBanco().getNombre();
			String ref2="OFICINAS";
			if(Clasificacion.RETIRO.name().equals(ca.getClasificacion())){
				if(ca.getCuenta().getTipo().equals(Cuenta.Clasificacion.INVERSION)){
					PolizaDetFactory.generarPolizaDet(poliza, "103",concepto,false, importe, desc2, ref1, ref2, asiento);
				}else{
					PolizaDetFactory.generarPolizaDet(poliza, "102",concepto,false, importe, desc2, ref, ref2, asiento);
				}
				
				
			}if(Clasificacion.DEPOSITO.name().equals(ca.getClasificacion())){
				
				PolizaDetFactory.generarPolizaDet(poliza, "102", concepto,true,importe, desc2, ref1, ref2, asiento);
				
			}if(Clasificacion.COMISION.name().equals(ca.getClasificacion())){
				
				PolizaDetFactory.generarPolizaDet(poliza, "600","151717",true, importe, desc2, ref, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "102",concepto,false, importe, desc2, ref, ref2, asiento);
				
				PolizaDetFactory.generarPolizaDet(poliza, "900","IETUD02",true,importe, desc2, ref, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "901","DIETU02",false,importe, desc2, ref, ref2, asiento);

				
			}if(Clasificacion.IMPUESTO_POR_TRASPASO.name().equals(ca.getClasificacion())){
				
				PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01",true, ca.getImporte().abs(), desc2, ref, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "102", concepto,false, ca.getImporte().abs(), desc2, ref, ref2, asiento);
			}
			
		}
		
	}

}
