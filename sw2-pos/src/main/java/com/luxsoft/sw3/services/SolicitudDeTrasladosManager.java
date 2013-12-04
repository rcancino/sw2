package com.luxsoft.sw3.services;

import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;

import com.luxsoft.siipap.service.GenericManager;

public interface SolicitudDeTrasladosManager extends GenericManager<SolicitudDeTraslado, String>{
	
	public SolicitudDeTraslado cancelar(final SolicitudDeTraslado sol);

}
