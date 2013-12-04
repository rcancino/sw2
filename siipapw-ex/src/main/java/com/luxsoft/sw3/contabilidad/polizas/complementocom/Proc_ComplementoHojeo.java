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
import com.luxsoft.siipap.inventarios.model.CostoHojeable;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;
import com.luxsoft.sw3.maquila.model.AnalisisDeHojeo;

public class Proc_ComplementoHojeo implements IProcesador {

	

	public void procesar(Poliza poliza, ModelMap model) {

		EventList<AnalisisDeHojeo> analisis = (EventList<AnalisisDeHojeo>) model.get("analisisDeHojeo");
	

		for (AnalisisDeHojeo a : analisis) {
			
			Date fechaPol = poliza.getFecha();
			fechaPol=DateUtils.truncate(fechaPol, Calendar.DATE);
			if(true){
				
				
				EventList source = new BasicEventList(0);
				for(Inventario i:a.getEntradas()){
					if(DateUtils.isSameDay(i.getFecha(), fechaPol))
						source.add(i);
				}
				for(Inventario i:a.getTransformaciones()){
					if(DateUtils.isSameDay(i.getFecha(), fechaPol))
						source.add(i);
				}
				
				Comparator c = GlazedLists.beanPropertyComparator(Inventario.class, "sucursal.id");
				
				final GroupingList<Inventario> analisisPorSucursal = new GroupingList<Inventario>(source, c);
				
				CXPFactura fac = a.getCxpFactura();
				String ref1 = fac.getNombre();
				String desc2 = MessageFormat.format("Hojeo Fac: {0}  {1,date, short} Analisis:{2}", fac.getDocumento(), fac.getFecha(), a.getId());

								
				CantidadMonetaria analizadoDelDia=CantidadMonetaria.pesos(0);
				for(Object obj:source){
					Inventario i=(Inventario)obj;
					CostoHojeable ca=(CostoHojeable)i;
					CantidadMonetaria importe=CantidadMonetaria.pesos(ca.getCostoCorte()).multiply(i.getCantidadEnUnidad());
					if(DateUtils.isSameDay(i.getFecha(), fechaPol)){
						analizadoDelDia=analizadoDelDia.add(importe);
					}
				}
				
				BigDecimal diferencia=fac.getImporteMN().subtract(analizadoDelDia).amount();
				
				//Transito
				if(diferencia.abs().doubleValue()>1){
					String asiento = "HOJEO TRANSITO";
					
					System.out.println("Analisis "+a.getId()+ " Dif: "+diferencia);
					for (List<Inventario> as : analisisPorSucursal) {						
						String ref2 = as.get(0).getSucursal().getNombre();
						BigDecimal importeAcumulado = BigDecimal.ZERO;

						for (final Inventario det : as) {
							CostoHojeable ca=(CostoHojeable)det;
							CantidadMonetaria importe=CantidadMonetaria.pesos(ca.getCostoCorte()).multiply(det.getCantidadEnUnidad());
							importeAcumulado = importeAcumulado.add(importe.amount());
						}						
						// Cargo a Inventario para
						PolizaDetFactory.generarPolizaDet(poliza, "119","IMQC01", true, importeAcumulado, desc2, ref1,ref2, asiento);
					}

					String sql="select max(x.fecha) from ( " +
							"select min(fecha) as fecha from sx_inventario_maq where ANALISIS_HOJEO_ID=?" +
							" union" +
							" select min(fecha) as fecha from sx_inventario_trs where ANALISIS_HOJEO_ID=?) as x";
					Date fEntrada=(Date)ServiceLocator2.getJdbcTemplate().queryForObject(sql, new Object[]{a.getId(),a.getId()},  Date.class);
					fEntrada=DateUtils.truncate(fEntrada, Calendar.DATE);
					
					if (fEntrada.equals(fechaPol)) {
						// Cargo a IVA Acumulado por analisis
						PolizaDetFactory.generarPolizaDet(poliza, "117","IVAC02", true, fac.getImpuestoMN().amount(),desc2, ref1, "TODAS", asiento);

						PolizaDetFactory.generarPolizaDet(poliza, "119","ITNS01", true, diferencia, desc2, ref1,"TODAS", asiento);
						// Abono a proveedor
						PolizaDetFactory.generarPolizaDet(poliza, "200", fac.getClave(), false, fac.getTotalMN().amount(),desc2, ref1, "TODAS", asiento);

					} else {
						// abono a Transito
						PolizaDetFactory.generarPolizaDet(poliza, "119","ITNS01", false, analizadoDelDia.amount(), desc2,ref1, "TODAS", asiento);
					}
				}
				//Entrada Normal
				else{
					
					String asiento = "HOJEO ENTRADAS";
					
					for (List<Inventario> as : analisisPorSucursal) {
						String ref2 = as.get(0).getSucursal().getNombre();

						BigDecimal importeAcumulado = BigDecimal.ZERO;
						
						for (final Inventario det : as) {
							
							CostoHojeable ca=(CostoHojeable)det;
							CantidadMonetaria importe=CantidadMonetaria.pesos(ca.getCostoCorte()).multiply(det.getCantidadEnUnidad());
							importeAcumulado = importeAcumulado.add(importe.amount());
							

						}
						// ivaAcumulado=ivaAcumulado.add(MonedasUtils.calcularImpuesto(importeAcumulado));
						// Cargo a Inventario para
						importeAcumulado = PolizaUtils.redondear(importeAcumulado);
						PolizaDetFactory.generarPolizaDet(poliza, "119","IMQC01", true, importeAcumulado, desc2, ref1,ref2, asiento);
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
