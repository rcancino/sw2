package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.tesoreria.model.Clasificacion;
import com.luxsoft.sw3.tesoreria.model.Inversion;

public class Proc_Inversiones implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		
		List<Inversion> inversiones=(List<Inversion>)model.get("inversiones");
		for(Inversion i:inversiones){
			
			String desc2="Inv: "+i.getId();			
			
			String asiento="INVERSIONES";
			String ref2="OFICINAS";
			
			CargoAbono retiro=i.buscarMovimiento(Clasificacion.RETIRO);
			final BigDecimal importeDeLaInversion=retiro.getImporte().abs().multiply(retiro.getTc());
			
			PolizaDetFactory.generarPolizaDet(
					poliza
					, "102"
					,i.getCuentaOrigen().getNumero().toString(),false
					,importeDeLaInversion
					, desc2
					, i.getCuentaOrigen().getBanco().getNombre()
					, ref2
					, asiento);
			
			//Abono  a la inversion  103
			PolizaDet d1=PolizaDetFactory.generarPolizaDet(poliza
					, "103"
					,i.getCuentaDestino().getNumero().toString()
					,true
					,importeDeLaInversion
					, desc2
					, i.getCuentaDestino().getBanco().getNombre()
					, ref2, asiento);
			if(d1.getConcepto()==null){
				ConceptoContable cc=PolizaDetFactory.generarConceptoContable(
						 i.getCuentaDestino().getNumero().toString()
						, i.getCuentaDestino().getDescripcion()
						,"103"
						);
				d1.setConcepto(cc);
			}
		}
	}

	/*
	public void procesar(Poliza poliza, ModelMap model) {
		List<Inversion> inversiones=(List<Inversion>)model.get("inversiones");
		
		String asiento="INVERSIONES";
		
		BigDecimal importeDeLaInversion=BigDecimal.ZERO;
		BigDecimal inversionConIntereses=BigDecimal.ZERO;
		
		for(Inversion i:inversiones){
			String ref1=i.getId().toString();
			
			for(CargoAbono ca:i.getMovimientos()){
				BigDecimal importe=ca.getImporte().abs();
				String concepto= ca.getCuenta().getNumero().toString();
				String desc2=ca.getTraspaso().getDescripcion();
				
				String ref2="OFICINAS";
				
				if(Clasificacion.RETIRO.name().equals(ca.getClasificacion())){
					importeDeLaInversion=ca.getImporte().abs();
					PolizaDetFactory.generarPolizaDet(poliza, "102",concepto,false, importe.abs(), desc2, ref1, ref2, asiento);
					
				}if(Clasificacion.DEPOSITO.name().equals(ca.getClasificacion())){
					inversionConIntereses=importe;
					//PolizaDetFactory.generarPolizaDet(poliza, "103", importe,false,importe, desc2, ref1, ref2, asiento);					
				}if(Clasificacion.INTERESES.name().equals(ca.getClasificacion())){					
					//PolizaDetFactory.generarPolizaDet(poliza, "701",concepto,false, importe, desc2, ref1, ref2, asiento);					
				}				
			}
			
			//Abono  a la inversion  103
			PolizaDet d1=PolizaDetFactory.generarPolizaDet(poliza, "103",i.getCuentaDestino().getNumero().toString(),true,importeDeLaInversion, "INVERSION", ref1, "OFICINAS", asiento);
			if(d1.getConcepto()==null){
				ConceptoContable cc=PolizaDetFactory.generarConceptoContable(
						 i.getCuentaDestino().getNumero().toString()
						, i.getCuentaDestino().getDescripcion()
						,"103"
						);
				d1.setConcepto(cc);
			}
			
			//Intereses
			BigDecimal intereses=inversionConIntereses.subtract(importeDeLaInversion);			
			PolizaDetFactory.generarPolizaDet(poliza, "103",i.getCuentaDestino().getNumero().toString()
					,true, intereses, "INTERESES NETOS (INVERSION)", ref1, "OFICINAS", asiento);
			
			//ISR			
			PolizaDet d2=PolizaDetFactory.generarPolizaDet(poliza, "750",i.getCuentaDestino().getNumero().toString()
					,true,i.getImporteISR(), "IMPUESTOS RETENIDOS (ISR POR INVERSION)", ref1, "OFICINAS", asiento);
			if(d2.getConcepto()==null){
				ConceptoContable cc=PolizaDetFactory.generarConceptoContable(
						i.getCuentaDestino().getNumero().toString()
						, i.getCuentaDestino().getDescripcion()
						, "750"
						);
				d2.setConcepto(cc);
			}
			
			//Abono a productos financieros ( ISR+INTERESES)			
			BigDecimal productoFinanciero=intereses.add(i.getImporteISR());
			productoFinanciero=PolizaUtils.redondear(productoFinanciero);
			PolizaDet d3=PolizaDetFactory.generarPolizaDet(poliza, "701",i.getCuentaDestino().getNumero().toString()
					,false,productoFinanciero
					, "INTERESES BANCARIOS", ref1, "OFICINAS", asiento);
			if(d3.getConcepto()==null){
				ConceptoContable cc=PolizaDetFactory.generarConceptoContable(
						i.getCuentaDestino().getNumero().toString()
						, i.getCuentaDestino().getDescripcion()
						, "103");
				d3.setConcepto(cc);
			}
			
			// Retorno de la inversion
			for(CargoAbono ca:i.getMovimientos()){
				BigDecimal importe=ca.getImporte().abs();
				String concepto= ca.getCuenta().getNumero().toString();
				String desc2=ca.getTraspaso().getDescripcion();
				
				String ref2="OFICINAS";
				if(Clasificacion.RETIRO_POR_INVERSION.name().equals(ca.getClasificacion())){
					PolizaDetFactory.generarPolizaDet(poliza, "103",concepto,false, importe, desc2, ref1, ref2, asiento);
				}if(Clasificacion.DEPOSITO_POR_INVERSION.name().equals(ca.getClasificacion())){
					PolizaDetFactory.generarPolizaDet(poliza, "102", concepto,true,importe, desc2, ref1, ref2, asiento);					
				}
			}
		}
		
		
	}*/

}
