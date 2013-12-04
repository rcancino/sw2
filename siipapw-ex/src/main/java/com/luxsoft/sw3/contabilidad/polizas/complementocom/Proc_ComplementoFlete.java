package com.luxsoft.sw3.contabilidad.polizas.complementocom;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.cxp.model.CXPFactura;

import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.maquila.model.MovimientoConFlete;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;
import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;


public class Proc_ComplementoFlete implements IProcesador {

	

	public void procesar(Poliza poliza, ModelMap model) {

		EventList<AnalisisDeFlete> analisis = (EventList<AnalisisDeFlete>) model.get("analisisDeFlete");
	

		for (AnalisisDeFlete a : analisis) {
			
			Date fechaPol = poliza.getFecha();
			fechaPol=DateUtils.truncate(fechaPol, Calendar.DATE);
			if(true){
				
				EventList source = GlazedLists.eventList(a.getEntradas());
				source.addAll(a.getTransformaciones());
				source.addAll(a.getTraslados());
				source.addAll(a.getComs());
				
				EventList target = new BasicEventList(0);
				for(Inventario i:a.getEntradas()){
					if(DateUtils.isSameDay(i.getFecha(), fechaPol))
						target.add(i);
				}				
                 for(Inventario i:a.getTraslados()){
					
					if(DateUtils.isSameDay(i.getFecha(), fechaPol))
						target.add(i);
				}
				for(Inventario i:a.getTransformaciones()){
					
				
					if(DateUtils.isSameDay(i.getFecha(), fechaPol))
						target.add(i);
				}
				for(Inventario i:a.getComs()){
					
				
					if(DateUtils.isSameDay(i.getFecha(), fechaPol))
						target.add(i);
				}
				
				Comparator c = GlazedLists.beanPropertyComparator(Inventario.class, "sucursal.id");
				
				final GroupingList<Inventario> analisisPorSucursal = new GroupingList<Inventario>(target, c);
				
				CXPFactura fac = a.getCxpFactura();
				String ref1 = fac.getNombre();
				String desc2 = MessageFormat.format("Flete Fac: {0}  {1,date, short} analisis:{2}", fac.getDocumento(), fac.getFecha(), a.getId());		
								
				CantidadMonetaria analizadoDelDia=CantidadMonetaria.pesos(0);
				for(Object obj:target){
					Inventario i=(Inventario)obj;
					MovimientoConFlete ca=(MovimientoConFlete)i;
					//CantidadMonetaria importe=CantidadMonetaria.pesos(ca.getCostoCorte()).multiply(i.getCantidadEnUnidad());
					if(DateUtils.isSameDay(i.getFecha(), fechaPol)){
						analizadoDelDia=analizadoDelDia.add(CantidadMonetaria.pesos(ca.getImporteDelFlete()) );
					}
				}
				
				BigDecimal diferencia=fac.getImporteMN().subtract(analizadoDelDia).amount();
				
				//Transito
				if(diferencia.abs().doubleValue()>1){
					String asiento = "FLETE TRANSITO";
					
					System.out.println("Analisis Flete "+a.getId()+ " Dif: "+diferencia);
					for (List<Inventario> as : analisisPorSucursal) {						
						String ref2 = as.get(0).getSucursal().getNombre();
						BigDecimal importeAcumulado = BigDecimal.ZERO;

						for (final Inventario det : as) {
							MovimientoConFlete ca=(MovimientoConFlete)det;
							//CantidadMonetaria importe=CantidadMonetaria.pesos(ca.getCostoCorte()).multiply(det.getCantidadEnUnidad());
							importeAcumulado = importeAcumulado.add(ca.getImporteDelFlete());
						}						
						// Cargo a Inventario para
						PolizaDetFactory.generarPolizaDet(poliza, "119","IFLT01", true, importeAcumulado, desc2, ref1,ref2, asiento);
					}

					String sql="select min(x.fecha) from ( " +
							"select min(fecha) as fecha from sx_inventario_maq where ANALISIS_FLETE_ID=?" +
							" union" +
							" select min(fecha) as fecha from sx_inventario_com where ANALISIS_FLETE_ID=?" +
							" union" +
							" select min(fecha) as fecha from sx_inventario_trs where ANALISIS_FLETE_ID=?" +
							" union" +
							" select min(fecha) as fecha from sx_inventario_trd where ANALISIS_FLETE_ID=?) as x";
					
					Date fEntrada=(Date)ServiceLocator2.getJdbcTemplate().queryForObject(sql, new Object[]{a.getId(),a.getId(),a.getId(),a.getId()},  Date.class);
					fEntrada=DateUtils.truncate(fEntrada, Calendar.DATE);
					
					if (fEntrada.equals(fechaPol)) {
						
						// Cargo a IVA Acumulado por analisis
						PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC02",true, fac.getImpuestoMN().amount().subtract(fac.getRetencionflete()), desc2, ref1,"TODAS", asiento);
						// Abono a proveedor
					
						PolizaDetFactory.generarPolizaDet(poliza, "205","IMPR03",false,a.getCxpFactura().getRetencionFleteMN().amount(),desc2,ref1,"TODAS", asiento);
						PolizaDetFactory.generarPolizaDet(poliza, "119","IFLT01",true,a.getCxpFactura().getFleteMN().amount(),desc2,ref1,"TODAS", asiento);
					//	PolizaDetFactory.generarPolizaDet(poliza, "117","IVAC02",true,a.getCxpFactura().getImpuestoFleteMN().amount().subtract(a.getCxpFactura().getRetencionFleteMN().amount()),desc2,ref1,"TODAS", asiento);
						PolizaDetFactory.generarPolizaDet(poliza, "117","IVAR02",true,a.getCxpFactura().getRetencionFleteMN().amount(),desc2,ref1,"TODAS", asiento);

						PolizaDetFactory.generarPolizaDet(poliza, "119","ITNS03", true, diferencia, desc2, ref1,"TODAS", asiento);
						// Abono a proveedor
						PolizaDetFactory.generarPolizaDet(poliza, "200", fac.getClave(), false, fac.getTotalMN().amount(),desc2, ref1, "TODAS", asiento);

					} else {
						// abono a Transito
						PolizaDetFactory.generarPolizaDet(poliza, "119","ITNS03", false, analizadoDelDia.amount(), desc2,ref1, "TODAS", asiento);
					}
				}
				//Entrada Normal
				else{
					
					String asiento = "FLETE ENTRADAS";
					
					for (List<Inventario> as : analisisPorSucursal) {
						String ref2 = as.get(0).getSucursal().getNombre();

						BigDecimal importeAcumulado = BigDecimal.ZERO;
						
						for (final Inventario det : as) {
							
							MovimientoConFlete ca=(MovimientoConFlete)det;
							//CantidadMonetaria importe=CantidadMonetaria.pesos(ca.getCostoCorte()).multiply(det.getCantidadEnUnidad());
							importeAcumulado = importeAcumulado.add(ca.getImporteDelFlete());
							

						}
						// ivaAcumulado=ivaAcumulado.add(MonedasUtils.calcularImpuesto(importeAcumulado));
						// Cargo a Inventario para
						importeAcumulado = PolizaUtils.redondear(importeAcumulado);
						PolizaDetFactory.generarPolizaDet(poliza, "119","IFLT01", true, importeAcumulado, desc2, ref1,ref2, asiento);
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
	}

}
