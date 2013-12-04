package com.luxsoft.sw3.contabilidad.polizas.che;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.ChequeDevuelto;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDet;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_Cargo_Che_Dev implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		 System.out.println("Ya estoy en el procesador");
		List<ChequeDevuelto> cargos=(List<ChequeDevuelto>)model.get("chequesDev");
		for(ChequeDevuelto cargo:cargos){
			procesar(poliza,cargo);
		}
	}
	
void procesar(Poliza poliza, Cargo entidad) {
		
		ChequeDevuelto cargo=(ChequeDevuelto)entidad;
		// System.out.println("Ya estoy en el procesador");
	
			System.out.println("Procesamdp Cheque devuelto: "+ cargo);
			//if(det.getVenta().getOrigen().equals(OrigenDeOperacion.CRE)){
			
				BigDecimal total=cargo.getTotal();
				BigDecimal importe=PolizaUtils.calcularImporteDelTotal(total);
				BigDecimal iva=PolizaUtils.calcularImpuesto(importe);
				total=PolizaUtils.redondear(total);
				importe=PolizaUtils.redondear(importe);
				iva=PolizaUtils.redondear(iva);
				
				String asiento="CHEQUEDEVUELTO";
				
				String desc2=MessageFormat.format("Cargo: {0}  Fac: {1} Cte:{2}",cargo.getDocumento(),cargo.getDocumento(),cargo.getNombre());
				
				String ref1=cargo.getOrigen().name();
				String ref2=cargo.getSucursal().getNombre();
				

				//Cargo a Clientes credito
				PolizaDetFactory.generarPolizaDet(poliza,"113",cargo.getClave(), true, total,desc2, ref1, ref2, asiento);

				//Abono a Bancos
				PolizaDetFactory.generarPolizaDet(poliza,"102",cargo.getCheque().getCuenta().getNumero().toString(), false, total,desc2, ref1, ref2, asiento);
				
				//Cargo Iva en ventas por trasladar
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", false, iva, desc2, ref1, ref2, asiento);
				//Abono Iva en ventas
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", true, iva, desc2, ref1, ref2, asiento);
				
				//Cargo Iva en ventas por trasladar
				PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU03", false, importe, desc2, ref1, ref2, asiento);
				//Abono Iva en ventas
				PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA03", true, importe, desc2, ref1, ref2, asiento);
			
			
			
			
		
		
		
		
	}
	


}
