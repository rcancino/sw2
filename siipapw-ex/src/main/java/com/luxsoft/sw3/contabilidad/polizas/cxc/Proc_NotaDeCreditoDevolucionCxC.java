package com.luxsoft.sw3.contabilidad.polizas.cxc;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_NotaDeCreditoDevolucionCxC implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		List<NotaDeCredito> notas=(List<NotaDeCredito>)model.get("notas");
		for(NotaDeCredito nota:notas){
			procesar(poliza,nota);
		}
	}

	boolean evaluar(Abono entidad,Poliza poliza) {
		return entidad instanceof NotaDeCreditoDevolucion;
	}

	void procesar(Poliza poliza, Abono entidad) {
		if(!evaluar(entidad,poliza))
			return;
		NotaDeCreditoDevolucion nota=(NotaDeCreditoDevolucion)entidad;
		Devolucion dev=nota.getDevolucion();
		
		BigDecimal total=nota.getTotal();
		BigDecimal importe=PolizaUtils.calcularImporteDelTotal(total);
		BigDecimal iva=PolizaUtils.calcularImpuesto(importe);
		total=PolizaUtils.redondear(total);
		importe=PolizaUtils.redondear(importe);
		iva=PolizaUtils.redondear(iva);
		
		String asiento="DEVOLUCIONES";
		String desc2=MessageFormat.format("Devolucion: {0}  Fac: {1}",nota.getFolio(),dev.getVenta().getDocumento());
		String ref1=dev.getVenta().getOrigen().name();
		String ref2=dev.getVenta().getSucursal().getNombre();
		//Cargo a devoluciones sobre ventas
		PolizaDetFactory.generarPolizaDet(poliza, "405", "DVTA03", true, importe,desc2, ref1, ref2, asiento);
		//Cargo a Iva Dev sobre ventas
		PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV03", true, iva,desc2, ref1, ref2, asiento);
		//Abono a Clientes camioneta
		
		/*
		 * La Cuenta 106 se debe registrar por Cliente   "CPG"
		 */
		
		PolizaDetFactory.generarPolizaDet(poliza,"106", nota.getClave(), false, total,desc2, ref1, ref2, asiento);
		
	}

}
