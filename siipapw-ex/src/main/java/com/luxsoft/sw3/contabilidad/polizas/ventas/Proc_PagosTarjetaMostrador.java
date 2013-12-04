package com.luxsoft.sw3.contabilidad.polizas.ventas;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;


import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;
import com.luxsoft.sw3.tesoreria.model.CargoAbonoPorCorte;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;

public class Proc_PagosTarjetaMostrador implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		
		String asiento="COBRANZA TARJETA";
		String ref1="MOS";
		
		List<CorteDeTarjeta> cortes=(List<CorteDeTarjeta>)model.get("cortes");
		for(CorteDeTarjeta corte:cortes){
			String desc2=MessageFormat.format("Corte - {0}",corte.getTipoDeTarjeta()+ " Folio: "+corte.getId());
			if(corte.getTipoDeTarjeta().startsWith("AMEX")){
				//Cargo a AMEX				
				PolizaDetFactory.generarPolizaDet(poliza,"109","DEUD01", true, corte.getTotal(), desc2,ref1, corte.getSucursal().getNombre(),asiento+" "+ref1);
				
				// Abon a AMEX (Para traspaso a banco= 109 = MontoTrasladable+Comision_AMEX+Impuesto AMEX)
				BigDecimal montoTrasladable=this.getTrasladoAmex(corte);
			//	PolizaDetFactory.generarPolizaDet(poliza,"109","DEUD01", false, montoTrasladable, desc2, ref1, corte.getSucursal().getNombre(),asiento);
				PolizaDetFactory.generarPolizaDet(poliza,"109","DEUD01", false, corte.getTotal(), desc2, ref1, corte.getSucursal().getNombre(),asiento);
				PolizaDetFactory.generarPolizaDet(poliza,"102",corte.getCuenta().getNumero().toString(), true, montoTrasladable, desc2, ref1, corte.getSucursal().getNombre(),asiento);
				
				/*
				//Abono a comision e IVA de Comision  AMEX
				for(CargoAbonoPorCorte aplic:corte.getAplicaciones()){
					switch (aplic.getTipo()) {
					case COMISION_AMEX:
						PolizaDetFactory.generarPolizaDet(poliza,"109","DEUD01", false,aplic.getCargoAbono().getImporte().abs(), "COMISION "+desc2, ref1, corte.getSucursal().getNombre(),asiento);
						break;
					case IMPUESTO:
						PolizaDetFactory.generarPolizaDet(poliza,"109","DEUD01", false,aplic.getCargoAbono().getImporte().abs(), "IVA COMISION "+desc2, ref1, corte.getSucursal().getNombre(),asiento);
						break;
					default:
						break;
					}
				}
				*/
			}else{
				// Cargo a bancos
				PolizaDetFactory.generarPolizaDet(poliza,"102",corte.getCuenta().getNumero().toString(), true, corte.getTotal().abs(), desc2,ref1, corte.getSucursal().getNombre(),asiento+" "+ref1);
				
				//Abono a comision e IVA de Comision  DEBITO
				for(CargoAbonoPorCorte aplic:corte.getAplicaciones()){
					switch (aplic.getTipo()) {
					case COMISION_DEBITO:
						PolizaDetFactory.generarPolizaDet(poliza,"102",corte.getCuenta().getNumero().toString(), false,aplic.getCargoAbono().getImporte().abs(),aplic.getComentario()+" "+desc2, ref1, corte.getSucursal().getNombre(),asiento);
						break;
					case IMPUESTO:
						PolizaDetFactory.generarPolizaDet(poliza,"102",corte.getCuenta().getNumero().toString(), false,aplic.getCargoAbono().getImporte().abs(),aplic.getComentario()+" "+desc2, ref1, corte.getSucursal().getNombre(),asiento);
						break;
					case COMISION_CREDITO:
						PolizaDetFactory.generarPolizaDet(poliza,"102",corte.getCuenta().getNumero().toString(), false,aplic.getCargoAbono().getImporte().abs(),aplic.getComentario()+" "+desc2, ref1, corte.getSucursal().getNombre(),asiento);
						break;
					default:
						break;
					}
				}
				
			}
			//Cargo a Gastos (comision+impuesto del corte)
			BigDecimal importeIetu=BigDecimal.ZERO;
			for(CargoAbonoPorCorte aplic:corte.getAplicaciones()){
				switch (aplic.getTipo()) {
				case COMISION_DEBITO:
				case COMISION_CREDITO:
					PolizaDetFactory.generarPolizaDet(poliza,"600","151717", true,aplic.getCargoAbono().getImporte().abs(),aplic.getComentario()+" "+desc2, ref1, corte.getSucursal().getNombre(),asiento);
					importeIetu=importeIetu.add(aplic.getCargoAbono().getImporte().abs());
					break;
					case COMISION_AMEX:
					PolizaDetFactory.generarPolizaDet(poliza,"600","256203", true,aplic.getCargoAbono().getImporte().abs(),aplic.getComentario()+" "+desc2, ref1, corte.getSucursal().getNombre(),asiento);
					importeIetu=importeIetu.add(aplic.getCargoAbono().getImporte().abs());
					break;
				case IMPUESTO:
					PolizaDetFactory.generarPolizaDet(poliza,"117","IVAG01", true,aplic.getCargoAbono().getImporte().abs(),aplic.getComentario()+" "+desc2, ref1, corte.getSucursal().getNombre(),asiento);
					break;
				
				default:
					break;
				}
			}
			
			
			//IETU DEDUCIBLE GASTOS 
			PolizaDetFactory.generarPolizaDet(poliza, "900", "IETUD02", true,importeIetu,desc2+ " IETU DEDUCIBLE ", "MOS", corte.getSucursal().getNombre(), asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "901","DIETU02", false,importeIetu,desc2+ " IETU DEDUCIBLE ", "MOS", corte.getSucursal().getNombre(), asiento);
			
			
			
		}
		
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");
		Comparator<Pago> c=GlazedLists.beanPropertyComparator(Pago.class, "sucursal.id","origenAplicacion");
		GroupingList<Pago> pagosPorSucursalYOrigen=new GroupingList<Pago>(pagos,c);
		
		
		
		// Aculuar la cobranza PagoConCheque y PagoConEfectivo
		for(List<Pago> lpagos:pagosPorSucursalYOrigen){
			
			if(lpagos.get(0).getSucursal().getId()==2L){
				System.out.println("DEBUG: ");
			}
			
			BigDecimal totalAplicado=BigDecimal.ZERO;
			BigDecimal importeAplicado=BigDecimal.ZERO;
			BigDecimal ivaAplicado=BigDecimal.ZERO;
			
			
			
			Pago primerPagoDeLaSucursal=lpagos.get(0);
			String origen=primerPagoDeLaSucursal.getOrigenAplicacion();
			String ref2=primerPagoDeLaSucursal.getSucursal().getNombre();
			
			//System.out.println("cLIENTE "+pago.getClave()+"total " +pago.getTotal() + " Pagos "+lpagos.size()+" Origen: "+origen.substring(0,3)+ "Suc: "+pago.getSucursal()+ " Size: "+lpagos.size());
			for(Pago p:lpagos){

				if((p.isAnticipo() && p instanceof PagoConTarjeta && p.getFecha().equals(poliza.getFecha())))
					continue;
				
				if( (p instanceof PagoConTarjeta) ){
					//System.out.println("Acumulado pago tar: "+p+ "  Orig: "+p.getOrigenAplicacion());
					if(DateUtils.isSameDay(p.getPrimeraAplicacion(), poliza.getFecha())){
						if(!"MOS".equals(origen)){
							//System.out.println("Acumulado pago tar: "+p);
							totalAplicado=totalAplicado.add(p.getAplicado(poliza.getFecha()));
						
							
						}
						
						if("MOS".equals(origen) || "CAM".equals(origen)){
							PolizaDetFactory.generarSaldoAFavor(poliza, p, p.getOrigenAplicacion(), asiento);
							PolizaDetFactory.generarOtrosIngresos(poliza, p, p.getOrigenAplicacion(), asiento);	
						}
						
						
					}					
				}
				if(p instanceof PagoConTarjeta )
					if(DateUtils.isSameDay(p.getPrimeraAplicacion(), poliza.getFecha())){
						
						BigDecimal totalPago=p.getTotal();
						BigDecimal importePago=MonedasUtils.calcularImporteDelTotal(totalPago);
						BigDecimal ivaPago=MonedasUtils.calcularImpuestoDelTotal(totalPago);
						
						if("CREDITO".equals(p.getOrigen().toString()) || "CHE".equals(p.getOrigen()) || "JUR".equals(p.getOrigen())  ){
							PolizaDetFactory.generarPolizaDet(poliza,"203", "DIVR02", false, importePago, p.getInfo()+" "+p.getNombre()+" "+origen, origen, ref2, asiento+" "+ref1);						
							PolizaDetFactory.generarPolizaDet(poliza,"206", "IVAV01", false, ivaPago, p.getInfo()+" "+p.getNombre()+" "+origen, origen, ref2, asiento+" "+ref1);
							
							PolizaDetFactory.generarPolizaDet(poliza,"902", "AIETU03", true, importePago,"ACUMULABLE IETU ", origen, ref2, asiento+" "+ref1);
							PolizaDetFactory.generarPolizaDet(poliza,"903", "IETUA03", false, importePago,"ACUMULABLE IETU ", origen, ref2, asiento+" "+ref1);
						}
							
							
							
					}
			}
			importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);		
			ivaAplicado=PolizaUtils.calcularImpuesto(importeAplicado);
			importeAplicado=PolizaUtils.redondear(importeAplicado);
			ivaAplicado=PolizaUtils.redondear(ivaAplicado);
			
			//System.out.println(" Abono a clientes : "+totalAplicado);
			
			
			if("CAM".equals(origen)){
				//Abono a cliente camioneta 
				PolizaDetFactory.generarPolizaDet(poliza,"105", primerPagoDeLaSucursal.getSucursal().getId().toString(), false, totalAplicado, "Clientes cobranza "+origen, origen, ref2, asiento+" "+ref1);
				PolizaDetFactory.generarPolizaDet(poliza,"206", "IVAV02", true,  MonedasUtils.calcularImpuestoDelTotal(totalAplicado), "Clientes cobranza "+origen, origen, ref2, asiento+" "+ref1);
				PolizaDetFactory.generarPolizaDet(poliza,"206", "IVAV01", false, MonedasUtils.calcularImpuestoDelTotal(totalAplicado), "Clientes cobranza "+origen, origen, ref2, asiento+" "+ref1);
			}
			
	/*		else if("CRE".equals(origen) )
				PolizaDetFactory.generarPolizaDet(poliza,"203", "DIVR02", false, totalAplicado, "Acredores diversos "+origen, origen, ref2, asiento+" "+ref1);
			else if("CHE".equals(origen) )
				PolizaDetFactory.generarPolizaDet(poliza,"203", "DIVR02", false, totalAplicado, "Acredores diversos "+origen, origen, ref2, asiento+" "+ref1);
			else if("JUR".equals(origen) )
				PolizaDetFactory.generarPolizaDet(poliza,"203", "DIVR02", false, totalAplicado, "Acredores diversos "+origen, origen, ref2, asiento+" "+ref1);*/
			
		}	
	}
	
	
	private BigDecimal getTrasladoAmex(CorteDeTarjeta corte){
		BigDecimal ingreso=BigDecimal.ZERO;
		BigDecimal comision=BigDecimal.ZERO;
		BigDecimal ivaComision=BigDecimal.ZERO;
		for(CargoAbonoPorCorte aplic:corte.getAplicaciones()){
			switch (aplic.getTipo()) {
			case COMISION_AMEX:
				comision=aplic.getCargoAbono().getImporte().abs();
				break;
			case IMPUESTO:
				ivaComision=aplic.getCargoAbono().getImporte().abs();
				break;
			case INGRESO:
				ingreso=aplic.getCargoAbono().getImporte().abs();
				break;
			default:
				break;
			}
		}
		return ingreso.subtract(ivaComision).subtract(comision);
	}
	
	

}
