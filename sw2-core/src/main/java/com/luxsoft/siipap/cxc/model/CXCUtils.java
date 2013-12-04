package com.luxsoft.siipap.cxc.model;

import java.util.Calendar;
import java.util.Date;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.DateUtil;

public class CXCUtils {
	
	/**
	 * Calcula el saldo del cargo al corte indicado
	 * 
	 * @param cargo
	 * @param corte
	 * @return
	 */
	public static CantidadMonetaria getSaldoAlCorte(Cargo cargo,Date corte){
		
		corte=DateUtil.truncate(corte, Calendar.DATE);
		CantidadMonetaria saldo=cargo.getTotalCM();
		for(Aplicacion a:cargo.getAplicaciones()){
			Date fecha=DateUtil.truncate(a.getFecha(), Calendar.DATE);
			if(fecha.compareTo(corte)<=0){
				CantidadMonetaria aplicado=new CantidadMonetaria(a.getImporte(),cargo.getMoneda());
				saldo=saldo.subtract(aplicado);
			}	
		}
		return saldo;
	}
	
	
	
	
	/**
	 * Calcula el disponible del abono al corte indicado
	 * 
	 * @param abono
	 * @param corte
	 * @return
	 */
	public static CantidadMonetaria getDisponibleAlCorte(Abono abono,Date corte){
		
		corte=DateUtil.truncate(corte, Calendar.DATE);
		CantidadMonetaria disponible=abono.getTotalCM();
		for(Aplicacion a:abono.getAplicaciones()){
			if(a==null ){
				System.out.println("ODD abono nulo "+abono.getId());
				continue;				
			}
			Date fecha=DateUtil.truncate(a.getFecha(), Calendar.DATE);
			if(fecha.compareTo(corte)<=0){
				CantidadMonetaria aplicado=new CantidadMonetaria(a.getImporte(),abono.getMoneda());
				disponible=disponible.subtract(aplicado);
			}	
		}
		return disponible;
	}
	
		

}
