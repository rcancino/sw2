package com.luxsoft.sw3.cfdi;

import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.model.CFDI;

public interface IFactura {
	
	public CFDI generar(Venta venta);

}
