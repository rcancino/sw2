package com.luxsoft.siipap.cxc.rules;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDet;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Utilerias para Cargo
 * 
 * @author Ruben Cancino
 * TODO Faltan los Tests
 *
 */
public final class CXCUtils {
	
	
	/**
	 * Valida que la lista de cuentas por cobrar
	 * correspondan al mismo cliente
	 * 
	 * @param cxcs
	 * @return Verdadero si todas las cuentas corresponden al mismo cliente
	 *  
	 */
	public static boolean validarMismoCliente(Collection<? extends Cargo> cxcs){
		if(cxcs.isEmpty())
			return false;
		Cliente c=cxcs.iterator().next().getCliente();
		for(Cargo cx:cxcs){
			if(!cx.getCliente().equals(c))
				return false;
		}
		return true;
		
	}
	
	/** 
	 * Valida que una collection de cargos sean del mismo tipo de operacion
	 * 
	 * @param cargos
	 * @param tipo
	 * @return
	 */
	public static void validarMismoTipoDeOperacion(Collection<? extends Cargo> cargos){
		if(cargos.isEmpty()) return ;
		final OrigenDeOperacion tipo=cargos.iterator().next().getOrigen();		
		for (Cargo cargo : cargos) {
			if(!cargo.getOrigen().equals(tipo))
				throw new IllegalArgumentException("No todos los cargos pertenecen al mismo origen de operacion");
		}
	}
	
	/**
	 * Valida que la lista de cuentas por cobrar
	 * correspondan a la misma fecha revision
	 * 
	 * @param cxcs
	 * @return
	 */
	public static boolean validarMismoFechaRevision(Collection<Cargo> cxcs){
		if(cxcs.isEmpty())
			return false;
		Date pivot=cxcs.iterator().next().getFechaRevisionCxc();
		for(Cargo cx:cxcs){			
			if(!cx.getFechaRevisionCxc().equals(pivot))
				return false;
		}
		return true;
		
	}
	
	
	/**
	 * Genera una copia de las cuentas,cargando desde la base de datos
	 * 
	 * @param cxcs
	 * @return
	 */
	public static List<Cargo> obtenerCopia(final Collection<Cargo> cxcs){
		final List<Cargo> data=new ArrayList<Cargo>(cxcs.size());
		for(Cargo c:cxcs){
			data.add(ServiceLocator2.getCXCManager().getCargo(c.getId()));
		}
		return data;
		
	}
	
	/**
	 * Filtra una coleccion de cargos para mantener solo los que tienen saldo
	 * 
	 * @param cargos
	 */
	public static void seleccionarConSaldo(final Collection<Cargo> cargos){
		CollectionUtils.filter(cargos, new Predicate(){
			public boolean evaluate(Object object) {
				Cargo c=(Cargo)object;
				return c.getSaldoCalculado().doubleValue()>0;
			}
		});
	}
	
	/**
	 * Sincroniza los elementos de source en el event list target
	 * 
	 * @param source
	 * @param target
	 */
	public static void sincronizar(final List source,final EventList target){
		for(Object bean:source){
			int index=target.indexOf(bean);
			if(index!=-1){
				target.set(index,bean);
			}else{
				target.add(bean);
			}
		}
	}
	
	/**
	 * Genera una nota de cargo a partir de una venta
	 * 
	 * @param v
	 * @return
	 */
	public static NotaDeCargo generaNotaDeCargo(Venta v){
		Cargo target=new NotaDeCargo();
		Cargo source=(Cargo)v;
		BeanUtils.copyProperties(source, target,new String[]{"id","version","partidas",});
		ToStringBuilder.reflectionToString(target, ToStringStyle.MULTI_LINE_STYLE);
		return (NotaDeCargo)target;
		
	}
	
	public static CantidadMonetaria calcularSaldo(final List<Cargo> cargos){
		CantidadMonetaria saldo=CantidadMonetaria.pesos(0);
		for(Cargo c:cargos){
			saldo=saldo.add(c.getSaldoCalculadoCM());
		}
		return saldo;
	}
	
	public static void prorratearElImporte(final NotaDeCredito nota){
		BigDecimal total=BigDecimal.ZERO;
		for(Aplicacion a:nota.getAplicaciones()){
			total=total.add(a.getCargo().getSaldoCalculado());
		}
		for(Aplicacion a:nota.getAplicaciones()){
			CantidadMonetaria saldo=a.getCargo().getSaldoCalculadoCM();
			if(total.doubleValue()>0){
				double participacion=saldo.amount().doubleValue()/total.doubleValue();			
				CantidadMonetaria imp=nota.getTotalCM().multiply(participacion);
				a.setImporte(imp.amount());
			}else
				a.setImporte(BigDecimal.ZERO);
		}
	}
	
	public static void prorratearElImporteEnConceptos(final NotaDeCredito nota){
		BigDecimal total=BigDecimal.ZERO;
		for(NotaDeCreditoDet det:nota.getConceptos()){
			total=total.add(det.getVenta().getSaldoSinPagos());
		}
		for(NotaDeCreditoDet det:nota.getConceptos()){
			CantidadMonetaria saldo=det.getVenta().getSaldoSinPagosCM();
			if(total.doubleValue()>0){
				double participacion=saldo.amount().doubleValue()/total.doubleValue();			
				double imp=nota.getTotalCM().amount().doubleValue()*participacion;
				det.setImporte(new BigDecimal(imp).setScale(5, RoundingMode.HALF_EVEN));
				det.setDescuento(det.getDescuentoCalculado());
			}else
				det.setImporte(BigDecimal.ZERO);
		}
	}
	
	public static CantidadMonetaria calcularSaldoVencido(final List<Cargo> cargos){
		CantidadMonetaria vencido=CantidadMonetaria.pesos(0);
		Date ini=DateUtils.truncate(new Date(), Calendar.DATE);
		for(Cargo cargo:cargos){
			Date vto=DateUtil.truncate(cargo.getVencimiento(), Calendar.DATE);
			if(DateUtils.isSameDay(ini,cargo.getVencimiento()))
				vencido=vencido.add(cargo.getSaldoCalculadoCM());
			else if(vto.compareTo(ini)<0){
				vencido=vencido.add(cargo.getSaldoCalculadoCM());
			}
		}
		return vencido;
	}
	public static CantidadMonetaria calcularSaldoPorVencer(final List<Cargo> cargos){
		return calcularSaldo(cargos).subtract(calcularSaldoVencido(cargos));
	}
	
	public static CantidadMonetaria getSaldoVencido(final List<Cargo> cargos,final Date f1, final Date f2){
		Date ini=DateUtils.truncate(f1, Calendar.DATE);
		Date fin=DateUtil.truncate(f2, Calendar.DATE);
		CantidadMonetaria importe=CantidadMonetaria.pesos(0);
		for(Cargo c:cargos){
			Date vto=c.getVencimiento();
			if(vto.compareTo(ini)>=0)
				if(c.getVencimiento().compareTo(fin)<0)
					importe=importe.add(c.getSaldoCalculadoCM());
		}
		return importe;
	}
	
	public static CantidadMonetaria getVencido1_30(final List<Cargo> cargos){
		Date f2=new Date();
		Date f1 = DateUtils.addDays(f2, -30);
		return getSaldoVencido(cargos,f1, f2);
	}
	
	public static CantidadMonetaria getVencido31_60(final List<Cargo> cargos){
		Date f2= DateUtils.addDays(new Date(), -31);
		Date f1 = DateUtils.addDays(f2, -30);
		return getSaldoVencido(cargos,f1, f2);
	}
	
	public static CantidadMonetaria getVencido61_90(final List<Cargo> cargos){
		Date f2= DateUtils.addDays(new Date(), -61);
		Date f1 = DateUtils.addDays(f2, -30);
		return getSaldoVencido(cargos,f1, f2);
	}
	
	public static CantidadMonetaria getVencidoMasDe90(final List<Cargo> cargos){
		return calcularSaldoVencido(cargos)
		.subtract(getVencido1_30(cargos))
		.subtract(getVencido31_60(cargos))
		.subtract(getVencido61_90(cargos));
	}
	
	public static Collection<Cargo> filtrarDescuentoFinanciero(final List<Cargo> cargos){		
		return CollectionUtils.select(cargos, new Predicate(){
			public boolean evaluate(Object object) {
				Cargo c=(Cargo)object;
				return c.getDescuentoFinanciero()>0d;
			}
		});
	}
	
	
	/**** Utilizando CargoRow *****/
	
	

}
