package com.luxsoft.sw3.contabilidad.polizas.cxc;

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

public class Proc_PagosSAFCxC implements IProcesador{
	
	Logger logger=LoggerHelper.getLogger();
	
	public void procesar(Poliza poliza, ModelMap model) {
		List<Pago> pagos=(List<Pago>)model.get("pagosSAF");
		for(Pago pago:pagos){
		
			
			procesar(poliza,pago);
		}
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
		
		System.out.println("Pagos SAF**************************************************" + entidad.getId());
		logger.debug("Procesando pago: "+entidad);
		Pago deposito=(Pago)entidad;
		
		String ref1=deposito.getOrigen().toString();
		String ref2=deposito.getSucursal().getNombre();
		String asiento="COBRANZA CXC SAF";
		String desc2=MessageFormat.format(" {0} Ref:{1}  F.D:"
				, deposito.getBanco(),deposito.getInfo());
		
		BigDecimal totalAbono=deposito.getTotal();
		BigDecimal importeAbono=PolizaUtils.calcularImporteDelTotal(totalAbono);		
		BigDecimal impuestoAbono=PolizaUtils.calcularImpuesto(importeAbono);
		
		
		importeAbono=PolizaUtils.redondear(importeAbono);
		impuestoAbono=PolizaUtils.redondear(impuestoAbono);
		
	      if (deposito instanceof PagoConDeposito){	
	    	  PagoConDeposito deposito1 = (PagoConDeposito) deposito;
		         desc2=MessageFormat.format(" {0} Ref:{1}  F.D:"
				, deposito.getBanco(),deposito1.getReferenciaBancaria())
				+new SimpleDateFormat("dd/MM/yyyy").format(deposito1.getFechaDeposito());
				
		
		if(Periodo.obtenerYear(deposito1.getFechaDeposito())!=Periodo.obtenerYear(poliza.getFecha())){
			desc2+=" ERROR EN AO";
			 //Abono a clientes		
			PolizaDetFactory.generarPolizaDet(poliza, "102",deposito.getCuenta().getNumero().toString(), true, totalAbono, desc2, ref1, ref2, asiento);
		
		} 
	    	  
	      }
	      
	   
			//Acredores diversos
			PolizaDetFactory.generarPolizaDet(poliza, "203", "DIVR02", false, totalAbono, desc2, ref1, ref2, asiento);
			
					
			//IETU Credito
		    PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU03", true, importeAbono, "ACUMULABLE IETU ", ref1, ref2, asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA03", false, importeAbono,"IETU ACUMULABLE ", ref1, ref2, asiento);
	      
		
		
	}

}
