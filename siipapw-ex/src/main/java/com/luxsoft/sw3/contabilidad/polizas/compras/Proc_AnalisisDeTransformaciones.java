package com.luxsoft.sw3.contabilidad.polizas.compras;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;


import com.luxsoft.siipap.cxp.model.AnalisisDeTransformacion;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.model.Periodo;

import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_AnalisisDeTransformaciones implements IProcesador {

	private boolean evaluarEntrada(Date fechaPoliza, AnalisisDeTransformacion det) {
		/*Date fechaFactura = det.getCxpFactura().getFecha();
		Date fechaEntrada = fechaPoliza;

		int mesFactura = Periodo.obtenerMes(fechaFactura) + 1;
		int yearFactura = Periodo.obtenerYear(fechaFactura);
		int mesEntrada = Periodo.obtenerMes(fechaEntrada) + 1;
		int yearEntrada = Periodo.obtenerYear(fechaEntrada);

		if (yearFactura != yearEntrada)
			return false;
		return mesFactura == mesEntrada;*/
		return true;
	}
	

	public void procesar(Poliza poliza, ModelMap model) {

		EventList<AnalisisDeTransformacion> analisis = (EventList<AnalisisDeTransformacion>) model.get("analisisDeTransformaciones");
		String asiento = "TRANSFORMACIONES";
		//System.out.println("Proceando analisis de transformaciones: "+analisis.size());

		for (AnalisisDeTransformacion a : analisis) {
			Date fechaPol = poliza.getFecha();
			
			EventList<TransformacionDet> source = GlazedLists.eventList(a.getPartidas());
			Comparator c = GlazedLists.beanPropertyComparator(TransformacionDet.class, "sucursal.id");
			final GroupingList<TransformacionDet> analisisPorSucursal = new GroupingList<TransformacionDet>(source, c);
			
			CXPFactura fac = a.getCxpFactura();
			String ref1 = fac.getNombre();

			String desc2 = MessageFormat.format("TRS: Fac: {0}  {1,date, short}", fac.getDocumento(), fac.getFecha());
			
			
			
			for (List<TransformacionDet> as : analisisPorSucursal) {
				
				String ref2 = as.get(0).getSucursal().getNombre();
				BigDecimal importePorSucursal = BigDecimal.ZERO;
				
				for (final TransformacionDet det : as) {
					importePorSucursal = importePorSucursal.add(det.getImporteGasto());

				}
				importePorSucursal = PolizaUtils.redondear(importePorSucursal);
				PolizaDetFactory.generarPolizaDet(poliza, "119","INVT01", true, importePorSucursal, desc2, ref1,ref2, asiento);
			}
			
			BigDecimal dif=fac.getImporte()
						.subtract(a.getAnalizado()).abs();
			if(dif.doubleValue()>0){
				PolizaDetFactory.generarPolizaDet(poliza, "119", "ITNS04",true, dif, desc2, ref1,"TODAS", asiento);
			}
			// Cargo a IVA Acumulado por analisis
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC02",true, fac.getImpuestoMN().amount(), desc2, ref1,"TODAS", asiento);
			// Abono a proveedor
			PolizaDetFactory.generarPolizaDet(poliza, "200",fac.getClave(), false, fac.getTotalMN().amount(),desc2, ref1, "TODAS", asiento);
		
		}
	}

}
