package com.luxsoft.sw3.contabilidad.polizas.jur;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.Juridico;
import com.luxsoft.siipap.cxc.model.Juridico;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDet;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_Traspaso_Jur implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		 System.out.println("Ya estoy en el procesador De Juridico");
		List<Juridico> traspasos=(List<Juridico>)model.get("traspasosJur");
		for(Juridico traspaso:traspasos){
			procesar(poliza,traspaso);
		}
	}
	
void procesar(Poliza poliza, Juridico entidad) {
		
		Juridico traspaso=(Juridico)entidad;
		// System.out.println("Ya estoy en el procesador");
	
			System.out.println("Procesando Traspaso a Juridico: "+ traspaso);
			//if(det.getVenta().getOrigen().equals(OrigenDeOperacion.CRE)){
			
				BigDecimal traspasoSaldo=traspaso.getSaldoDocumento();
				BigDecimal total=traspaso.getTotal();
				BigDecimal importe=PolizaUtils.calcularImporteDelTotal(total);
				BigDecimal iva=PolizaUtils.calcularImpuesto(importe);
				total=PolizaUtils.redondear(total);
				importe=PolizaUtils.redondear(importe);
				iva=PolizaUtils.redondear(iva);
				
				String asiento="TRASPASO JURIDICO";
				
				String desc2=MessageFormat.format("Cargo: {0}  Fac: {1} Cte:{2}",traspaso.getDocumento(),traspaso.getCargo().getOrigen(),traspaso.getNombre());
				
				String ref1=traspaso.getCargo().getOrigen().name();
				String ref2=traspaso.getCargo().getSucursal().getNombre();
				

				//Cargo a Clientes credito
				PolizaDetFactory.generarPolizaDet(poliza,"114",traspaso.getClave(), true, traspasoSaldo,desc2, ref1, ref2, asiento);
				
				if(traspaso.getCargo().getOrigen().toString().equals("CHE")){
					
					PolizaDetFactory.generarPolizaDet(poliza,"113",traspaso.getClave(), false, traspasoSaldo,desc2, ref1, ref2, asiento);
				}else{
					PolizaDetFactory.generarPolizaDet(poliza,"106",traspaso.getClave(), false, traspasoSaldo,desc2, ref1, ref2, asiento);	
				}
				
				
				
			
			
			
			
		
		
		
		
	}
	


}
