package com.luxsoft.sw3.contabilidad.polizas.cobranzacam;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDet;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_NotasDeCargoCamioneta implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		 //System.out.println("Ya estoy en el procesador");
		List<NotaDeCargo> cargos=(List<NotaDeCargo>)model.get("notasDeCargo");
		for(NotaDeCargo cargo:cargos){
			procesar(poliza,cargo);
		}
	}
	
void procesar(Poliza poliza, Cargo entidad) {
		
		NotaDeCargo cargo=(NotaDeCargo)entidad;
		// System.out.println("Ya estoy en el procesador");
		//for(NotaDeCargoDet det:cargo.getConceptos()){
			System.out.println("Procesamdp nota de cargo: "+cargo);
			//if(det.getVenta().getOrigen().equals(OrigenDeOperacion.CRE)){
			//if(det.getVenta().getOrigen().equals(OrigenDeOperacion.CHE)){
			if(cargo.getOrigen().equals(OrigenDeOperacion.CAM)){
				BigDecimal total=cargo.getTotal();
				BigDecimal importe=PolizaUtils.calcularImporteDelTotal(total);
				BigDecimal iva=PolizaUtils.calcularImpuesto(importe);
				total=PolizaUtils.redondear(total);
				importe=PolizaUtils.redondear(importe);
				iva=PolizaUtils.redondear(iva);
				
				String asiento="CARGO";
				
				String desc2=MessageFormat.format("Cargo: {0}  Fac: {1} Cte:{2}",cargo.getDocumento(),cargo.getNombre());
				
				String ref1=cargo.getOrigen().name();
				String ref2=cargo.getSucursal().getNombre();
				
				//Abono Productos Financieros
				PolizaDetFactory.generarPolizaDet(poliza, "701","PRFN02", false, importe,desc2, ref1, ref2, asiento);
				//Abono Iva en Otros Ingresos Por Trasladar
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAO01", false, iva,desc2, ref1, ref2, asiento);
				
				//Cargo a Clientes credito
				PolizaDetFactory.generarPolizaDet(poliza,"105",cargo.getSucursal().getId().toString(), true, total,desc2, ref1, ref2, asiento);
			
			}
			
			
		//}
		
		
		
	}
	


}
