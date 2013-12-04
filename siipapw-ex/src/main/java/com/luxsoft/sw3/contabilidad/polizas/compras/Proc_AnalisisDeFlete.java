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

import com.luxsoft.siipap.cxp.model.CXPFactura;

import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.maquila.model.MovimientoConFlete;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;
import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;


public class Proc_AnalisisDeFlete implements IProcesador {

	private boolean evaluarEntrada(Date fechaPoliza, AnalisisDeFlete det) {
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

		EventList<AnalisisDeFlete> analisis = (EventList<AnalisisDeFlete>) model.get("analisisDeFlete");
		String asiento = "Flete facturado";
		System.out.println("Proceando analisis de flete: "+analisis.size());

		for (AnalisisDeFlete a : analisis) {
			Date fechaPol = poliza.getFecha();
			
			EventList source = GlazedLists.eventList(a.getEntradas());
			source.addAll(a.getTransformaciones());
			source.addAll(a.getTraslados());
			source.addAll(a.getComs());
			
			Comparator c = GlazedLists.beanPropertyComparator(Inventario.class, "sucursal.id");
			final GroupingList<Inventario> analisisPorSucursal = new GroupingList<Inventario>(source, c);
			
			CXPFactura fac = a.getCxpFactura();
			String ref1 = fac.getNombre();

			String desc2 = MessageFormat.format("Fac: {0}  {1,date, short}", fac.getDocumento(), fac.getFecha(),"A. FLETE:");
			
			
			
			for (List<Inventario> as : analisisPorSucursal) {
				
				String ref2 = as.get(0).getSucursal().getNombre();
				BigDecimal importePorSucursal = BigDecimal.ZERO;
				
				
				for (final Inventario det : as) {
					MovimientoConFlete ca=(MovimientoConFlete)det;
					importePorSucursal = importePorSucursal.add(new BigDecimal(ca.getCantidad()/ca.getFactor()*ca.getCostoFlete().doubleValue()));
				}
				importePorSucursal = PolizaUtils.redondear(importePorSucursal);
				PolizaDetFactory.generarPolizaDet(poliza, "119","IFLT01", true, importePorSucursal, desc2, ref1,ref2, asiento);
			}
			
			// Cargo a IVA Acumulado por analisis
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC02",true, fac.getImpuestoMN().amount().subtract(fac.getRetencionflete()), desc2, ref1,"TODAS", asiento);
			// Abono a proveedor
			PolizaDetFactory.generarPolizaDet(poliza, "200",fac.getClave(), false, fac.getTotalMN().amount(),desc2, ref1, "TODAS", asiento);
		
			PolizaDetFactory.generarPolizaDet(poliza, "205","IMPR03",false,a.getCxpFactura().getRetencionFleteMN().amount(),desc2,ref1,"TODAS", asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "119","IFLT01",true,a.getCxpFactura().getFleteMN().amount(),desc2,ref1,"TODAS", asiento);
		//	PolizaDetFactory.generarPolizaDet(poliza, "117","IVAC02",true,a.getCxpFactura().getImpuestoFleteMN().amount().subtract(a.getCxpFactura().getRetencionFleteMN().amount()),desc2,ref1,"TODAS", asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "117","IVAR02",true,a.getCxpFactura().getRetencionFleteMN().amount(),desc2,ref1,"TODAS", asiento);
		}
	}

}
