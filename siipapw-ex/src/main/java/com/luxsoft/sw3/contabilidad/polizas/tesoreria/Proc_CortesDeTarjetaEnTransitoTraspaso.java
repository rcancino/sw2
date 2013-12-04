package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

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

public class Proc_CortesDeTarjetaEnTransitoTraspaso implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		
		String asiento="COBRANZA TARJETA TRANSITO TRASPASO";
		String ref1="MOS";
		
		List<CorteDeTarjeta> cortes=(List<CorteDeTarjeta>)model.get("cortes");
		for(CorteDeTarjeta corte:cortes){
			String desc2=MessageFormat.format("Corte - {0}",corte.getTipoDeTarjeta()+ " Folio: "+corte.getId());
			if(corte.getTipoDeTarjeta().startsWith("AMEX")){
				//Cargo a AMEX				
	//			PolizaDetFactory.generarPolizaDet(poliza,"109","DEUD01", true, corte.getTotal(), desc2,ref1, corte.getSucursal().getNombre(),asiento+" "+ref1);
				
				// Abon a AMEX (Para traspaso a banco= 109 = MontoTrasladable+Comision_AMEX+Impuesto AMEX)
				BigDecimal montoTrasladable=this.getTrasladoAmex(corte);
				PolizaDetFactory.generarPolizaDet(poliza,"109","DEUD01", false, corte.getTotal(), desc2, ref1, corte.getSucursal().getNombre(),asiento);
				PolizaDetFactory.generarPolizaDet(poliza,"102",corte.getCuenta().getNumero().toString(), true, montoTrasladable, desc2, ref1, corte.getSucursal().getNombre(),asiento);
				
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
