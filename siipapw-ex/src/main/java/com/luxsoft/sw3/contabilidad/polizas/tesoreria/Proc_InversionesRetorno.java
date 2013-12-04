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
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;
import com.luxsoft.sw3.tesoreria.model.Clasificacion;
import com.luxsoft.sw3.tesoreria.model.Inversion;

public class Proc_InversionesRetorno implements IProcesador{


	public void procesar(Poliza poliza, ModelMap model) {
		List<CargoAbono> movimientos=(List<CargoAbono>)model.get("retornoDeinversiones");
		
		String asiento="INVERSIONES_RETORNO";
		String ref2="OFICINAS";
		
		for(CargoAbono ca:movimientos){
			if(Clasificacion.RETIRO_POR_INVERSION.name().equals(ca.getClasificacion())){
				Inversion i=(Inversion)ca.getTraspaso();
				String ref1=ca.getCuenta().getBanco().getNombre();
				
				BigDecimal importeDeLaInversion=i.getImporte();
				
				//Intereses
				PolizaDetFactory.generarPolizaDet(
						poliza
						, "103"
						,i.getCuentaDestino().getNumero().toString()
						,true
						,i.getRendimientoNeto()
						, "INTERESES NETOS  INVERSION: "+i.getId()
						, ref1, ref2, asiento);	
				
				//ISR			
				PolizaDet d2=PolizaDetFactory.generarPolizaDet(
						poliza
						, "750"
						,"IMPE02"
						,true
						,i.getImporteISR()
						, "ISR POR INVERSION: "+i.getId()
						, ref1, ref2, asiento);
				
				if(d2.getConcepto()==null){
					ConceptoContable cc=PolizaDetFactory.generarConceptoContable(
							i.getCuentaDestino().getNumero().toString()
							, i.getCuentaDestino().getDescripcion()
							, "750"
							);
					d2.setConcepto(cc);
				}
				
				//Abono a productos financieros ( ISR+INTERESES)
				PolizaDetFactory.generarPolizaDet(
						poliza
						, "701"
						,"740346"
						,false
						,i.getRendimientoNeto().add(i.getImporteISR())
						, "INTERESES BANCARIOS INVERSION: "+i.getId()
						, ref1
						, ref2
						, asiento);
				
				
				// Abono a ionversion 103
				PolizaDetFactory.generarPolizaDet(poliza, "103"
						,i.getCuentaDestino().getNumero().toString()
						,false
						,importeDeLaInversion.add(i.getRendimientoNeto())
						, "RETORNO DE INVERSION: "+i.getId()
						, ref1
						, ref2
						, asiento);
				
				// Cargo a bancos 102
				PolizaDetFactory.generarPolizaDet(poliza, "102"
						,i.getCuentaOrigen().getNumero().toString()
						,true
						,importeDeLaInversion.add(i.getRendimientoNeto())
						, "RETORNO DE INVERSION: "+i.getId()
						, ref1
						, ref2
						, asiento);
			}
		}
	}

}
