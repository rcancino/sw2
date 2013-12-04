package com.luxsoft.sw3.contabilidad.polizas.cobranzacam;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_InteresesPrestamoCamioneta implements IProcesador {

	public void procesar(Poliza poliza, ModelMap model) {
		
		List<NotaDeCargo> cargos = (List<NotaDeCargo>) model
				.get("notasDeCargo");
		BigDecimal ivaAcumulado=BigDecimal.ZERO;
		String asiento = "CARGO_CHOFER";
		
		for (NotaDeCargo cargo : cargos) {
			
			BigDecimal iv=procesar(poliza, cargo,asiento);
			ivaAcumulado=ivaAcumulado.add(iv);
		}
		System.out.println("Iva Acumulado: "+ivaAcumulado);
		PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", false,ivaAcumulado, "Intereses por prestamo a choferes", "", "", asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", true,ivaAcumulado, "Intereses por prestamo a choferes", "", "", asiento);
	}

	BigDecimal procesar(Poliza poliza, Cargo entidad,String asiento) {

		NotaDeCargo cargo = (NotaDeCargo) entidad;
		//System.out.println("Procesamdp nota de cargo: " + cargo);
		if (cargo.getOrigen().equals(OrigenDeOperacion.CAM)) {
			BigDecimal total = cargo.getTotal();
			BigDecimal importe = PolizaUtils.calcularImporteDelTotal(total);
			BigDecimal iva = PolizaUtils.calcularImpuesto(importe);
			total = PolizaUtils.redondear(total);
			importe = PolizaUtils.redondear(importe);
			iva = PolizaUtils.redondear(iva);

			

			String desc2 = MessageFormat.format("Cargo: {0}  {1} ",
					cargo.getDocumento(), cargo.getNombre());

			String ref1 = cargo.getNombre();
			String ref2 = cargo.getSucursal().getNombre();

			// Abono Productos Financieros
			PolizaDetFactory.generarPolizaDet(poliza, "701", "PRFN05", false,
					importe, desc2, ref1, ref2, asiento);
			// Abono Iva en Otros Ingresos Por Trasladar
			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", false,
					iva, desc2, ref1, ref2, asiento);

			// Cargo a Clientes credito
			PolizaDetFactory.generarPolizaDet(poliza, "109", "740379", true, total, desc2,ref1, ref2, asiento);
			
			//ivaAcumulado=ivaAcumulado.add(iva);
			return iva;

		}
		
		return BigDecimal.ZERO;

	}

}
