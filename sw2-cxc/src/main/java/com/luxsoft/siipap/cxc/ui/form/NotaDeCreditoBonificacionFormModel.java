package com.luxsoft.siipap.cxc.ui.form;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.AplicacionDeNota;
import com.luxsoft.siipap.cxc.model.NotaBonificacionRules;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion.ModeloDeCalculo;
import com.luxsoft.siipap.cxc.rules.CXCUtils;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.MonedasUtils;

public class NotaDeCreditoBonificacionFormModel extends NotaDeCreditoFormModel implements PropertyChangeListener{
	
	

	public NotaDeCreditoBonificacionFormModel(NotaDeCreditoBonificacion bean,boolean readOnly) {
		super(bean, readOnly);
		setRules(new NotaBonificacionRules());
		getModel("descuento").addValueChangeListener(this);
		getModel("total").addValueChangeListener(this);
		getModel("total").addValueChangeListener(new TotalHandler());
		getModel("modo").addValueChangeListener(new ModoHandler());
		
	}
	
	public NotaDeCreditoBonificacion getNotaBonificacion(){
		return (NotaDeCreditoBonificacion)getNota();
	}
	
	

	/**
	 * Util para determinar comportamiento en UI
	 * 
	 * @return
	 */
	public boolean isPorPorcentaje(){
		if(aplicaciones.isEmpty())
			return false;
		else
			return getNotaBonificacion().getModo().equals(ModeloDeCalculo.DESCUENTO);
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		//Validamos el comentario si no existen partidas
		if(aplicaciones.isEmpty()){
			String comentario=getAbono().getComentario();
			if(StringUtils.isBlank(comentario)){
				support.addError("", "Sin aplicaciones se requiere el comentario");
			}
		}
	}

	@Override
	public void actualizar(){
		logger.info("Aplicando business rules para la nota:"+getNota());
		ModeloDeCalculo m=getNotaBonificacion().getModo();
		if(m.equals(ModeloDeCalculo.DESCUENTO))
			actualizarConDescuento();
		else
			CXCUtils.prorratearElImporte(getNotaBonificacion());
		super.actualizar();		
	}
	
	private void actualizarConDescuento(){
		double descuento=getNotaBonificacion().getDescuento();
		BigDecimal total=BigDecimal.ZERO;
		for(Aplicacion a:aplicaciones){			
			CantidadMonetaria importe=a.getCargo().getSaldoSinPagosCM();
			importe=importe.multiply(descuento);
			a.setImporte(importe.amount());
			total=total.add(a.getImporte());
		}
		setValue("total", total);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if(!aplicaciones.isEmpty())
			actualizar();
		
	}	
	
	
	/**
	 * Solo detecta el cambio en el total para acualizar el importe
	 * y el impuesto
	 *  
	 * @author Ruben Cancino Ramos
	 *
	 */
	private class TotalHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {			
			BigDecimal tot=getAbono().getTotal();
			if(tot==null)
				tot=BigDecimal.ZERO;
			BigDecimal importe=MonedasUtils.calcularImporteDelTotal(tot);
			getAbono().setImporte(importe);
			getAbono().setImpuesto(MonedasUtils.calcularImpuesto(importe));
			
		}
		
	}
	
	private class ModoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			setValue("descuento", new Double(0));
			setValue("total", BigDecimal.ZERO);
			
			
		}
		
	}

}
