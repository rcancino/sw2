package com.luxsoft.sw3.contabilidad.polizas.compras;

import java.text.MessageFormat;

import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_FleteProveedorComp implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		EventList<AnalisisDeFactura> analisis=(EventList<AnalisisDeFactura>) model.get("analisis");
		String asiento="COMPRAS FLETE";
		
		for(AnalisisDeFactura a:analisis){
		
			String desc2=MessageFormat.format("Fac: {0}  {1,date, short}",a.getFactura().getDocumento(),a.getFactura().getFecha(),"FLETE");
			String ref1=a.getFactura().getNombre();
			
			if (a.isPrimerAnalisis()){
				
				if(a.getFactura().getFlete().longValue() > 0){
					PolizaDetFactory.generarPolizaDet(poliza, "119","IFLT01",true,a.getFactura().getFleteMN().amount(),desc2,ref1,"TODAS", asiento);
					PolizaDetFactory.generarPolizaDet(poliza, "117","IVAC02",true,a.getFactura().getImpuestoFleteMN().amount().subtract(a.getFactura().getRetencionFleteMN().amount()),desc2,ref1,"TODAS", asiento);
					PolizaDetFactory.generarPolizaDet(poliza, "117","IVAR02",true,a.getFactura().getRetencionFleteMN().amount(),desc2,ref1,"TODAS", asiento);
					PolizaDetFactory.generarPolizaDet(poliza, "205","IMPR03",false,a.getFactura().getRetencionFleteMN().amount(),desc2,ref1,"TODAS", asiento);
					
				}
			}
		}
			
	}
		
		
		
	

}
