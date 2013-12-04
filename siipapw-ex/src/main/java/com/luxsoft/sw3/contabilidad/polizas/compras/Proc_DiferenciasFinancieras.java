package com.luxsoft.sw3.contabilidad.polizas.compras;

import java.math.BigDecimal;
import java.text.MessageFormat;

import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_DiferenciasFinancieras implements IProcesador{
	
	
	public void procesar(Poliza poliza, ModelMap model) {
		EventList<CXPFactura> facturas=(EventList<CXPFactura>) model.get("diferencias");
		for(CXPFactura fac:facturas){
			BigDecimal diferencia=fac.getDiferencia();
			String asiento="DIFERENCIAS FINANCIERAS";
			if(diferencia.doubleValue()>0){
				
				String desc2=MessageFormat.format("Otros gastos por compra fac: {0}  {1,date,short}", fac.getDocumento(),fac.getFecha());
				PolizaDetFactory.generarPolizaDet(poliza, "704", "OGST01", true, diferencia.abs(), desc2, fac.getNombre(), "TODAS", asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "119", "ITNS04", false, diferencia.abs(), desc2, fac.getNombre(), "TODAS", asiento);
			}else {
				
				String desc2=MessageFormat.format("Ingresos por compra fac: {0}  {1,date,short}", fac.getDocumento(),fac.getFecha());
				PolizaDetFactory.generarPolizaDet(poliza, "702", "OING01", false, diferencia.abs(), desc2, fac.getNombre(), "TODAS", asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "119", "ITNS04", true, diferencia.abs(), desc2, fac.getNombre(), "TODAS", asiento);
			}	
		}
	}

}
