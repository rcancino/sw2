package com.luxsoft.sw3.contabilidad.polizas.che;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;

import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_NotaDeCreditoBonificacionChe implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
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
		NotaDeCreditoBonificacion nota=(NotaDeCreditoBonificacion)entidad;
		
			if(nota.getOrigen().equals(OrigenDeOperacion.CHE)){	
				BigDecimal total=nota.getTotal();
				BigDecimal importe=nota.getImporte();
				BigDecimal iva=nota.getImpuesto();
			
				String asiento="BONIFICACION";
				String desc2=MessageFormat.format("Bonificacion: {0}  Cte: {1}",nota.getFolio(),nota.getNombre());
				
				String ref1=nota.getOrigen().name();
				String ref2=nota.getSucursal().getNombre();
				
				//Cargo a descuentos sobre ventas
				PolizaDetFactory.generarPolizaDet(poliza, "702", "OING04", true, importe,desc2, ref1, ref2, asiento);
				//Cargo a Iva Desc sobre ventas
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAO01", true, iva,desc2, ref1, ref2, asiento);
				//Abono a Clientes credito
				
				PolizaDetFactory.generarPolizaDet(poliza,"113", nota.getClave(), false, total,desc2, ref1, ref2, asiento);
			}
			
			
//		}
		
		
		
	}

}
