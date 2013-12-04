package com.luxsoft.sw3.contabilidad.polizas.jur;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDet;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;

import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_NotaDeCreditoBonificacionJur implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		//System.out.println("estoy en el procesador de notas de bonificacion jur");
		List<NotaDeCredito> notas=(List<NotaDeCredito>)model.get("notas");
		for(NotaDeCredito nota:notas){
			procesar(poliza,nota);
		}
		
	}

	boolean evaluar(Abono entidad,Poliza poliza) {
		return entidad instanceof NotaDeCreditoBonificacion;
	}

	void procesar(Poliza poliza, Abono entidad) {
		if(!evaluar(entidad,poliza))
			return;
		//System.out.println("Estoy en el metodo procear  sde jur ");
		NotaDeCreditoBonificacion nota=(NotaDeCreditoBonificacion)entidad;
		
	//	if (nota.getConceptos().isEmpty()){
			if(nota.getOrigen().equals(OrigenDeOperacion.JUR)){
				BigDecimal total=nota.getTotal();
				BigDecimal importe=PolizaUtils.calcularImporteDelTotal(total);
				BigDecimal iva=PolizaUtils.calcularImpuesto(importe);
				total=PolizaUtils.redondear(total);
				importe=PolizaUtils.redondear(importe);
				iva=PolizaUtils.redondear(iva);
				
				String asiento="BONIFICACION";
				String desc2=MessageFormat.format("Bonificacion: {0}  Fac: {1} Cte: {2}",nota.getFolio(),nota.getFolio(),nota.getNombre());
				
				String ref1=nota.getOrigen().name();
				String ref2=nota.getSucursal().getNombre();
				
				//Cargo a descuentos sobre ventas
				PolizaDetFactory.generarPolizaDet(poliza, "701", "PRFN02", true, importe,desc2, ref1, ref2, asiento);
				//Cargo a Iva Desc sobre ventas
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAO01", true, iva,desc2, ref1, ref2, asiento);
				//Abono a Clientes credito
				
				/*
				 * La Cuenta 106 se debe registrar por Cliente   "CPG"
				 */
				
				PolizaDetFactory.generarPolizaDet(poliza,"114", nota.getClave(), false, total,desc2, ref1, ref2, asiento);
			}
			
	//	}
		
		/*else{
		
			for(NotaDeCreditoDet det:nota.getConceptos()){
			//System.out.println("pROCESANDO NOTA : "+ det);
			//if(det.getVenta().getOrigen().equals(OrigenDeOperacion.CRE)){
			if(det.getOrigen().equals(OrigenDeOperacion.JUR)){
				BigDecimal total=det.getImporte();
				BigDecimal importe=PolizaUtils.calcularImporteDelTotal(total);
				BigDecimal iva=PolizaUtils.calcularImpuesto(importe);
				total=PolizaUtils.redondear(total);
				importe=PolizaUtils.redondear(importe);
				iva=PolizaUtils.redondear(iva);
				
				String asiento="BONIFICACION";
				String desc2=MessageFormat.format("Bonificacion: {0}  Fac: {1} Cte: {2}",nota.getFolio(),det.getDocumento(),nota.getNombre());
				
				String ref1=det.getVenta().getOrigen().name();
				String ref2=det.getVenta().getSucursal().getNombre();
				
				//Cargo a descuentos sobre ventas
				PolizaDetFactory.generarPolizaDet(poliza, "701", "PRFN02", true, importe,desc2, ref1, ref2, asiento);
				//Cargo a Iva Desc sobre ventas
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAO01", true, iva,desc2, ref1, ref2, asiento);
				//Abono a Clientes credito
				
				
				 * La Cuenta 106 se debe registrar por Cliente   "CPG"
				 
				
				PolizaDetFactory.generarPolizaDet(poliza,"114", nota.getClave(), false, total,desc2, ref1, ref2, asiento);
			}
			
			
		}
		}*/
		
		
	}

}
