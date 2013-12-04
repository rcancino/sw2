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
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_TransitoXDifComp implements IProcesador {

	private boolean evaluar(Date fechaPoliza, AnalisisDeFactura analisis) {
		if(analisis.getFactura().isAnticipo() || analisis.getFactura().getAnticipo()!=null)
			return false;
		return true;
		/* YA NO EXISTE EL TRANSITO por la fecha de factura
		Date fechaFactura=analisis.getFactura().getFecha();
		return DateUtil.isSameMonth(fechaFactura, fechaPoliza);*/
	}

	public void procesar(Poliza poliza, ModelMap model) {

		EventList<AnalisisDeFactura> analisis = (EventList<AnalisisDeFactura>) model.get("analisis");
	//	String asiento = "COMPRAS TRANSITO";

		for (AnalisisDeFactura a : analisis) {
			
			Date fechaPol = poliza.getFecha();
			if(evaluar(fechaPol,a)  && !a.getFactura().isAnticipo()){
				EventList<AnalisisDeFacturaDet> source = GlazedLists.eventList(a.getPartidas());
				Comparator c = GlazedLists.beanPropertyComparator(AnalisisDeFacturaDet.class, "entrada.sucursal.id");
				final GroupingList<AnalisisDeFacturaDet> analisisPorSucursal = new GroupingList<AnalisisDeFacturaDet>(source, c);
				CXPFactura fac = a.getFactura();
				String ref1 = a.getFactura().getNombre();

				String desc2 = MessageFormat.format("Fac: {0}  {1,date, short}", a.getFactura().getDocumento(), a.getFactura().getFecha(),"Transito ");
				String desc3 = MessageFormat.format("Fac: {0}  {1,date, short}", a.getFactura().getDocumento(), a.getFactura().getFecha());
								
				
				BigDecimal diferencia=fac.getImporteMN().subtract(a.getImporteMN()).amount();
			//	diferencia=MonedasUtils.calcularTotal(diferencia);
				//Transito
				if(diferencia.doubleValue()!=0){
					String asiento = "COMPRAS TRANSITO";
					
					System.out.println("Analisis "+a.getId()+ " Dif: "+diferencia);
					for (List<AnalisisDeFacturaDet> as : analisisPorSucursal) {						
						String ref2 = as.get(0).getEntrada().getSucursal().getNombre();
						BigDecimal importeAcumulado = BigDecimal.ZERO;

						for (final AnalisisDeFacturaDet det : as) {
							importeAcumulado = importeAcumulado.add(det.getImporteMN());
						}						
						// Cargo a Inventario para
						PolizaDetFactory.generarPolizaDet(poliza, "119","INVC01", true, importeAcumulado, desc3, ref1,ref2, asiento);
					}

					if (a.isPrimerAnalisis()) {
						
						String clv="200";
						
						System.err.println("proveedor "+fac.getClave()+" "+fac.getNombre()+"fac moneda "+fac.getDocumento()+" "+fac.getMoneda());
						
						if(fac.getMoneda().equals("USD")){
							clv="201";
						}
						
						// Cargo a IVA Acumulado por analisis
						PolizaDetFactory.generarPolizaDet(poliza, "117","IVAC02", true, fac.getImpuestoMN().amount(),desc3, ref1, "TODAS", asiento);

						PolizaDetFactory.generarPolizaDet(poliza, "119","ITNS04", true, diferencia, desc2, ref1,"TODAS", asiento);
						// Abono a proveedor
						PolizaDetFactory.generarPolizaDet(poliza, clv, fac.getClave(), false, fac.getTotalMN().amount(),desc3, ref1, "TODAS", asiento+" Aqui E");

					} else {
						// abono a Transito
						PolizaDetFactory.generarPolizaDet(poliza, "119","ITNS04", false, a.getImporteMN().amount(), desc2,ref1, "TODAS", asiento);
					}
				}
				//Entrada Normal
				else{
					
					String asiento = "COMPRAS ENTRADAS";
					
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
						PolizaDetFactory.generarPolizaDet(poliza, "119","INVC01", true, importeAcumulado, desc3, ref1,ref2, asiento);
					}
					// Cargo a IVA Acumulado por analisis
					PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC02",true, fac.getImpuestoMN().amount(), desc3, ref1,"TODAS", asiento);
					// Abono a proveedor
					
					System.out.println("Fact "+fac.getDocumento()+" Prov "+fac.getNombre()+" tc "+fac.getTc()+" mon "+fac.getMoneda());
					
				//	if(fac.getTc()!=1){
					if(fac.getMoneda().toString().equals("USD")){
						PolizaDetFactory.generarPolizaDet(poliza, "201",fac.getClave(), false, fac.getTotalMN().amount(),desc3, ref1, "TODAS", asiento);
					}else{
						PolizaDetFactory.generarPolizaDet(poliza, "200",fac.getClave(), false, fac.getTotalMN().amount(),desc3, ref1, "TODAS", asiento);	
					}
					

					
				}
				

			}			
			

		}
	}

}
