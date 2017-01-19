package com.luxsoft.sw3.contabilidad.polizas.maquila;

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

import com.luxsoft.siipap.cxp.model.AnalisisDeTransformacion;
import com.luxsoft.siipap.cxp.model.CXPFactura;

import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

import com.luxsoft.sw3.maquila.model.AnalisisDeMaterial;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;


public class Proc_EntradaDeMaterialAlMaquilador implements IProcesador {

	

	public void procesar(Poliza poliza, ModelMap model) {

		EventList<AnalisisDeMaterial> analisis = (EventList<AnalisisDeMaterial>) model.get("analisisDeMaterial");
	

		for (AnalisisDeMaterial a : analisis) {
			
			Date fechaPol = poliza.getFecha();
			fechaPol=DateUtils.truncate(fechaPol, Calendar.DATE);
			if(true){
				
				EventList source = new BasicEventList(0);
				for(EntradaDeMaterialDet i:a.getEntradas()){
					if(DateUtils.isSameDay(i.getFecha(), fechaPol))
						source.add(i);
				}
				Comparator c = GlazedLists.beanPropertyComparator(EntradaDeMaterialDet.class, "almacen.id");
				
				final GroupingList<EntradaDeMaterialDet> analisisPorSucursal = new GroupingList<EntradaDeMaterialDet>(source, c);
				
				CXPFactura fac = a.getCxpFactura();
				System.out.println("Analisis"+ a.getId());
				System.out.println("Factura"+ fac);
				
				String ref1 = fac.getNombre();
				String desc2 = MessageFormat.format("Fac: {0}  {1,date, short} Analisis:{2}", fac.getDocumento(), fac.getFecha(), a.getId());

								
				CantidadMonetaria analizadoDelDia=CantidadMonetaria.pesos(0);
				for(Object obj:source){
					EntradaDeMaterialDet i=(EntradaDeMaterialDet)obj;
					CantidadMonetaria importe=CantidadMonetaria.pesos(i.getImporte());
					if(DateUtils.isSameDay(i.getFecha(), fechaPol)){
						analizadoDelDia=analizadoDelDia.add(importe);
					}
				}
				
				BigDecimal diferencia=fac.getImporteMN().subtract(analizadoDelDia).amount();
				
				//Transito
				if(diferencia.abs().doubleValue()>1){
					String asiento = "ENTRADA MAQUILADOR";
					
					System.out.println("Analisis Maq"+a.getId()+ " Dif: "+diferencia);
					for (List<EntradaDeMaterialDet> as : analisisPorSucursal) {
						String concepto=as.get(0).getAlmacen().getId().toString();
						String ref2 = as.get(0).getAlmacen().getNombre();
						BigDecimal importeAcumulado = BigDecimal.ZERO;

						for (final EntradaDeMaterialDet det : as) {
							
							CantidadMonetaria importe=CantidadMonetaria.pesos(det.getImporte());
							importeAcumulado = importeAcumulado.add(importe.amount());
						}						
						// Cargo a Inventario para
						PolizaDetFactory.generarPolizaDet(poliza, "119","IMQM01", true, importeAcumulado, desc2, ref1,ref2, asiento);
					}

					String sql="select min(E.fecha) as fecha from SX_MAQ_ENTRADASDET e JOIN sx_maq_entradas_analizadas A ON(A.ENTRADADET_ID=E.ENTRADADET_ID) where A.ANALISIS_ID=?";
					Date fEntrada=(Date)ServiceLocator2.getJdbcTemplate().queryForObject(sql, new Object[]{a.getId()},  Date.class);
					fEntrada=DateUtils.truncate(fEntrada, Calendar.DATE);					
					
					if (fEntrada.equals(fechaPol)) {
						
						String moneda=fac.getMoneda().toString();
						String clv="200";					
													
						if(moneda.equals("USD")){
							clv="201";
						}	
						
						System.err.println("Moneda "+moneda+" clave "+clv);
						
						// Cargo a IVA Acumulado por analisis
						PolizaDetFactory.generarPolizaDet(poliza, "117","IVAC02", true, fac.getImpuestoMN().amount(),desc2, ref1, "TODAS", asiento);

						PolizaDetFactory.generarPolizaDet(poliza, "119","ITNS05", true, diferencia, desc2, ref1,"TODAS", asiento);
						// Abono a proveedor
						PolizaDetFactory.generarPolizaDet(poliza, clv, fac.getClave(), false, fac.getTotalMN().amount(),desc2, ref1, "TODAS", asiento);

					} else {
						// abono a Transito
						PolizaDetFactory.generarPolizaDet(poliza, "119","ITNS05", false, analizadoDelDia.amount(), desc2,ref1, "TODAS", asiento);
				
					}
				}
				//Entrada Normal
				else{
					
					String asiento = "ENTRADA MAQUILADOR";
					
					for (List<EntradaDeMaterialDet> as : analisisPorSucursal) {
						String ref2 = as.get(0).getAlmacen().getNombre();

						BigDecimal importeAcumulado = BigDecimal.ZERO;
						
						for (final EntradaDeMaterialDet det : as) {
							
							CantidadMonetaria importe=CantidadMonetaria.pesos(det.getImporte());
							importeAcumulado = importeAcumulado.add(importe.amount());
							

						}
						// ivaAcumulado=ivaAcumulado.add(MonedasUtils.calcularImpuesto(importeAcumulado));
						// Cargo a Inventario para
						importeAcumulado = PolizaUtils.redondear(importeAcumulado);
						PolizaDetFactory.generarPolizaDet(poliza, "119","IMQM01", true, importeAcumulado, desc2, ref1,ref2, asiento);
					}
					
					String moneda=fac.getMoneda().toString();
					String clv="200";					
												
					if(moneda.equals("USD")){
						clv="201";
					}	
					
					System.err.println("Moneda2 "+moneda+" clave2 "+clv);
					
					
					// Cargo a IVA Acumulado por analisis
					PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC02",true, fac.getImpuestoMN().amount(), desc2, ref1,"TODAS", asiento);
					// Abono a proveedor
					PolizaDetFactory.generarPolizaDet(poliza, clv,fac.getClave(), false, fac.getTotalMN().amount(),desc2, ref1, "TODAS", asiento);

					
				}

			}	

		}
	}

}
