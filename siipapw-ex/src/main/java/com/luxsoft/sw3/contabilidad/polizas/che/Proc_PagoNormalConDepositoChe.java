package com.luxsoft.sw3.contabilidad.polizas.che;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;
import com.luxsoft.utils.LoggerHelper;

public class Proc_PagoNormalConDepositoChe implements IProcesador{
	
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
					return DateUtil.isSameMonth(pago.getPrimeraAplicacion(), pago.getFechaDeposito());
				}
			}
		}
		return false;
	}

	/**
	 *  Genera un asiento con los siguientes registros
	 *  
	 *  - Abono a clientes camioneta
	 *  - Cargo a bancos
	 *  - Cargo a Iva en Ventas x trasladar
	 *  - Abono a Iva en Ventas
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
		String asiento="COBRANZA DEPOSITO";
		
		BigDecimal totalAbono=deposito.getTotal();
		BigDecimal importeAbono=PolizaUtils.calcularImporteDelTotal(totalAbono);		
		BigDecimal impuestoAbono=PolizaUtils.calcularImpuesto(importeAbono);
		
		importeAbono=PolizaUtils.redondear(importeAbono);
		impuestoAbono=PolizaUtils.redondear(impuestoAbono);
		
		List<Aplicacion> aplicaciones=deposito.getAplicaciones();
		BigDecimal aplFac=BigDecimal.ZERO;
		BigDecimal aplCar=BigDecimal.ZERO;
		if(!deposito.getAplicaciones().isEmpty()){
			for(Aplicacion ap:aplicaciones){
				if(ap.getCargo() instanceof NotaDeCargo){
					aplCar=aplCar.add(ap.getImporte());
				}
				if(ap.getCargo() instanceof Venta){
					aplFac=aplFac.add(ap.getImporte());
				}
			}
		}
		
		System.out.println("importe Fac: "+aplFac +" importe Car: "+aplCar);
		
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
		PolizaDetFactory.generarPolizaDet(poliza, "113",deposito.getClave(), false, totalAplicado, MessageFormat.format(desc2, deposito.getFechaDeposito(),deposito.getPrimeraAplicacion()), ref1, ref2, asiento);
		//Cargo Bancos
		PolizaDetFactory.generarPolizaDet(poliza, "102",deposito.getCuenta().getNumero().toString(), true, deposito.getTotal(),desc2 , ref1, ref2, asiento);
		
		//Cargo Iva en ventas por trasladar
		PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", true, ivaAplicado, desc2, ref1, ref2, asiento);
		//Abono Iva en ventas
		PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", false, ivaAplicado, desc2, ref1, ref2, asiento);
		
		//IETU Che Dev
		PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU03", true, importeAplicado, "ACUMULABLE IETU ", ref1, ref2, asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA03", false, importeAplicado,"IETU ACUMULABLE ", ref1, ref2, asiento);
		
		PolizaDetFactory.generarOtrosIngresos(poliza, deposito, ref1, asiento);
		
		PolizaDetFactory.generarSaldoAFavor(poliza, deposito, ref1, asiento);
		
		
	}

}
