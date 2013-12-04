package com.luxsoft.siipap.cxc.model;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.security.AutorizacionWindow;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.LoginManager;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.DateUtil;


/**
 * Singleton/Facade  para autorizaciones
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AutorizacionesCxC {
	
	
	public static boolean requiereAutorizacion(Abono abono){
		Date hoy=ServiceLocator2.obtenerFechaDelSistema();
		 return requiereAutorizacion(abono, hoy);
	}
	
	public static boolean requiereAutorizacion(Abono abono,Date fechaLimite){
		
		
		int diasDif=DateUtil.getDaysDiff(fechaLimite.getTime(), abono.getFecha().getTime());
		
		
		int dia=DateUtil.getDayOfWeek(abono.getFecha().getTime());
		int tolerancia=0;
		
		switch (dia) {
		case Calendar.MONDAY:
		case Calendar.TUESDAY:
		case Calendar.WEDNESDAY:
		case Calendar.THURSDAY:
		case Calendar.FRIDAY:
			tolerancia=1;
			break;
		case Calendar.SATURDAY:
			tolerancia=2;
			break;
		case Calendar.SUNDAY:
			tolerancia=1;
			break;
		default:
			break;
		}
		
		return diasDif>tolerancia; 
	}
	
	public static AutorizacionDeAplicacionCxC autorizacionParaAplicacionDeAbono(){
		return autorizacionCxC("CXC_AUTORIZA_N1");
	}
	
	/**
	 * Regresa una autorizacion para aplicar abonos 
	 * 
	 */
	public static AutorizacionDeAplicacionCxC autorizacionCxC(String role){
		
		AutorizacionWindow window=new AutorizacionWindow("Autorización Requerida");
		window.setHeaderTitle("La fecha del Pago/Abono es de otro día");
		window.setHeaderDesc("Solicite autorización para proceder");
		window.open();
		if(!window.hasBeenCanceled()){
			User u=window.getUser().getUser();
			if(u.getRole(role)!=null){
				AutorizacionDeAplicacionCxC aut=new AutorizacionDeAplicacionCxC();
				aut.setAutorizo(u.getUsername());
				aut.setComentario(window.getUser().getComentario());
				aut.setFechaAutorizacion(ServiceLocator2.obtenerFechaDelSistema());
				aut.setIpAdress(KernellSecurity.getIPAdress());
				aut.setMacAdress(KernellSecurity.getMacAdress());
				return aut;
			}else{
				MessageUtils.showMessage("El  usuario no tiene el nivel de seguridad requerido ", "Autorizaciones");
				return null;
			}
			
		}
		MessageUtils.showMessage("La operación no se puede generar sin autorización válida", "Autorizaciones");
		return null;
	}
	
	/**
	 * Autorizacion para notas de bonificacion 
	 * 
	 */
	public static AutorizacionDeAbono autorizacionDeNotaDeBonificacion(){
		final String role="CXC_AUTORIZA_N2";
		AutorizacionWindow window=new AutorizacionWindow("Autorización Requerida");
		window.setHeaderTitle("Nota por bonificación ");
		window.setHeaderDesc("Solicite autorización para proceder");
		window.open();
		if(!window.hasBeenCanceled()){
			User u=window.getUser().getUser();
			if(u.getRole(role)!=null){
				//AutorizacionDeAplicacionCxC aut=new AutorizacionDeAplicacionCxC();
				AutorizacionDeAbono aut=new AutorizacionDeAbono();
				aut.setAutorizo(u.getUsername());
				aut.setComentario(window.getUser().getComentario());
				aut.setFechaAutorizacion(ServiceLocator2.obtenerFechaDelSistema());
				aut.setIpAdress(KernellSecurity.getIPAdress());
				aut.setMacAdress(KernellSecurity.getMacAdress());
				return aut;
			}else{
				MessageUtils.showMessage("El  usuario no tiene el nivel de seguridad requerido ", "Autorizaciones");
				return null;
			}
			
		}
		MessageUtils.showMessage("La operación no se puede generar sin autorización válida", "Autorizaciones");
		return null;
	}
	
	public static void main(String[] args) {
		PagoConCheque pago=new PagoConCheque();
		pago.setFecha(DateUtil.toDate("29/08/2009"));
		
		boolean req=requiereAutorizacion(pago,DateUtil.toDate("31/08/2009"));
		System.out.println("Requiere: "+req);
		
	}

}
