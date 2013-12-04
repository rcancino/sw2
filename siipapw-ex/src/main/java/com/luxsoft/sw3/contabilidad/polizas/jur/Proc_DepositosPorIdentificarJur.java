package com.luxsoft.sw3.contabilidad.polizas.jur;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;
import com.luxsoft.utils.LoggerHelper;

public class Proc_DepositosPorIdentificarJur implements IProcesador{
	
	Logger logger=LoggerHelper.getLogger();
	
	public void procesar(Poliza poliza, ModelMap model) {
		List<Pago> pagos=(List<Pago>)model.get("pagos");
		for(Pago pago:pagos){
			procesar(poliza,pago);
		}
	}

	boolean evaluar(Abono entidad,Poliza poliza) {
		
		if(entidad instanceof PagoConDeposito){
			PagoConDeposito pago=(PagoConDeposito)entidad;
			if(!pago.isAnticipo()){
				if(DateUtils.isSameDay(pago.getPrimeraAplicacion(), poliza.getFecha())){
					return !DateUtil.isSameMonth(pago.getPrimeraAplicacion(), pago.getFechaDeposito());
				}
			}
		}
		return false;
	}

	/**
	 *  Genera un asiento con los siguientes registros
	 *  
	 *  - Abono a clientes credito
	 *  - Cargo a acredores diversos
	 *  - Cargo a Iva en Ventas x trasladar
	 *  - Cargo a Acumulable IETU (Deposits X identificar) 902
	 *  - Abono a IETU Acumulable (Depositos por identificar) 903
	 *  - Cargo a Acumulable IETU
	 *  - Abono a IETU Acumulable
	 */
	void procesar(Poliza poliza, Abono entidad) {
		if(!evaluar(entidad,poliza))
			return;
		logger.debug("Procesando pago: "+entidad);
		PagoConDeposito deposito=(PagoConDeposito)entidad;
		
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
		
		//Abono a clientes		
		PolizaDetFactory.generarPolizaDet(poliza, "114",deposito.getClave(), false, totalAplicado, MessageFormat.format(desc2, deposito.getFechaDeposito(),deposito.getPrimeraAplicacion()), ref1, ref2, asiento);
		//Acredores diversos
		PolizaDetFactory.generarPolizaDet(poliza, "203", "DEPI01", true, importeAplicado, desc2, ref1, ref2, asiento);
		//Iva en ventas por trasladar
		PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAD01", true, ivaAplicado, desc2, ref1, ref2, asiento);
		
		//Cancelacion de IETU
		PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU04", false, importeAplicado, "ACUMULABLE IETU "+asiento, ref1, ref2, asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA04", true, importeAplicado,"IETU ACUMULABLE "+asiento, ref1, ref2, asiento);
		//IETU Credito
		PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU03", true, importeAplicado, "ACUMULABLE IETU ", ref1, ref2, asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA03", false, importeAplicado,"IETU ACUMULABLE ", ref1, ref2, asiento);
		
		PolizaDetFactory.generarOtrosIngresos(poliza, deposito, ref1, asiento);
		
		//PolizaDetFactory.generarSaldoAFavor(poliza, deposito, ref1, asiento);
		
		
	}

}
