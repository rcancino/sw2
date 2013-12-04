package com.luxsoft.siipap.cxp.model;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.util.DateUtil;

public class CXPUtils {
	
	/**
	 * Calcula el saldo del cargo al corte indicado
	 * 
	 * @param cargo
	 * @param corte
	 * @return
	 */
	public static CantidadMonetaria getSaldoAlCorte(CXPCargo cargo,Date corte){
		
		corte=DateUtil.truncate(corte, Calendar.DATE);
		CantidadMonetaria saldo=cargo.getTotalCM();
		for(CXPAplicacion a:cargo.getAplicaciones()){
			Date fecha=DateUtil.truncate(a.getFecha(), Calendar.DATE);
			if(fecha.compareTo(corte)<=0){
				CantidadMonetaria aplicado=new CantidadMonetaria(a.getImporte(),cargo.getMoneda());
				saldo=saldo.subtract(aplicado);
			}	
		}
		return saldo;
	}
	
	/**
	 * Calcula el saldo del cargo al corte indicado en moneda nacional
	 * 
	 * @param cargo
	 * @param corte
	 * @return
	 */
	public static CantidadMonetaria getSaldoAlCorteMN(CXPCargo cargo,Date corte){
		
		corte=DateUtil.truncate(corte, Calendar.DATE);
		CantidadMonetaria saldo=cargo.getTotalMN();
		for(CXPAplicacion a:cargo.getAplicaciones()){
			Date fecha=DateUtil.truncate(a.getFecha(), Calendar.DATE);
			if(fecha.compareTo(corte)<=0){
				CantidadMonetaria aplicado=CantidadMonetaria.pesos(a.getImporteMN().doubleValue());
				saldo=saldo.subtract(aplicado);
			}	
		}
		if( (cargo.getDiferenciaFecha()!=null) && (corte.compareTo(cargo.getDiferenciaFecha())>0) ){
			saldo=saldo.subtract(cargo.getDiferenciaMN());
		}
		return saldo;
	}
	
	/**
	 * 
	 * @param cargo
	 * @param corte
	 * @return
	 */
	public static CantidadMonetaria getSaldoAnalizadoAlCorte(CXPFactura cargo,Date corte){
		
		corte=DateUtil.truncate(corte, Calendar.DATE);
		CantidadMonetaria saldo=cargo.getTotalAnalisis();
		if(saldo.amount().doubleValue()==0)
			return saldo;
		saldo=saldo.subtract(cargo.getImporteDescuentoFinanciero());
		saldo=saldo.subtract(cargo.getBonificadoCM());
		
		for(CXPAplicacion a:cargo.getAplicaciones()){
			if(a.getAbono() instanceof CXPPago){
				Date fecha=DateUtil.truncate(a.getFecha(), Calendar.DATE);
				if(fecha.compareTo(corte)<=0){
					CantidadMonetaria aplicado=new CantidadMonetaria(a.getImporte(),cargo.getMoneda());
					saldo=saldo.subtract(aplicado);
				}
			}	
		}
		if(saldo.amount().doubleValue()<0)
			saldo=new CantidadMonetaria(0d,cargo.getMoneda());
		return saldo;
	}
	
	/**
	 * 
	 * @param cargo
	 * @param corte
	 * @return
	 */
	public static CantidadMonetaria getSaldoAnalizadoAlCorteMN(CXPFactura cargo,Date corte){
		
		corte=DateUtil.truncate(corte, Calendar.DATE);
		CantidadMonetaria saldo=cargo.getTotalAnalisisMN();
		if(saldo.amount().doubleValue()==0)
			return saldo;
		//Importe descuento financiero
		CantidadMonetaria impDf=CantidadMonetaria.pesos(cargo.getImporteDescuentoFinanciero().amount());
		impDf=impDf.multiply(cargo.getTc());		
		saldo=saldo.subtract(impDf);
		CantidadMonetaria impBon=CantidadMonetaria.pesos(cargo.getBonificado());
		impBon=impBon.multiply(cargo.getTc());
		saldo=saldo.subtract(impBon);
		
		for(CXPAplicacion a:cargo.getAplicaciones()){
			if(a.getAbono() instanceof CXPPago){
				Date fecha=DateUtil.truncate(a.getFecha(), Calendar.DATE);
				if(fecha.compareTo(corte)<=0){
					BigDecimal imp=a.getImporteMN();
					CantidadMonetaria aplicado=CantidadMonetaria.pesos(imp);//new CantidadMonetaria(a.getImporte(),cargo.getMoneda());
					saldo=saldo.subtract(aplicado);
				}
			}	
		}
		if(saldo.amount().doubleValue()<0)
			saldo=CantidadMonetaria.pesos(0);
		return saldo;
	}
	
	
	/**
	 * Calcula el saldo del cargo al corte indicado
	 * 
	 * @param abono
	 * @param corte
	 * @return
	 */
	public static CantidadMonetaria getDisponibleAlCorte(CXPAbono abono,Date corte){
		
		corte=DateUtil.truncate(corte, Calendar.DATE);
		CantidadMonetaria disponible=abono.getTotalCM();
		for(CXPAplicacion a:abono.getAplicaciones()){
			Date fecha=DateUtil.truncate(a.getFecha(), Calendar.DATE);
			if(fecha.compareTo(corte)<=0){
				CantidadMonetaria aplicado=new CantidadMonetaria(a.getImporte(),abono.getMoneda());
				disponible=disponible.subtract(aplicado);
			}	
		}
		return disponible;
	}
	
	public static Date calcularVencimiento(final Date revision,final Date fechaDocumento,final Proveedor p){
		Date fecha=p.getVtoFechaRevision()?revision:fechaDocumento;
		fecha=DateUtil.truncate(fecha, Calendar.DATE);
		return DateUtils.addDays(fecha, p.getPlazo());
	}
	
	public static Date calcularVencimientoDescuentoF(final Date revision,final Date fechaDocumento,final Proveedor p){
		Date fecha=p.getVtoFechaRevision()?revision:fechaDocumento;
		fecha=DateUtil.truncate(fecha, Calendar.DATE);
		return DateUtils.addDays(fecha, p.getDiasDescuentoF());
	}
	
	
	
	

}
