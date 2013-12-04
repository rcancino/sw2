package com.luxsoft.siipap.cxc.service;

import java.util.Date;

import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.ventas.model.Devolucion;

public interface NotaDeCreditoManager {
	
	
	public NotaDeCreditoDevolucion[] generarNotaDeDevolucion(final Devolucion rmd,final Date fecha,final int folio);

}
