package com.luxsoft.sw3.contabilidad.polizas.ventas;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_DepositosPorIdentificarMostrador implements IProcesador{
	
	
	private boolean evaluar(Pago entidad,Poliza poliza) {		
		if(entidad instanceof PagoConDeposito){
			PagoConDeposito pago=(PagoConDeposito)entidad;
			if(pago.getOrigenAplicacion().equals("MOS")){
				if(!pago.isAnticipo()){
					if(DateUtils.isSameDay(pago.getPrimeraAplicacion(), poliza.getFecha())){
						return !DateUtil.isSameMonth(pago.getPrimeraAplicacion(), pago.getFechaDeposito());
					}
				}
			}
		}
		return false;
	}

	public void procesar(Poliza poliza, ModelMap model) {
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");
		if(pagos==null) return;
		for(Pago pago:pagos){
			if(evaluar(pago, poliza)){
				PagoConDeposito deposito=(PagoConDeposito)pago;
				
				String desc2=MessageFormat.format(" {0} Ref:{1}  F.D:"
						, deposito.getBanco(),deposito.getReferenciaBancaria())
						+new SimpleDateFormat("dd/MM/yyyy").format(deposito.getFechaDeposito());
				String ref1=deposito.getOrigenAplicacion();
				String ref2=deposito.getSucursal().getNombre();
				String asiento="POR IDENTIFICAR";
				
				BigDecimal totalAbono=deposito.getTotal();
				BigDecimal importeAbono=PolizaUtils.calcularImporteDelTotal(totalAbono);		
				BigDecimal impuestoAbono=PolizaUtils.calcularImpuesto(importeAbono);
				
				importeAbono=PolizaUtils.redondear(importeAbono);
				impuestoAbono=PolizaUtils.redondear(impuestoAbono);
				
				
				BigDecimal totalAplicado=deposito.getAplicado(poliza.getFecha());
				BigDecimal importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);
				BigDecimal ivaAplicado=PolizaUtils.calcularImpuesto(importeAplicado);
				totalAplicado=PolizaUtils.redondear(totalAplicado);
				importeAplicado=PolizaUtils.redondear(importeAplicado);
				ivaAplicado=PolizaUtils.redondear(ivaAplicado);
				
				if(Periodo.obtenerYear(deposito.getFechaDeposito())!=Periodo.obtenerYear(poliza.getFecha())){
					desc2+=" ERROR EN AÑO";
				}
				
				
				//Acredores diversos
				PolizaDetFactory.generarPolizaDet(poliza, "203", "DEPI01", true, importeAplicado, desc2, ref1, ref2, asiento+" "+pago.getOrigenAplicacion());
				//Iva en ventas por trasladar
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAD01", true, ivaAplicado, desc2, ref1, ref2, asiento+" "+pago.getOrigenAplicacion());
				
				
				PolizaDetFactory.generarOtrosIngresos(poliza, deposito, ref1, asiento);
				
	//			PolizaDetFactory.generarSaldoAFavor(poliza, deposito, ref1, asiento);
			}
		}
		
	}

}
