package com.luxsoft.sw3.contabilidad.polizas.compras;

import java.text.MessageFormat;

import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxp.model.CXPCargoAbono;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_AnticipoComp implements IProcesador{
	
	

	
	
	public void procesar(Poliza poliza, ModelMap model) {
		EventList<CXPFactura> anticipos=(EventList<CXPFactura>) model.get("anticipos");
		String asiento="COMPRAS ANTICIPO";
		String ref2="TODAS";
		
		for(CXPFactura ant:anticipos){
		
			 if (ant instanceof CXPCargoAbono){
				 String ref1= ant.getNombre();
			
				 //System.out.println("ANTICIPO: "+ant.getDocumento()+" F: "+ant.getFecha()+" Imp: "+ant.getImporteMN() );
			
				 String desc2=MessageFormat.format("ANTICIPO Fac : {0}  {1,date,short}", ant.getDocumento(),ant.getFecha());
			
			
				 //Cargo a Inventario para 
				 PolizaDetFactory.generarPolizaDet(poliza, "111","ANTP02", true, ant.getImporteMN().amount(),desc2,ref1,ref2, asiento);
				 //	Cargo a IVA Acumulado por analisis
				 PolizaDetFactory.generarPolizaDet(poliza, "117","IVAC02", true, ant.getImpuestoMN().amount(),desc2,ref1,ref2, asiento);
				 //Abono a proveedor
				 PolizaDetFactory.generarPolizaDet(poliza, "200",ant.getClave(), false,ant.getTotalMN().amount(),desc2,ref1,ref2, asiento);
			 }
		}
	}
	


}