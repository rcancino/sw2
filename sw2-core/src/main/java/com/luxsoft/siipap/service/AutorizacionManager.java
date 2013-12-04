package com.luxsoft.siipap.service;

import java.util.Date;

import com.luxsoft.siipap.model.Autorizacion;

public class AutorizacionManager {
	
	private static AutorizacionManager INSTANCE;
	
	private AutorizacionManager(){
		
	}
	
	public static synchronized AutorizacionManager getInstance(){
		if(INSTANCE==null){
			INSTANCE=new AutorizacionManager();
		}
		return INSTANCE;
	}
	
	public  Autorizacion generarAutorizacion(){
		final Autorizacion aut=new Autorizacion();
		aut.setAutorizo(LoginManager.getCurrentUser());
		aut.setFechaAutorizacion(new Date());
		return aut;
	}
	
	public void cancelarAutorizacion(final Autorizacion aut){
		ServiceLocator2.getUniversalDao().remove(Autorizacion.class, aut.getId());
	}

}
