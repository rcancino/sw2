package com.luxsoft.sw3.contabilidad.polizas.gastos;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_DiferenciaEnGastoVsFactura implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		CargoAbono pago=(CargoAbono)model.get("pago");
		String asiento="PAGO_DIFERENCIAS";
		CantidadMonetaria gasto=CantidadMonetaria.pesos(0);
		//CantidadMonetaria iva=CantidadMonetaria.pesos(0);
		Requisicion requisicion=pago.getRequisicion();	
		if(requisicion==null) return;
		for(RequisicionDe det:requisicion.getPartidas()){
			GFacturaPorCompra fac=det.getFacturaDeGasto();
			if(fac==null)
				continue;
			//if(!DateUtil.isSameMonth(pago.getFecha(), fac.getFecha())){
				//continue;
			//}
			for(GCompraDet compraDet:fac.getCompra().getPartidas()){
				gasto=gasto.add(compraDet.getImporteMN()
						.add(compraDet.getImpuestoMN())
						.subtract(compraDet.getRetencion1MN())
						.subtract(compraDet.getRetencion2MN())
						);
				//iva=iva.add(compraDet.getImpuestoMN());
				
			}			
		}
		
		//gasto=gasto.add(iva);
		if(gasto.amount().doubleValue()==0)
			return;
		
		CantidadMonetaria diferencia=pago.getImporteMN().abs().subtract(gasto);
		//System.out.println("Pago: "+pago.getId()+ " Imp: "+pago.getImporteMN()+ "Gasto :"+gasto);
		if(diferencia.amount().doubleValue()>0){
			PolizaDetFactory.generarPolizaDet(poliza, "704", "OGST01", true, diferencia.amount().abs(),"Otros gastos x gasto", pago.getAFavor()
					, "OFICINAS", asiento);
		}else if(diferencia.amount().doubleValue()<0){
			PolizaDetFactory.generarPolizaDet(poliza, "702", "OING01", false, diferencia.amount().abs(),"Otros ingresos x gasto", pago.getAFavor()
					, "OFICINAS", asiento);
		}
	}
	
	
	
		
	

}
