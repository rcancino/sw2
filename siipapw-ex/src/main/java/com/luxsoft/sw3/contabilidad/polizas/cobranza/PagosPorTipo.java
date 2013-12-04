package com.luxsoft.sw3.contabilidad.polizas.cobranza;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.util.ClassUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.Matcher;

import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.cxc.model.PagoDeDiferencias;
import com.luxsoft.siipap.cxc.model.PagoEnEspecie;
import com.luxsoft.siipap.cxc.model.PagoPorCambioDeCheque;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;
import com.luxsoft.utils.LoggerHelper;

public class PagosPorTipo {
	
	Logger logger=LoggerHelper.getLogger();
	
	private EventList<Pago> source;
	private Map<String, List<? extends Pago>> pagosPorTipo;
	
	public PagosPorTipo(EventList<Pago> pagos,Date fechaAplicacion) {
		this.source=pagos;
		pagosPorTipo=GlazedLists.syncEventListToMultiMap(source, new PagosPorTipoFunction(fechaAplicacion));
	}
	
	public List<PagoConCheque> getCheques(){
		List<PagoConCheque> res=(List<PagoConCheque>) pagosPorTipo.get(ClassUtils.getShortName(PagoConCheque.class));
		return res!=null?res:ListUtils.EMPTY_LIST;
	}
	public List<PagoConDeposito> getDepositos(){
		List<PagoConDeposito> res=(List<PagoConDeposito>) pagosPorTipo.get(ClassUtils.getShortName(PagoConDeposito.class));
		return res!=null?res:ListUtils.EMPTY_LIST;
	}
	public List<PagoConEfectivo> getEfectivo(){
		List<PagoConEfectivo> res=(List<PagoConEfectivo>)pagosPorTipo.get(ClassUtils.getShortName(PagoConEfectivo.class));
		return res!=null?res:ListUtils.EMPTY_LIST;
	}
	public List<PagoConTarjeta> getTarjeta(){
		List<PagoConTarjeta> res=(List<PagoConTarjeta>)pagosPorTipo.get(ClassUtils.getShortName(PagoConTarjeta.class));
		return res!=null?res:ListUtils.EMPTY_LIST;
	}
	public List<PagoDeDiferencias> getDeDiferencias(){
		List<PagoDeDiferencias> res=(List<PagoDeDiferencias>)pagosPorTipo.get(ClassUtils.getShortName(PagoDeDiferencias.class));
		return res!=null?res:ListUtils.EMPTY_LIST;
	}
	public List<PagoEnEspecie> getEnEspecie(){
		List<PagoEnEspecie> res=(List<PagoEnEspecie>)pagosPorTipo.get(ClassUtils.getShortName(PagoEnEspecie.class));
		return res!=null?res:ListUtils.EMPTY_LIST;
	}
	public List<PagoPorCambioDeCheque> getCambioDeCheque(){
		List<PagoPorCambioDeCheque> res= (List<PagoPorCambioDeCheque>)pagosPorTipo.get(ClassUtils.getShortName(PagoPorCambioDeCheque.class));
		return res!=null?res:ListUtils.EMPTY_LIST;
	}
	public List<PagoConDeposito> getDepositosPorIdentificar(){
		List<PagoConDeposito> res=(List<PagoConDeposito>)pagosPorTipo.get("DEPOSITO_POR_IDENTIFICAR");
		return res!=null?res:ListUtils.EMPTY_LIST;
	}
	public List<Pago> getAnticipos(){
		List<Pago> res= (List<Pago>)pagosPorTipo.get("ANTICIPO");
		return res!=null?res:ListUtils.EMPTY_LIST;	
	}
	
	public List<Pago> getSaldosAFavor(){
		List<Pago> res=(List<Pago>)pagosPorTipo.get("SALDO_A_FAVOR");
		return res!=null?res:ListUtils.EMPTY_LIST;
	}
	public BigDecimal sumar(List<? extends Pago> pagos){
		BigDecimal total=BigDecimal.ZERO;
		for(Pago p:pagos){
			total=total.add(p.getTotal());
		}
		return total;
	}
	
	public BigDecimal sumar(List<? extends Pago> pagos,Date aplicado){
		BigDecimal total=BigDecimal.ZERO;
		for(Pago p:pagos){
			total=total.add(p.getAplicado(aplicado));
		}
		return total;
	}
	
	public BigDecimal sumarDisponible(List<? extends Pago> pagos,Date aplicado){
		BigDecimal total=BigDecimal.ZERO;
		for(Pago p:pagos){
			total=total.add(p.getDisponibleAlCorte(aplicado).amount());
		}
		return total;
	}
	
	public BigDecimal sumarDiferenciaDePagos(final Date fecha){
		Matcher<Pago> matcher=new Matcher<Pago>() {
			public boolean matches(Pago entidad) {	
				BigDecimal diferencia=entidad.getDiferencia();
				if(diferencia.doubleValue()>0 && DateUtils.isSameDay(entidad.getDirefenciaFecha()
						, fecha)){
					return true;
				}
				return false;
			}			
		};
		FilterList<Pago> diferencias=new FilterList<Pago>(source,matcher);
		BigDecimal importeAcumuladoNormal=BigDecimal.ZERO;
		
		for(Pago pago:diferencias){			
			BigDecimal diferencia=pago.getDiferencia();
			BigDecimal importeDiferencia=PolizaUtils.calcularImporteDelTotal(diferencia);
			importeDiferencia=PolizaUtils.redondear(importeDiferencia);			
			if(DateUtils.isSameDay(pago.getDirefenciaFecha(), pago.getPrimeraAplicacion())){				
				importeAcumuladoNormal=importeAcumuladoNormal.add(importeDiferencia);
			}
		}
		return importeAcumuladoNormal;
	}
	public BigDecimal sumarDiferenciaDeSAF(final Date fecha){
		Matcher<Pago> matcher=new Matcher<Pago>() {
			public boolean matches(Pago entidad) {	
				BigDecimal diferencia=entidad.getDiferencia();
				if(diferencia.doubleValue()>0 && DateUtils.isSameDay(entidad.getDirefenciaFecha()
						, fecha)){
					return true;
				}
				return false;
			}			
		};
		FilterList<Pago> diferencias=new FilterList<Pago>(source,matcher);
		BigDecimal importeAcumuladoNormal=BigDecimal.ZERO;
		
		for(Pago pago:diferencias){			
			BigDecimal diferencia=pago.getDiferencia();
			BigDecimal importeDiferencia=PolizaUtils.calcularImporteDelTotal(diferencia);
			importeDiferencia=PolizaUtils.redondear(importeDiferencia);			
			if(!DateUtils.isSameDay(pago.getDirefenciaFecha(), pago.getPrimeraAplicacion())){				
				importeAcumuladoNormal=importeAcumuladoNormal.add(importeDiferencia);
			}
		}
		return importeAcumuladoNormal;
	}
	
	public String toString(){
		ToStringBuilder sb=new ToStringBuilder(this,ToStringStyle.DEFAULT_STYLE);
		for(Map.Entry<String, List<? extends Pago>> entry:this.pagosPorTipo.entrySet()){
			sb.append(MessageFormat.format("{0} : {1} por un total: {2}", entry.getKey(),entry.getValue().size(),sumar(entry.getValue())));
		}
		return sb.toString();
	}
	
	public String resumenAplicado( Date fecha){
		ToStringBuilder sb=new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE);
		sb.append(MessageFormat.format("Fecha : {0,date,short}", fecha));
		for(Map.Entry<String, List<? extends Pago>> entry:this.pagosPorTipo.entrySet()){
			
			sb.append(MessageFormat.format("{0} : {1} Total Del Pago: {2}, Aplicado: {3}  Disponible: {4}"
					,entry.getKey()
					,entry.getValue().size()
					,sumar(entry.getValue())
					,sumar(entry.getValue(),fecha)
					,sumarDisponible(entry.getValue(),fecha)
					
					)
					);
		}
		sb.append("\n Ajustes de Pagos: "+sumarDiferenciaDePagos(fecha));
		sb.append("\n Ajustes de SAF: "+sumarDiferenciaDeSAF(fecha));
		return sb.toString();
	}
	
	@SuppressWarnings("rawtypes")
	private static class PagosPorTipoFunction implements FunctionList.Function{
		
		final Date fecha;
		
		public PagosPorTipoFunction(Date fecha) {			
			this.fecha = fecha;
		}

		public Object evaluate(Object sourceValue) {
			//return ((Pago)sourceValue).getTipo();
			Pago pago=(Pago)sourceValue;
			if(pago.isAnticipo())
				return "ANTICIPO";
			
				
			if(pago instanceof PagoConDeposito){
				PagoConDeposito deposito=(PagoConDeposito)pago;
				
			
				
				/*if(!(pago.getDirefenciaFecha()==null)){
					if(!DateUtils.isSameDay(pago.getDirefenciaFecha(), fecha) && !DateUtils.isSameDay(pago.getDirefenciaFecha(), pago.getPrimeraAplicacion()))
						return "SALDO_A_FAVOR";
				}*/
				
				
				System.out.println("pago:"+pago.getId());
				
				if(DateUtils.isSameDay(deposito.getPrimeraAplicacion(), fecha)){
					
			
					if( !DateUtil.isSameMonth(deposito.getPrimeraAplicacion(), deposito.getFechaDeposito()))
						return "DEPOSITO_POR_IDENTIFICAR";
				}
			}
			
				if(!DateUtils.isSameDay(fecha, pago.getPrimeraAplicacion())){
					return "SALDO_A_FAVOR";
				}
	
			
			return ClassUtils.getShortName(sourceValue.getClass());
		}
		
	}

}
