package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

/**
 * Centraliza las reglas de negocios para la generacion de notas
 * de credito
 * 
 * TODO Cleanup a partir de la refactorizacion
 * 
 * @author Ruben Cancino
 *
 */
public class AbstractNotasRules implements NotaRules {
	
	public static final MathContext mtx=new MathContext(6,RoundingMode.HALF_EVEN);
	
	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.cxc.model.NotasRules#actualizarImportes(com.luxsoft.siipap.cxc.model.NotaDeCredito)
	 */
	public void actualizarImportes(final NotaDeCredito notaCredito){
		
	}
	
	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.cxc.model.NotasRules#aplicarDescuento(com.luxsoft.siipap.cxc.model.NotaDeCredito)
	 */
	public void aplicarDescuento(final NotaDeCredito nota){
		if(nota.getDescuento()>0){
			for(Aplicacion a:nota.getAplicaciones()){
				BigDecimal imp=a.getCargo().getTotal();
				BigDecimal desc=new BigDecimal(nota.getDescuento());
				a.setImporte(imp.multiply(desc,mtx));
			}
		}
	}
	
	/**
	 * Prorratea el importe de la nota entre sus aplicaciones
	 * ponderando en funcion del saldo
	 * 
	 * @param nota
	 */
	public void prorratearElImporte(final NotaDeCredito nota){
		BigDecimal total=BigDecimal.ZERO;
		for(Aplicacion a:nota.getAplicaciones()){
			total=total.add(a.getCargo().getSaldoCalculado());
		}
		if(total.doubleValue()<=0)
			return;
		BigDecimal monto=nota.getTotal();
		for(Aplicacion a:nota.getAplicaciones()){
			BigDecimal saldo=a.getCargo().getSaldo();
			BigDecimal participacion=saldo.divide(total,mtx);
			BigDecimal imp=monto.multiply(participacion, mtx);
			a.setImporte(imp);
		}
	}
	
	protected static  Logger logger=Logger.getLogger(AbstractNotasRules.class);
	

	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.cxc.model.NotasRules#actualizarImportesDesdeAplicaciones(com.luxsoft.siipap.cxc.model.NotaDeCredito)
	 */
	public void actualizarImportesDesdeAplicaciones(final NotaDeCredito nota){
		BigDecimal imp=BigDecimal.ZERO;
		
		for(Aplicacion a:nota.getAplicaciones()){
			imp=imp.add(a.getImporte(),mtx);
		}
		//Aplicamos en el importe por que se requiere
		nota.setImporte(imp.divide(BigDecimal.valueOf(1.15d), mtx));
		//nota.actualizarImpuesto();
	}
	
	public static AplicacionDeNota generarAplicaionParaNotaDeBonificacion(final NotaDeCredito nota,final Cargo cuenta){
		AplicacionDeNota a=new AplicacionDeNota();
		//Buscamos si la aplicacion existe
		Aplicacion res=buscarAplicacion(cuenta, nota);
		if(res!=null){
			//Error la factura ya esta aplicada
			logger.info("Esta trantando mas de una vez a la misma cuenta por pagar con una nota de credito");
			return null;
		}
		a.setCargo(cuenta);
		nota.agregarAplicacion(a);
		return a;
	}
	
	/**
	 * Busca una  Aplicacion para la cuenta en un abono
	 * 
	 * TODO Mover a AbonoUtils
	 * 
	 * @param cuenta
	 * @param nota
	 * @return
	 */
	public static Aplicacion buscarAplicacion(final Cargo cuenta,final Abono abono){
		Object res=CollectionUtils.find(abono.getAplicaciones(), new Predicate(){
			public boolean evaluate(Object object) {
				Aplicacion aa=(Aplicacion)object;
				return cuenta.equals(aa.getCargo());
			}
			
		});

		return (Aplicacion)res;
	}

}
