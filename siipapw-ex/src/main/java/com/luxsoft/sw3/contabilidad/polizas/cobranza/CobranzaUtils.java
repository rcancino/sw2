package com.luxsoft.sw3.contabilidad.polizas.cobranza;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;

public class CobranzaUtils {
	
	
	
	public static boolean isDepositable(Abono abono){
		return ( (abono instanceof PagoConDeposito) || 
				 (abono instanceof PagoConEfectivo) || 
				 (abono instanceof PagoConCheque));
		
	}
	
	public static boolean isDepositable(Pago pago){
		return isDepositable((Abono)pago);
		
	}
	
	public static boolean aplicaIetu(Pago pago){
		return ( (pago instanceof PagoConDeposito) || 
				 (pago instanceof PagoConEfectivo) || 
				 (pago instanceof PagoConCheque) ||
				 (pago instanceof PagoConTarjeta)
				 );
	}

}
