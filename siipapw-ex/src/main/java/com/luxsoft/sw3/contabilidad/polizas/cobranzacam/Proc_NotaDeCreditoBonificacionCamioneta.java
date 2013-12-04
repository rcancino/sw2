package com.luxsoft.sw3.contabilidad.polizas.cobranzacam;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
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
import com.luxsoft.sw3.contabilidad.polizas.Procesador;

public class Proc_NotaDeCreditoBonificacionCamioneta implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		List<NotaDeCredito> notas=(List<NotaDeCredito>)model.get("notas");
		for(NotaDeCredito nota:notas){
			procesar(poliza, nota);
		}
	}

	private boolean evaluar(Abono entidad,Poliza poliza) {
		return entidad instanceof NotaDeCreditoBonificacion;
	}

	private void procesar(Poliza poliza, Abono entidad) {
		if(!evaluar(entidad,poliza))
			return;
		NotaDeCreditoBonificacion nota=(NotaDeCreditoBonificacion)entidad;
		
	//	for(NotaDeCreditoDet det:nota.getConceptos()){
			
			//if(det.getVenta().getOrigen().equals(OrigenDeOperacion.CAM)){
			//if(det.getOrigen().equals("CAM")){
			 if(nota.getOrigen().equals(OrigenDeOperacion.CAM))
			 {
				BigDecimal total=nota.getTotal();
				BigDecimal importe=PolizaUtils.calcularImporteDelTotal(total);
				BigDecimal iva=PolizaUtils.calcularImpuesto(importe);
				total=PolizaUtils.redondear(total);
				importe=PolizaUtils.redondear(importe);
				iva=PolizaUtils.redondear(iva);
				
				String asiento="BONIFICACION";
				String desc2=MessageFormat.format("Bonificacion: {0}  ",nota.getFolio());
				
				String ref1=nota.getOrigen().toString();
				String ref2=nota.getSucursal().getNombre();
				
				//Cargo a descuentos sobre ventas
				PolizaDetFactory.generarPolizaDet(poliza, "406", "BVTA02", true, importe,desc2, ref1, ref2, asiento);
				//Cargo a Iva Desc sobre ventas
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV04", true, iva,desc2, ref1, ref2, asiento);
				//Abono a Clientes camioneta
				PolizaDetFactory.generarPolizaDet(poliza,"105", nota.getSucursal().getId().toString(), false, total,desc2, ref1, ref2, asiento);
		///	}
			
			
		}
		
		
		
	}

}
