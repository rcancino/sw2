package com.luxsoft.siipap.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import com.luxsoft.siipap.model.CantidadMonetaria;



public class MonedasUtils {
	
	public final static Currency PESOS;
	public final static Currency DOLARES;
	public final static Currency EUROS;
	
	static{
		Locale mx=new Locale("es","mx");
		PESOS=Currency.getInstance(mx);
		DOLARES=Currency.getInstance(Locale.US);
		EUROS=Currency.getInstance("EUR");
	}
	
	public static final BigDecimal IVA=BigDecimal.valueOf(.16d);
	
	/**
	 * Calcula el impuesto nacional 
	 * @param importe
	 * @return
	 */
	public static final CantidadMonetaria calcularImpuesto(CantidadMonetaria importe){
		return importe.multiply(IVA);
	}
	
	public static final BigDecimal calcularImpuesto(BigDecimal importe){
		return importe.multiply(IVA);
	}
	
	public static final CantidadMonetaria calcularTotal(CantidadMonetaria importe){
		return importe.add(calcularImpuesto(importe));
	}
	
	public static final BigDecimal calcularTotal(BigDecimal importe){
		return importe.add(calcularImpuesto(importe));
	}
	
	public static final CantidadMonetaria aplicarDescuentosEnCascada(final CantidadMonetaria precio,List<Double> descuentos){
		return aplicarDescuentosEnCascada(precio, (Double[])descuentos.toArray(new Double[0]));
	}
	
	public static final CantidadMonetaria aplicarDescuentosEnCascada(final CantidadMonetaria precio,Double... descuentos){
		CantidadMonetaria neto=precio;
		for(Double d:descuentos){
			if(d==null)continue;
			if(d.doubleValue()>0){
				CantidadMonetaria descuento=neto.multiply(d);
				neto=neto.subtract(descuento);
			}/*else{
				neto=new CantidadMonetaria(0,precio.currency());
			}*/
		}
		return neto;
	}
	
	public static final CantidadMonetaria aplicarDescuentosEnCascadaBase100(final CantidadMonetaria precio,Double... descuentos){
		CantidadMonetaria neto=precio;
		for(Double d:descuentos){
			if(d==null)continue;
			if(d.doubleValue()>0){
				CantidadMonetaria descuento=neto.multiply(d/100);
				neto=neto.subtract(descuento);
			}/*else{
				neto=new CantidadMonetaria(0,precio.currency());
			}*/
		}
		return neto;
	}
	
	public static final CantidadMonetaria aplicarCargosEnCascadaBase100(final CantidadMonetaria precio,Double... cargos){
		CantidadMonetaria costo=precio;
		for(Double d:cargos){
			if(d==null)continue;
			if(d.doubleValue()>0){
				CantidadMonetaria cargo=costo.multiply(d/100);
				costo=costo.add(cargo);
			}
		}
		return costo;
	}
	
	public static final CantidadMonetaria aplicarDescuentosEnCascada(final CantidadMonetaria importe,BigDecimal... descuentos){
		CantidadMonetaria neto=importe;
		for(BigDecimal d:descuentos){
			if(d==null)continue;
			if(d.doubleValue()>0){
				CantidadMonetaria descuento=neto.multiply(d);
				descuento=descuento.divide(BigDecimal.valueOf(100d));
				neto=neto.subtract(descuento);
			}
		}
		return neto;
	}
	
	public static final BigDecimal aplicarDescuentosEnCascada(final BigDecimal importe,Double... descuentos){
		BigDecimal neto=importe;
		for(Double dd:descuentos){
			if(dd==null)continue;
			if(dd.doubleValue()!=0){
				BigDecimal d=BigDecimal.valueOf(dd);
				BigDecimal descuento=neto.multiply(d);
				descuento=descuento.divide(BigDecimal.valueOf(100d));
				neto=neto.subtract(descuento).setScale(2, RoundingMode.HALF_EVEN);
			}
		}
		return neto;
	}
	
	public static final CantidadMonetaria calcularImporteDelTotal(CantidadMonetaria total){
		BigDecimal val=BigDecimal.valueOf(1).add(IVA);
		CantidadMonetaria importe=total.divide(val);
		return importe;
	}
	
	public static final BigDecimal calcularImporteDelTotal(BigDecimal total){
		BigDecimal val=BigDecimal.valueOf(1).add(IVA);
		BigDecimal importe=total.divide(val,2,RoundingMode.HALF_EVEN);
		return importe;
	}
	
	public static final BigDecimal calcularImporteDelTotal(BigDecimal total,int decimales){
		BigDecimal val=BigDecimal.valueOf(1).add(IVA);
		BigDecimal importe=total.divide(val,decimales,RoundingMode.HALF_EVEN);
		return importe;
	}
	
	public static final BigDecimal calcularImpuestoDelTotal(BigDecimal total){
		//return calcularImpuesto(calcularImporteDelTotal(total));
		return calcularImpuestoDelTotal(total,2);
	}
	
	public static final BigDecimal calcularImpuestoDelTotal(BigDecimal total,int decimales){
		BigDecimal importe=calcularImporteSinIva(total);
		return importe.multiply(IVA).setScale(decimales, RoundingMode.HALF_EVEN);
	}
	
	public static final CantidadMonetaria calcularImporteConDescuentos(CantidadMonetaria precio,BigDecimal cantidad,BigDecimal[] descuentos){
		CantidadMonetaria importe=precio.multiply(cantidad);
		return aplicarDescuentosEnCascada(importe,descuentos);
	}
	
	public static final BigDecimal aplicarDescuentosEnCascadaSinRedondeo(
			final BigDecimal importe,double... descuentos){
		BigDecimal neto=importe;
		for(double dd:descuentos){			
			if(dd>0){
				BigDecimal d=BigDecimal.valueOf(dd);
				BigDecimal descuento=neto.multiply(d);
				descuento=descuento.divide(BigDecimal.valueOf(100d));
				neto=neto.subtract(descuento).setScale(6, RoundingMode.HALF_EVEN);
			}
		}
		return neto;
	}
	
	public static BigDecimal calcularImporteSinIva(BigDecimal total){
		CantidadMonetaria totalmn=CantidadMonetaria.pesos(total);
		CantidadMonetaria factor=CantidadMonetaria.pesos(1d+IVA.doubleValue());
		return totalmn.divide(factor.amount()).amount();
	}
	
	public static void main(String[] args) {
		System.out.println(aplicarDescuentosEnCascada(BigDecimal.valueOf(100000), 20d,2d));
		
	}

}
