package com.luxsoft.siipap.cxc.util;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;


import com.luxsoft.siipap.cxc.model.CargoRow;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.DateUtil;

/**
 * Diversas utilerias para el manejo de beans tipo CargoRow
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CargoRowUtils {
	
	
	public static CantidadMonetaria calcularSaldo(final Collection<CargoRow> cargos){
		CantidadMonetaria saldo=CantidadMonetaria.pesos(0);
		for(CargoRow c:cargos){
			saldo=saldo.add(c.getSaldo());
		}
		return saldo;
	}	
	
	public static CantidadMonetaria calcularSaldoVencido(final List<CargoRow> cargos){
		CantidadMonetaria vencido=CantidadMonetaria.pesos(0);
		/*Date ini=new Date();
		for(CargoRow cargo:cargos){
			Date vto=DateUtil.truncate(cargo.getVencimiento(), Calendar.DATE);
			if(DateUtils.isSameDay(ini,cargo.getVencimiento()))
				vencido=vencido.add(cargo.getSaldo());
			else if(vto.compareTo(ini)<0){
				vencido=vencido.add(cargo.getSaldo());
			}
		}*/
		for(CargoRow cargo:cargos){
			if(cargo.getAtraso()>0)
				vencido=vencido.add(cargo.getSaldo());
		}
		return vencido;
	}
	
	public static CantidadMonetaria calcularSaldoPorVencer(final List<CargoRow> cargos){
		return calcularSaldo(cargos).subtract(calcularSaldoVencido(cargos));
	}
	
	public static CantidadMonetaria getSaldoVencido(final List<CargoRow> cargos,final Date f1, final Date f2){
		Date ini=DateUtils.truncate(f1, Calendar.DATE);
		Date fin=DateUtil.truncate(f2, Calendar.DATE);
		CantidadMonetaria importe=CantidadMonetaria.pesos(0);
		for(CargoRow c:cargos){
			Date vto=c.getVencimiento();
			if(vto.compareTo(ini)>=0)
				if(c.getVencimiento().compareTo(fin)<0)
					importe=importe.add(c.getSaldo());
		}
		return importe;
	}
	
	public static CantidadMonetaria getVencido1_30(final List<CargoRow> cargos){
		CantidadMonetaria vencido=CantidadMonetaria.pesos(0);
		for(CargoRow cargo:cargos){
			if(cargo.getAtraso()>0)
				if(cargo.getAtraso()<=30)
					vencido=vencido.add(cargo.getSaldo());
		}
		return vencido;
	}
	
	public static CantidadMonetaria getVencido31_60(final List<CargoRow> cargos){
		/*Date f2= DateUtils.addDays(new Date(), -31);
		Date f1 = DateUtils.addDays(f2, -30);
		return getSaldoVencido(cargos,f1, f2);*/
		CantidadMonetaria vencido=CantidadMonetaria.pesos(0);
		for(CargoRow cargo:cargos){
			if(cargo.getAtraso()>30)
				if(cargo.getAtraso()<=60)
					vencido=vencido.add(cargo.getSaldo());
		}
		return vencido;
	}
	
	public static CantidadMonetaria getVencido61_90(final List<CargoRow> cargos){
		CantidadMonetaria vencido=CantidadMonetaria.pesos(0);
		for(CargoRow cargo:cargos){
			if(cargo.getAtraso()>60)
				if(cargo.getAtraso()<=90)
					vencido=vencido.add(cargo.getSaldo());
		}
		return vencido;
	}
	
	public static CantidadMonetaria getVencidoMasDe90(final List<CargoRow> cargos){
		/*return calcularSaldoVencido(cargos)
		.subtract(getVencido1_30(cargos))
		.subtract(getVencido31_60(cargos))
		.subtract(getVencido61_90(cargos));*/
		CantidadMonetaria tot=CantidadMonetaria.pesos(0);
		for(CargoRow row:cargos){
			if(row.getAtraso()>90)
				tot=tot.add(row.getSaldo());
		}
		return tot;
	}

}
