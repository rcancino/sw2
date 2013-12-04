package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.tesoreria.model.Clasificacion;
import com.luxsoft.sw3.tesoreria.model.ComisionBancaria;

public class Proc_Comisiones implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		List<ComisionBancaria> comisiones=(List<ComisionBancaria>)model.get("comisiones");
		String asiento="COMISIONES BANCARIAS";
		for(ComisionBancaria comision:comisiones){
			List<CargoAbono> movimientos=new ArrayList<CargoAbono>();
			movimientos.add(comision.getComisionId());
			if(comision.getImpuestoId()!=null){
				movimientos.add(comision.getImpuestoId());	
			}
			
			for(CargoAbono ca:movimientos){
				BigDecimal importe=ca.getImporte().abs();
				BigDecimal tc=ca.getTc().abs();
				importe=new BigDecimal(importe.doubleValue()*tc.doubleValue());
				
				String moneda=ca.getMoneda().toString();
				
				BigDecimal ietu=MonedasUtils.calcularImporteSinIva(importe);
				if(moneda.equals("USD")){
					ietu=importe;
				}
				
				
				String concepto= ca.getCuenta().getNumero().toString();
				String desc2="Comisión: "+comision.getId()+" Ref: "+comision.getReferenciaOrigen();
				String ref1=ca.getCuenta().getBanco().getNombre();
				String ref2="OFICINAS";
				if(Clasificacion.COMISION.name().equals(ca.getClasificacion())){				
					PolizaDetFactory.generarPolizaDet(poliza, "600","151717",true, importe, desc2, ref1, ref2, asiento);
					PolizaDetFactory.generarPolizaDet(poliza, "102",concepto,false, importe, desc2, ref1, ref2, asiento);				
				}if(Clasificacion.IVA_COMISION_BANCARIA.name().equals(ca.getClasificacion())){
					
					PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01",true, importe, desc2, ref1, ref2, asiento);
					PolizaDetFactory.generarPolizaDet(poliza, "102", concepto,false, importe, desc2, ref1, ref2, asiento);
				}
				
				PolizaDetFactory.generarPolizaDet(poliza, "900","IETUD02",true,ietu, desc2, ref1, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "901","DIETU02",false,ietu, desc2, ref1, ref2, asiento);
			}
			
			
		}
		
	}

}
