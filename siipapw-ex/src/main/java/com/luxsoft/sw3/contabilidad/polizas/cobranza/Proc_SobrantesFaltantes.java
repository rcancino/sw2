package com.luxsoft.sw3.contabilidad.polizas.cobranza;

import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.tesoreria.model.CorreccionDeFicha;

public class Proc_SobrantesFaltantes  implements IProcesador{
	
	final String asiento;
	
	public Proc_SobrantesFaltantes(String asiento) {
		this.asiento = asiento;
	}

	public void procesar(Poliza poliza, ModelMap model) {
		
		
		List<CorreccionDeFicha> correcciones=(List<CorreccionDeFicha>)model.get("correcciones");
		for(CorreccionDeFicha correccion:correcciones){
			
			String desc=" ficha: "+correccion.getFicha().getFolio()+" "+correccion.getConcepto().getDescripcion();
			
			if(correccion.getTipo().equals(correccion.getTipo().FALTANTE_CORRECCION_FICHA)){
			PolizaDetFactory.generarPolizaDet(poliza,"109", "198838",true,correccion.getDiferencia().abs()
					                           , "Faltante: "+desc, correccion.getFicha().getOrigen().toString()
					                           ,correccion.getFicha().getSucursal().getNombre(), asiento);
			}
			if(correccion.getTipo().equals(correccion.getTipo().FALTANTE_POR_OPERACION)){
				PolizaDetFactory.generarPolizaDet(poliza,"109", "198838",true,correccion.getDiferencia().abs()
                        , "Faltante "+desc, correccion.getFicha().getOrigen().toString()
                        ,correccion.getFicha().getSucursal().getNombre(), asiento);
			}
			if(correccion.getTipo().equals(correccion.getTipo().SOBRANTE_NO_IDENTIFICADO)){
				PolizaDetFactory.generarPolizaDet(poliza,"109", "198838",false,correccion.getDiferencia().abs()
                        , "Sobrante "+desc, correccion.getFicha().getOrigen().toString()
                        ,correccion.getFicha().getSucursal().getNombre(), asiento);
			}
			if(correccion.getTipo().equals(correccion.getTipo().SOBRANTE_POR_COBRANZA)){
				PolizaDetFactory.generarPolizaDet(poliza,"109", "198838",false,correccion.getDiferencia().abs()
                        , "Sobrante "+desc, correccion.getFicha().getOrigen().toString()
                        ,correccion.getFicha().getSucursal().getNombre(), asiento);
			}
			
			//String desc2=MessageFormat.format("Ficha - {0}"+" Folio: ");
			//PolizaDetFactory.generarPolizaDet(poliza,"102",ficha.getCuenta().getNumero().toString(), true, ficha.getTotal(), desc2, ficha.getOrigen().name(), ficha.getSucursal().getNombre(),asiento);
		
		
		}
		
	
	}

}
