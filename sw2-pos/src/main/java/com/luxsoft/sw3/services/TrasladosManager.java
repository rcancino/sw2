package com.luxsoft.sw3.services;

import java.util.Date;

import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.model.Sucursal;

public interface TrasladosManager {
	
	public Traslado atender(final SolicitudDeTraslado sol,final Date fecha,final Sucursal suc);
	
	public Traslado cancelar(final Traslado t);

}
