package com.luxsoft.siipap.cxc.service;

import java.util.Date;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.AutorizacionDeAbono;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;

public class CXCAutorizaciones {
	
	public static AutorizacionDeAbono paraCancelarAbono(final Abono abono){
		
		AutorizacionDeAbono aut=new AutorizacionDeAbono();
		if(KernellSecurity.instance().isSecurityEnabled()){
			aut.setAutorizo(KernellSecurity.instance().getCurrentUserName());
		}else{
			aut.setAutorizo("ADMIN PRUEBAS");
		}
		aut.setFechaAutorizacion(ServiceLocator2.obtenerFechaDelSistema());
		aut.setFechaAutorizacion(new Date());
		aut.setComentario("CANCELACION DE ABONO");
		aut.setIpAdress(KernellSecurity.getIPAdress());
		aut.setMacAdress(KernellSecurity.getMacAdress());
		return aut;
	}
	
	

}
