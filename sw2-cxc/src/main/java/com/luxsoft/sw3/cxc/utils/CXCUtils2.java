package com.luxsoft.sw3.cxc.utils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.cxc.consultas.CargoRow2;

public class CXCUtils2 {
	
	
	public static CantidadMonetaria calcularSaldo(final List<CargoRow2> cargos){
		CantidadMonetaria saldo=CantidadMonetaria.pesos(0);
		for(CargoRow2 c:cargos){
			saldo=saldo.add(c.getSaldoMN());
		}
		return saldo;
	}
	
	public static CantidadMonetaria calcularSaldoVencido(final List<CargoRow2> cargos){
		/*CantidadMonetaria vencido=CantidadMonetaria.pesos(0);
		Date ini=DateUtils.truncate(new Date(), Calendar.DATE);
		for(CargoRow2 cargo:cargos){
			Date vto=DateUtil.truncate(cargo.getVencimiento(), Calendar.DATE);
			if(DateUtils.isSameDay(ini,cargo.getVencimiento()))
				vencido=vencido.add(cargo.getSaldoMN());
			else if(vto.compareTo(ini)<0){
				vencido=vencido.add(cargo.getSaldoMN());
			}
		}
		return vencido;*/
		BigDecimal vencido=BigDecimal.ZERO;
		for(CargoRow2 cargo:cargos){
			if(cargo.getAtraso()>0)
				vencido=vencido.add(cargo.getSaldo());
		}
		return CantidadMonetaria.pesos(vencido);
		
			}
	public static CantidadMonetaria calcularSaldoPorVencer(final List<CargoRow2> cargos){
		return calcularSaldo(cargos).subtract(calcularSaldoVencido(cargos));
	}
	
	public static CantidadMonetaria getSaldoVencido(final List<CargoRow2> cargos,final Date f1, final Date f2){
		Date ini=DateUtils.truncate(f1, Calendar.DATE);
		Date fin=DateUtil.truncate(f2, Calendar.DATE);
		CantidadMonetaria importe=CantidadMonetaria.pesos(0);
		for(CargoRow2 c:cargos){
			Date vto=c.getVencimiento();
			if(vto.compareTo(ini)>=0)
				if(c.getVencimiento().compareTo(fin)<0)
					importe=importe.add(c.getSaldoMN());
		}
		return importe;
	}
	
	public static CantidadMonetaria getVencido1_30(final List<CargoRow2> cargos){
		/*Date f2=new Date();
		Date f1 = DateUtils.addDays(f2, -30);
		return getSaldoVencido(cargos,f1, f2);*/
		BigDecimal vencido=BigDecimal.ZERO;
		for(CargoRow2 cargo:cargos){
			if(cargo.getAtraso()>0)
				if(cargo.getAtraso()<=30)
					vencido=vencido.add(cargo.getSaldo());
		}
		return CantidadMonetaria.pesos(vencido);
	}
	
	public static CantidadMonetaria getVencido31_60(final List<CargoRow2> cargos){
		/*Date f2= DateUtils.addDays(new Date(), -31);
		Date f1 = DateUtils.addDays(f2, -30);
		return getSaldoVencido(cargos,f1, f2);*/
		
		BigDecimal vencido=BigDecimal.ZERO;
		for(CargoRow2 cargo:cargos){
			if(cargo.getAtraso()>30)
				if(cargo.getAtraso()<=60)
					vencido=vencido.add(cargo.getSaldo());
		}
		return CantidadMonetaria.pesos(vencido);
	}
	
	public static CantidadMonetaria getVencido61_90(final List<CargoRow2> cargos){
		/*	Date f2= DateUtils.addDays(new Date(), -61);
		Date f1 = DateUtils.addDays(f2, -30);
		return getSaldoVencido(cargos,f1, f2);*/
		
		BigDecimal vencido=BigDecimal.ZERO;
		for(CargoRow2 cargo:cargos){
			if(cargo.getAtraso()>60)
				if(cargo.getAtraso()<=90)
					vencido=vencido.add(cargo.getSaldo());
		}
		return CantidadMonetaria.pesos(vencido);
	}
	
	public static CantidadMonetaria getVencidoMasDe90(final List<CargoRow2> cargos){
		/*return calcularSaldoVencido(cargos)
		.subtract(getVencido1_30(cargos))
		.subtract(getVencido31_60(cargos))
		.subtract(getVencido61_90(cargos));
		*/
		BigDecimal tot=BigDecimal.ZERO;
		for(CargoRow2 row:cargos){
			if(row.getAtraso()>90)
				tot=tot.add(row.getSaldo());
		}
		return CantidadMonetaria.pesos(tot);
	}
	
	

}
