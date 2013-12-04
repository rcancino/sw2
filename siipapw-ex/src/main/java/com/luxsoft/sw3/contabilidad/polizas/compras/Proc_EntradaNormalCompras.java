package com.luxsoft.sw3.contabilidad.polizas.compras;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.record.formula.ValueOperatorPtg;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.AnalisisDeFacturaDet;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_EntradaNormalCompras implements IProcesador {

	private boolean evaluarEntrada(Date fechaPoliza, AnalisisDeFactura det) {
		Date fechaFactura = det.getFactura().getFecha();
		Date fechaEntrada = fechaPoliza;

		int mesFactura = Periodo.obtenerMes(fechaFactura) + 1;
		int yearFactura = Periodo.obtenerYear(fechaFactura);
		int mesEntrada = Periodo.obtenerMes(fechaEntrada) + 1;
		int yearEntrada = Periodo.obtenerYear(fechaEntrada);

		if (yearFactura != yearEntrada)
			return false;
		return mesFactura == mesEntrada;
	}

	public void procesar(Poliza poliza, ModelMap model) {

		EventList<AnalisisDeFactura> analisis = (EventList<AnalisisDeFactura>) model
				.get("analisis");
		String asiento = "COMPRAS ENTRADA";

		for (AnalisisDeFactura a : analisis) {
			Date fechaPol = poliza.getFecha();
			if (a.getId() == 36635L) {
				System.out.println("DEBUG..");
			}
			EventList<AnalisisDeFacturaDet> source = GlazedLists.eventList(a.getPartidas());
			Comparator c = GlazedLists.beanPropertyComparator(AnalisisDeFacturaDet.class, "entrada.sucursal.id");
			final GroupingList<AnalisisDeFacturaDet> analisisPorSucursal = new GroupingList<AnalisisDeFacturaDet>(source, c);

			
			
			// Entrada por fecha
			if (evaluarEntrada(fechaPol, a)) {
				System.out.println("Analisis   "+ a.getId());
				CXPFactura fac = a.getFactura();
				String ref1 = a.getFactura().getNombre();

				String desc2 = MessageFormat.format("Entrada Fac: {0}  {1,date, short}", a.getFactura().getDocumento(), a.getFactura().getFecha());
				BigDecimal importeAcumuladoDet = BigDecimal.ZERO;

				for (AnalisisDeFacturaDet dets : a.getPartidas()) {
					BigDecimal importeAnalisis = BigDecimal.ZERO;
					
					if (a.getFactura().getProveedor().isDescuentoNota()) {
						importeAnalisis = dets.getImporteBrutoCalculadoMN().amount();
					} else {
						importeAnalisis = dets.getImporteMN();
					}
					importeAcumuladoDet = importeAcumuladoDet.add(importeAnalisis);
				}
				
				BigDecimal importeFactura = a.getFactura().getImporteMN().amount();
				BigDecimal diferencia = importeFactura.subtract(importeAcumuladoDet);

				if (diferencia.longValue() <= 5.00 && !a.getFactura().isAnticipo()) {
					System.out.println(fac.getClave() + " -- "
							+ fac.getDocumento() + " -- " + "  Importe Fac: "
							+ importeFactura + "  Importe Acu: "
							+ importeAcumuladoDet + "  Dif: " + diferencia);
					for (List<AnalisisDeFacturaDet> as : analisisPorSucursal) {
						String ref2 = as.get(0).getEntrada().getSucursal()
								.getNombre();

						BigDecimal importeAcumulado = BigDecimal.ZERO;
						// BigDecimal ivaAcumulado=BigDecimal.ZERO;

						for (final AnalisisDeFacturaDet det : as) {
							importeAcumulado = importeAcumulado.add(det
									.getImporteMN());

						}
						// ivaAcumulado=ivaAcumulado.add(MonedasUtils.calcularImpuesto(importeAcumulado));
						// Cargo a Inventario para
						importeAcumulado = PolizaUtils
								.redondear(importeAcumulado);
						PolizaDetFactory.generarPolizaDet(poliza, "119","INVC01", true, importeAcumulado, desc2, ref1,ref2, asiento);
					}
					// Cargo a IVA Acumulado por analisis
					PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC02",true, fac.getImpuestoMN().amount(), desc2, ref1,"TODAS", asiento);
					// Abono a proveedor
					PolizaDetFactory.generarPolizaDet(poliza, "200",fac.getClave(), false, fac.getTotalMN().amount(),desc2, ref1, "TODAS", asiento);

				}

			}
		}
	}

}
