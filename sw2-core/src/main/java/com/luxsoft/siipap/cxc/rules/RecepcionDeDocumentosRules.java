package com.luxsoft.siipap.cxc.rules;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.cxc.model.Cargo;

/**
 * BusinessRules relacionadas con la recepcion de documentos 
 * 
 * @author Ruben Cancino
 *
 */
public class RecepcionDeDocumentosRules {
	
	private static RecepcionDeDocumentosRules INSTANCE;
	
	private RecepcionDeDocumentosRules(){
		
	}
	
	/**
	 * Actualiza los datos realcionadso con la recepcion de documentos
	 * por parte del departemento de cunetas por cobrar
	 * 
	 * @param cuenta
	 * @param fecha
	 */
	public void recibir(final Cargo cuenta,final Date fecha){
		cuenta.setFechaRecepcionCXC(fecha);
	}
	
	/**
	 * Cancela la recepción de facturas por el departamento de CxC
	 *  
	 * @param cuentas
	 */
	public void cancelarRecepcion(final List<Cargo> cuentas){
		for(Cargo c:cuentas){
			if(!c.isRevisada())
				c.setFechaRecepcionCXC(null);
		}
	}
	
	public void cancelarRecepcion(final Cargo c){
		if(!c.isRevisada())
			c.setFechaRecepcionCXC(null);
	}
	
	public static RecepcionDeDocumentosRules instance(){
		if(INSTANCE==null){
			INSTANCE=new RecepcionDeDocumentosRules();
		}
		return INSTANCE;
	}
	
	/**
	 * Valida que las cuentas por cobrar no esten recibidas por el dpto
	 * 
	 * @param cuentas
	 * @return
	 */
	public boolean validarSinRecibirCXC(final List<Cargo> cuentas){
		for(Cargo c:cuentas){
			if(c.getFechaRecepcionCXC()!=null)
				return false;
		}
		return true;
	}
	
	public DateFormat df=new SimpleDateFormat("dd/MM/yyyy");
	
	/**
	 * Valida que a los documentos indicados se les pueda asignar la fecha de recepcion Cargo 
	 * 
	 * @param cuentas
	 * @param fecha
	 * @throws IllegalArgumentException
	 */
	public void validar(List<Cargo> cuentas,final Date fecha)throws IllegalArgumentException{
		for(Cargo c:cuentas){
			if(fecha.getTime()<=c.getFecha().getTime()){
				String pattern="La fecha del documento {0} es posteriro a la fecha designada:{1}";
				throw new IllegalArgumentException(MessageFormat.format(pattern, c.getDocumento(),df.format(fecha)));
			}
		}
	}

}
