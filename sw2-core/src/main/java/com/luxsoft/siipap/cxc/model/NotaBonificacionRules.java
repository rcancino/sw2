package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;

import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion.ModeloDeCalculo;

public class NotaBonificacionRules extends AbstractNotasRules{
	
	/**
	 * Actualiza de manera adecuada los importes de la nota de credito en funcion 
	 * del tipo de bonificacion y el modo de calculo
	 * 
	 * @param nota
	 */
	@Override
	public void actualizarImportes(final NotaDeCredito notaCredito){
		NotaDeCreditoBonificacion nota=(NotaDeCreditoBonificacion)notaCredito;
		if(ModeloDeCalculo.DESCUENTO.equals(nota.getModo())){
			aplicarDescuento(nota);
			actualizarImportesDesdeAplicaciones(nota);
		}else if(ModeloDeCalculo.PRORREATAR.equals(nota.getModo())){
			prorratearElImporte(nota);
		}
	}
	
	/**
	 * Calcula el importe de las aplicaciones en funcion
	 * de el saldo sin pagos de la cuenta por pagar y el descuento global
	 * de la nota.
	 * 
	 */
	public void aplicarDescuento(final NotaDeCredito nota){
		if(nota.getDescuento()>0){
			for(Aplicacion a:nota.getAplicaciones()){
				//BigDecimal imp=a.getCxc().getSaldoSinPagos();
				BigDecimal desc=new BigDecimal(nota.getDescuento());
				//a.setImporte(imp.multiply(desc,mtx));
			}
		}
	}
	
	

}
