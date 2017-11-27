package com.luxsoft.sw3.contabilidad.polizas.ventas;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_NotaDeCreditoDevolucionMostrador implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		List<NotaDeCredito> notas=(List<NotaDeCredito>)model.get("notas");
		if(notas==null) return;
		for(NotaDeCredito nota:notas){
			procesar(poliza, nota);
		}
	}

	boolean evaluar(Abono entidad,Poliza poliza) {
		return entidad instanceof NotaDeCreditoDevolucion;
	}

	void procesar(Poliza poliza, Abono entidad) {
		System.out.println("Procesando nota dev: "+entidad);
		if(!evaluar(entidad,poliza))
			return;
		NotaDeCreditoDevolucion nota=(NotaDeCreditoDevolucion)entidad;
		
		if(nota.getOrigen().equals(OrigenDeOperacion.MOS)){
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
			PolizaDetFactory.generarPolizaDet(poliza, "405", "DVTA01", true, importe,desc2, ref1, ref2, asiento+" "+nota.getOrigen());
			//Cargo a Iva Dev sobre ventas
			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", true, iva,desc2, ref1, ref2, asiento+" "+nota.getOrigen());
			//System.out.println("*************************************************************"+nota.getId());
			if(!(nota.getPrimeraAplicacion()== null)) 
			if(DateUtils.isSameDay(nota.getPrimeraAplicacion(), poliza.getFecha()))		return;
			
		//	if(nota.getPrimeraAplicacion()==null)   return; 
				//Abono a Acreedores Diversos
				PolizaDetFactory.generarPolizaDet(poliza, "203", "DIVR01", false, total,desc2, ref1, ref2, asiento+" "+nota.getOrigen());
				
			
			
		}
		
	}

}
