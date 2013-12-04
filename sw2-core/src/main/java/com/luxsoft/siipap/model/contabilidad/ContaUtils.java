package com.luxsoft.siipap.model.contabilidad;

import java.util.Calendar;
import java.util.Date;
import java.util.ListIterator;



import com.luxsoft.siipap.util.DateUtil;


/**
 * Utilerias para el sistema contable
 * 
 * @author Ruben Cancino
 *
 */
public class ContaUtils {
	
	
	public static void depurarPoliza(final Poliza p){		
		ListIterator<AsientoContable> iter=p.getRegistros().listIterator();
		while(iter.hasNext()){
			AsientoContable a=iter.next();
			if( (a.getDebe().amount().doubleValue()==0) && 
					(a.getHaber().amount().doubleValue()==0)){
				iter.remove();
			}
		}
	}
	
	/**
	 * Regresa verdadero si la fecha esta dentro del mimo mes
	 * que la fecha del sistema
	 * 
	 * @param fecha
	 * @return
	 */
	public static boolean validarPeriodoDelSistema(final Date fecha){
		return validarMismoMes(new Date(), fecha);
	}
	
	/**
	 * Valida si dos fechas se encuentran en el mismo mes - año
	 * 
	 * @param origen
	 * @param fecha
	 * @return
	 */
	public static boolean validarMismoMes(final Date origen,final Date fecha){
		Calendar c=Calendar.getInstance();
		c.setTime(origen);
		c.getTime();
		int yearActual=c.get(Calendar.YEAR);		
		int mesActual=c.get(Calendar.MONTH);
		
		c.setTime(fecha);
		c.getTime();
		int yearFecha=c.get(Calendar.YEAR);		
		int mesFecha=c.get(Calendar.MONTH);
		if(yearActual==yearFecha){
			if(mesActual==mesFecha)
				return true;
		}
		return false;
	}
	
	public static Date ultimoDiaDelPeriodoAnterior(){
		return ultimoDiaDelPeriodoAnterior(new Date());
	}
	
	/**
	 * Regresa el ultimo dia del mes anterior al mes de la fecha
	 * 
	 * @param fecha
	 * @return
	 */
	public static Date ultimoDiaDelPeriodoAnterior(final Date fecha){
		Calendar c=Calendar.getInstance();
		c.setTime(fecha);
		c.getTime();
		c.add(Calendar.MONTH, -1);
	
		c.set(Calendar.DATE, c.getActualMaximum(Calendar.DATE));
		Date res=c.getTime();
		return DateUtil.truncate(res, Calendar.DATE);
		
	}
	
	/**
	 * Regresa verdadero si la fecha indicada corresponde a un mes anterior
	 * 
	 * @param fehca
	 * @return
	 */
	public static boolean esMesAnterior(final Date fecha){
		Date f1=DateUtil.truncate(new Date(), Calendar.MONTH);
		Date f2=DateUtil.truncate(fecha, Calendar.MONTH);
		int res=f2.compareTo(f1);
		return res<0?true:false;
	}

}
