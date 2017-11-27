package com.luxsoft.sw3.contabilidad.polizas.cobranza;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.util.SystemOutLogger;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.matchers.Matcher;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

/**
 * Acumulablr IETU 
 * 
 * AIETU01 - MOS
 * AIETU02 - CAM
 * AIETU03 - CRE
 * AIETU04 - GENERICO POR IDENTIFICAR
 * AIETU05 - GENERICO ANTICIPOS
 * AIETU06 - GENERICO A FAVOR
 * AIETU07 - GENERICO OTROS INGRESOS
 * 
 * IETU Acumulable 
 * 
 * IETUA01 - MOS
 * IETUA02 - CAM
 * IETUA03 - CRE
 * IETUA04 - GENERICO POR IDENTIFICAR
 * IETUA05 - GENERICO ANTICIPOS
 * IETUA06 - GENERICO A FAVOR
 * IETUA07 - GENERICO OTROS INGRESOS
 * 
 * @author RUBEN
 *
 */
public class Proc_IETU implements IProcesador{
	
	
	PagosPorTipo pagosPorTipo;
	
	private String cuentaAIETU="902";
	private String cuentaIETUA="903";
	private String cuentaAIETUDesc="ACUMULABLE IETU ";
	private String cuentaIETUADesc="IETU ACUMULABLE ";
	private String sucursal;
	private final String origen;
	private final String conceptoSufix;
	
	public Proc_IETU(String origen,String conceptoSufix) {
		this.origen=origen;
		this.conceptoSufix=conceptoSufix;
	}
	
	public void procesar(final Poliza poliza, ModelMap model){
		
		//pagosPorTipo=(PagosPorTipo)model.get("pagosPorTipo");
		Comparator<Pago> c=new Comparator<Pago>() {
			public int compare(Pago p1, Pago p2) {
				 if(p1.isAnticipo() || p2.isAnticipo())
				  return p1.getSucursal().getNombre().compareTo(p2.getSucursal().getNombre());
					 
				Aplicacion a1=p1.findPrimeraAplicacion(poliza.getFecha());
				Aplicacion a2=p2.findPrimeraAplicacion(poliza.getFecha());
			
				if(a1==null ){
						System.out.println("P1:"+p1.getId());
				}
				if(a2==null ){
					System.out.println("P2:"+p2.getId());
				}
				if(a1.getDetalle().getOrigen().equals("MOS") || a1.getDetalle().getOrigen().equals("CAM") ){
					
					return a1.getDetalle().getSucursal().compareTo(a2.getDetalle().getSucursal());
				}else{
					sucursal="OFICINAS";
					return 0;
				}
			}
		};
		EventList<Pago> source=(EventList<Pago>)model.get("pagos");
		Matcher<Pago> mm1=new Matcher<Pago>() {
			public boolean matches(Pago item) {
				if (item.isAnticipo() && item.getFecha().equals(poliza.getFecha()))
					return true;
				for(Aplicacion a:item.getAplicaciones()){
					System.out.println("-------"+item.getId());					
					if(DateUtils.isSameDay(a.getFecha(),poliza.getFecha())){
						if(a.getDetalle().getOrigen().equals(origen)){
							return true;
						}
					}					
				}
				return false;
				//return item.getOrigenAplicacion().endsWith(origen);
			}
		};
		source=new FilterList<Pago>(source,mm1);
		GroupingList<Pago> pagosPorSucursal=new GroupingList<Pago>(source,c);	
		for(List<Pago> lp:pagosPorSucursal){
		
			pagosPorTipo=new PagosPorTipo(GlazedLists.eventList(lp), poliza.getFecha());
			if(origen.equals("MOS")|| origen.equals("CAM")){
				if(lp.get(0).isAnticipo()){
				 this.sucursal=lp.get(0).getSucursal().getNombre();
				}
				else{
					Aplicacion a1=lp.get(0).findPrimeraAplicacion(poliza.getFecha());
					this.sucursal=a1.getDetalle().getSucursal();
				}
				
			}
			//System.out.println("Procesando cobranza : "+sucursal+pagosPorTipo.resumenAplicado(poliza.getFecha()));
			
			// IETU de Cobranza
			BigDecimal acumPagoPorTC=BigDecimal.ZERO;
			for(PagoConDeposito p :pagosPorTipo.getDepositos()){
				

				//BigDecimal pagoPorTC= p.getTotal().multiply(new BigDecimal(p.getTc()));	 
				BigDecimal pagoPorTC= p.getImporteAplicado(poliza.getFecha()).multiply(new BigDecimal(p.getTc()));
				acumPagoPorTC=acumPagoPorTC.add(pagoPorTC);
				 				 
				
			 }
			BigDecimal importeAcumulado=acumularImporteAplicado(pagosPorTipo.getCheques(), poliza.getFecha());	
				
						
			
			 
			 importeAcumulado=importeAcumulado.add(acumularImporteAplicado(pagosPorTipo.getEfectivo(),poliza.getFecha()));
			 
			
			// importeAcumulado=importeAcumulado.add(acumularImporteAplicado(pagosPorTipo.getDepositos(),poliza.getFecha()));	
			 importeAcumulado=importeAcumulado.add(acumPagoPorTC);
		
			 
			 if("MOS".equals(origen) || "CAM".equals(origen)){
				 importeAcumulado=importeAcumulado.add(acumularImporteAplicado(pagosPorTipo.getTarjeta(),poliza.getFecha()));
				 
				
			 }		
			
			
			 importeAcumulado=importeAcumulado.add(acumularImporteAplicado(pagosPorTipo.getSaldosAFavor(),poliza.getFecha()));	
			
		
			//IETU de depositos por identificar que se agrega a la cobranza
			 BigDecimal acumDIPorTC=BigDecimal.ZERO;
				for(PagoConDeposito p :pagosPorTipo.getDepositosPorIdentificar()){
					

					//BigDecimal pagoPorTC= p.getTotal().multiply(new BigDecimal(p.getTc()));	 
					BigDecimal pagoPorTC= p.getImporteAplicado(poliza.getFecha()).multiply(new BigDecimal(p.getTc()));
					acumDIPorTC=acumDIPorTC.add(pagoPorTC);
					 				 
					
				}
			 
			// importeAcumulado=importeAcumulado.add(acumularImporteAplicado(pagosPorTipo.getDepositosPorIdentificar(), poliza.getFecha()));
			 importeAcumulado=importeAcumulado.add(acumDIPorTC);
			 
			PolizaDetFactory.generarPolizaDet(poliza, cuentaAIETU, "AIETU"+conceptoSufix, true, importeAcumulado, cuentaAIETUDesc,origen, sucursal, "IETU Pagos");
			PolizaDetFactory.generarPolizaDet(poliza, cuentaIETUA, "IETUA"+conceptoSufix, false, importeAcumulado,cuentaIETUADesc, origen, sucursal,"IETU Pagos");
			
			//IETU de Depositos por identificar
			String asiento="IETU por identificar";		
			//BigDecimal importeDepPorIdentificar=acumularImporteAplicado(pagosPorTipo.getDepositosPorIdentificar(), poliza.getFecha());		
			BigDecimal importeDepPorIdentificar=acumDIPorTC;	
			BigDecimal importeAcumuladoNormal=pagosPorTipo.sumarDiferenciaDePagos(poliza.getFecha());
			
			
			
			//***Existe un caso que manda IETU por identificar que no corresponde, el efecto lo provoca importeAcumulado*** 
			//Se debe documentar esta linea importeDepPorIdentificar 
			importeDepPorIdentificar=importeDepPorIdentificar.add(importeAcumuladoNormal) ;
			
			
			if(importeDepPorIdentificar.doubleValue()>8.62){
			//	PolizaDetFactory.generarPolizaDet(poliza, cuentaAIETU, "AIETU04", false, importeDepPorIdentificar.add(importeAcumuladoNormal), cuentaAIETUDesc+asiento, origen, sucursal, asiento);
			//	PolizaDetFactory.generarPolizaDet(poliza, cuentaIETUA, "IETUA04", true, importeDepPorIdentificar.add(importeAcumuladoNormal),cuentaIETUADesc+asiento, origen, sucursal, asiento);	
				
				PolizaDetFactory.generarPolizaDet(poliza, cuentaAIETU, "AIETU04", false, importeDepPorIdentificar, cuentaAIETUDesc+asiento, origen, sucursal, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, cuentaIETUA, "IETUA04", true, importeDepPorIdentificar,cuentaIETUADesc+asiento, origen, sucursal, asiento);
			
			}
			
			
			
			diferencias(poliza,model);
			anticipos(poliza, model);
//			anticiposAplicacion(poliza, model); 
			disponiblesAplicacion(poliza, model);
			
			
		}
		saldosAFavor(poliza, model,pagosPorSucursal);
	}
	
	
	
	private void diferencias(final Poliza poliza, ModelMap model) {
		
		String asiento="IETU de Diferencias";
		
		BigDecimal importeAcumuladoNormal=pagosPorTipo.sumarDiferenciaDePagos(poliza.getFecha());
		BigDecimal importeAcumuladoSAF=pagosPorTipo.sumarDiferenciaDeSAF(poliza.getFecha());
		
		// Ajuste de un pago 
		/*importeAcumuladoNormal=importeAcumuladoNormal.add(importeAcumuladoSAF);
		PolizaDetFactory.generarPolizaDet(poliza, cuentaAIETU, "AIETU07", true, importeAcumuladoNormal, cuentaAIETUDesc+asiento, origen, sucursal, asiento);
		PolizaDetFactory.generarPolizaDet(poliza, cuentaIETUA, "IETUA07", false,importeAcumuladoNormal,cuentaIETUADesc+asiento, origen, sucursal, asiento);
		*/
		//Ajuste de un saldo a favor
//		PolizaDetFactory.generarPolizaDet(poliza, cuentaAIETU, "AIETU06", false, importeAcumuladoSAF, cuentaAIETUDesc+asiento, origen, sucursal, asiento);
//		PolizaDetFactory.generarPolizaDet(poliza, cuentaIETUA, "IETUA06", true,importeAcumuladoSAF,cuentaIETUADesc+asiento, origen, sucursal, asiento);
		
	//	System.out.println("PRIMERA APLICACION OI: "+importeAcumuladoNormal+" OI SAF: "+importeAcumuladoSAF);
		
	}
	
	private void anticipos(final Poliza poliza, ModelMap model) {
		
		String asiento="IETU anticipos";
		BigDecimal importeAcumulado=BigDecimal.ZERO;
		for(Pago pago:pagosPorTipo.getAnticipos()){
			if(DateUtils.isSameDay(pago.getFecha(), poliza.getFecha())){
				BigDecimal importe=MonedasUtils.calcularImporteDelTotal(pago.getTotal().abs());
				importeAcumulado=importeAcumulado.add(importe);
				sucursal=pago.getSucursal().getNombre();
			}
		}
		PolizaDetFactory.generarPolizaDet(poliza, cuentaAIETU, "AIETU05", true, importeAcumulado, cuentaAIETUDesc+asiento, origen, sucursal, asiento);
		PolizaDetFactory.generarPolizaDet(poliza, cuentaIETUA, "IETUA05", false, importeAcumulado,cuentaIETUADesc+asiento, origen, sucursal, asiento);
		
	}
	
/*	private void anticiposAplicacion(final Poliza poliza, ModelMap model) {
		String asiento="IETU anticipos aplic";
		
		BigDecimal importeAcumulado=acumularImporteAplicado(pagosPorTipo.getAnticipos(), poliza.getFecha());
		String ref1=origen;
		String ref2=sucursal;
		
		//Cancelacion de IETU
		PolizaDetFactory.generarPolizaDet(poliza, cuentaAIETU, "AIETU05", false, importeAcumulado, cuentaAIETUDesc, ref1, ref2, asiento);
		PolizaDetFactory.generarPolizaDet(poliza, cuentaIETUA, "IETUA05", true, importeAcumulado,cuentaIETUADesc, ref1, ref2, asiento);
		
	}*/
	
	private void disponiblesAplicacion(final Poliza poliza ,ModelMap model){		
		String asiento="IETU Aplicación SAF ";
		BigDecimal importeAcumulado=acumularImporteAplicado(pagosPorTipo.getSaldosAFavor(),poliza.getFecha());
		
		String ref1=origen;
		String ref2=sucursal;
		//Cancelacion de IETU
		PolizaDetFactory.generarPolizaDet(poliza, cuentaAIETU, "AIETU06", false, importeAcumulado, cuentaAIETUDesc +" SAF ", ref1, ref2, asiento);
		PolizaDetFactory.generarPolizaDet(poliza, cuentaIETUA, "IETUA06", true, importeAcumulado,cuentaIETUADesc +" SAF", ref1, ref2, asiento);
		
	}
	/**
	 * Registra el IETU del acumulado de saldos a favor
	 * 
	 * @param poliza
	 * @param model
	 */
	private void saldosAFavor(final Poliza poliza,ModelMap model,GroupingList<Pago> pagosPorSucursal){
		String asiento="IETU SAF ";
		for(List<Pago> pagos:pagosPorSucursal){
			BigDecimal importeAcumulado=BigDecimal.ZERO;
			for(final Pago pago:pagos){
				if(!pago.isAnticipo() && DateUtils.isSameDay(poliza.getFecha(), pago.getPrimeraAplicacion()) ){
						
					if(CobranzaUtils.aplicaIetu(pago)){				
						BigDecimal disponible=pago.getDisponibleAlCorte(poliza.getFecha()).amount();
						//System.out.println("Disponible:  "+ pago.getInfo() +"--"+ disponible);
						if(disponible.doubleValue()>0){
							
							BigDecimal diferencia=pago.getDiferencia();
							if(    (pago.getDirefenciaFecha()==null) 
								|| (poliza.getFecha().compareTo(pago.getDirefenciaFecha())<0)
									){
								diferencia=BigDecimal.ZERO;
							}
							
							if(diferencia.doubleValue()==0){
								BigDecimal importeDisponible=PolizaUtils.calcularImporteDelTotal(disponible);
								
								if(pago instanceof PagoConDeposito){
									
									if(!DateUtil.isSameMonth(pago.getPrimeraAplicacion(), ((PagoConDeposito) pago).getFechaDeposito())){
										
										
										importeDisponible=new BigDecimal(0);
									}
									
								}
								
								importeDisponible=PolizaUtils.redondear(importeDisponible);
								importeAcumulado=importeAcumulado.add(importeDisponible);
							}
						}
					}
				}		
			}
			String ref1=origen;
			String ref2=pagos.get(0).findPrimeraAplicacion(poliza.getFecha()).getDetalle().getSucursal();
			PolizaDetFactory.generarPolizaDet(poliza, cuentaAIETU, "AIETU06", true, importeAcumulado, cuentaAIETUDesc+" SAF", ref1, ref2, asiento);
			PolizaDetFactory.generarPolizaDet(poliza, cuentaIETUA, "IETUA06", false,importeAcumulado,cuentaIETUADesc+ " SAF", ref1, ref2, asiento);		

		}
		
	}
	
	
	
	private BigDecimal acumularImporteAplicado(List<? extends Pago> pagoss,Date fecha){
		BigDecimal importeAcumulado=BigDecimal.ZERO;
		for(Pago pago:pagoss){	
			//Correccion Para que el importe aplicado no considere los anticipos de efectivo. 06-08-2012 
			if(pago.isAnticipo() && (pago instanceof PagoConEfectivo || pago instanceof PagoConCheque))
				continue;
			BigDecimal totalAplicado=pago.getAplicado(fecha,sucursal);
			if("OFICINAS".equals(sucursal))
				totalAplicado=pago.getAplicado(fecha);
			BigDecimal importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado.multiply(BigDecimal.valueOf(1)));
			importeAplicado=PolizaUtils.redondear(importeAplicado);
			
			importeAcumulado=importeAcumulado.add(importeAplicado);
					
		}
		return importeAcumulado;
	}
	
	
	


}
