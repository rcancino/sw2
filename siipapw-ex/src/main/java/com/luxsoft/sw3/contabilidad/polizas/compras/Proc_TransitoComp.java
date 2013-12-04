package com.luxsoft.sw3.contabilidad.polizas.compras;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.AnalisisDeFacturaDet;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_TransitoComp implements IProcesador{
	
	
	private boolean evaluar(Date fechaPoliza,AnalisisDeFactura det){
		Date fechaFactura=det.getFactura().getFecha();
		Date fechaEntrada=fechaPoliza;
		
		int mesFactura=Periodo.obtenerMes(fechaFactura)+1;
		int yearFactura=Periodo.obtenerYear(fechaFactura);
		int mesEntrada=Periodo.obtenerMes(fechaEntrada)+1;
		int yearEntrada=Periodo.obtenerYear(fechaEntrada);
		
		if(yearFactura!=yearEntrada)
			return true;
		/*if(yearFactura<yearEntrada)
			mesFactura=-1;*/
		
		return mesFactura!=mesEntrada;
	}
	
	private String evaluarTransito(Date fechaPoliza,AnalisisDeFactura det){
		
		Date fechaFactura=det.getFactura().getFecha();
		Date fechaEntrada=fechaPoliza;
		
		int mesFactura=Periodo.obtenerMes(fechaFactura)+1;
		int yearFactura=Periodo.obtenerYear(fechaFactura);
		int mesEntrada=Periodo.obtenerMes(fechaEntrada)+1;
		int yearEntrada=Periodo.obtenerYear(fechaEntrada);
		
		
		
		if(yearFactura<yearEntrada)
			return "Trans Anterior"; 
			
		if(yearFactura>yearEntrada)
			return "Trans Posterior";
		
		if( mesFactura<mesEntrada)
			return "Trans Anterior";
		
		if( mesFactura>mesEntrada)
			return "Trans Posterior";
		
		//return mesFactura!=mesEntrada;
		return "";
		
	}
	
	
	public void procesar(Poliza poliza, ModelMap model) {
		
		EventList<AnalisisDeFactura> analisis=(EventList<AnalisisDeFactura>)model.get("analisis");
		String asiento="COMPRAS TRANSITO";
		
		for(AnalisisDeFactura a:analisis){
			Date fechaPol=poliza.getFecha();
			
			
			EventList<AnalisisDeFacturaDet> source=GlazedLists.eventList(a.getPartidas());
			Comparator c=GlazedLists.beanPropertyComparator(AnalisisDeFacturaDet.class, "entrada.sucursal.id");
			final  GroupingList<AnalisisDeFacturaDet> analisisPorSucursal=new GroupingList<AnalisisDeFacturaDet>(source,c);
			
			
			// Transito por fecha
			if (evaluar(fechaPol,a)){
			CXPFactura fac=a.getFactura();
			String ref1=a.getFactura().getNombre();
			
			String tipo= evaluarTransito(fechaPol,a);
			String desc2=MessageFormat.format(tipo +" Fac: {0}  {1,date, short}",a.getFactura().getDocumento(),a.getFactura().getFecha());
						
			
			//BigDecimal ivaAcumulado=BigDecimal.ZERO;
			
			for(List<AnalisisDeFacturaDet> as:analisisPorSucursal){
				String ref2=as.get(0).getEntrada().getSucursal().getNombre();
				BigDecimal importeAcumulado=BigDecimal.ZERO;
				
				for(final AnalisisDeFacturaDet det:as){
	
						importeAcumulado=importeAcumulado.add(det.getImporteMN());

					
				}
				//ivaAcumulado=ivaAcumulado.add(MonedasUtils.calcularImpuesto(importeAcumulado));
				//Cargo a Inventario para 
				PolizaDetFactory.generarPolizaDet(poliza, "119","ITNS04", true, importeAcumulado,desc2,ref1,ref2, asiento);
				
				
			}			
			//Cargo a IVA Acumulado por analisis
			PolizaDetFactory.generarPolizaDet(poliza, "117","IVAC02", true,fac.getImpuestoMN().amount(),desc2,ref1,"TODAS", asiento);
			
			//Abono a proveedor
			PolizaDetFactory.generarPolizaDet(poliza, "200",fac.getClave(), false,fac.getTotalMN().amount(),desc2
					,ref1,"TODAS"
					, asiento);
			
			
		}
			
			
			
	}
	}

}
