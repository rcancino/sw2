package com.luxsoft.siipap.tesoreria.procesos;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Currency;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;

public class PagoDeRequisicionFormModel extends DefaultFormModel{
	
	private final ValueHolder cuentaHolder;
	private final ValueHolder fechaPagoHolder;
	private final ValueHolder comentarioHolder;
	private final ValueHolder referenciaHolder;
	
	public PagoDeRequisicionFormModel(final Requisicion req){		
		super(req);
		LocalHandler handler=new LocalHandler();
		cuentaHolder=new ValueHolder();
		cuentaHolder.addValueChangeListener(handler);
		cuentaHolder.addValueChangeListener(new CuentaHandler());
		
		comentarioHolder=new ValueHolder();
		comentarioHolder.addValueChangeListener(handler);
		
		fechaPagoHolder=new ValueHolder(req.getFechaDePago());
		fechaPagoHolder.addValueChangeListener(handler);
		
		referenciaHolder=new ValueHolder();
		referenciaHolder.addValueChangeListener(handler);
	}
	
	
	public Requisicion getRequisicion(){
		return (Requisicion)getBaseBean();
	}
	
	public ValueHolder getCuentaHolder(){
		return cuentaHolder;
	}

	/**
	 * @return the comentarioHolder
	 */
	public ValueHolder getComentarioHolder() {
		return comentarioHolder;
	}

	/**
	 * @return the fechaPagoHolder
	 */
	public ValueHolder getFechaPagoHolder() {
		return fechaPagoHolder;
	}
	
	public ValueHolder getReferenciaHolder(){
		return referenciaHolder;
	}
	
	
	
	public void aplicarPago(){
		final Cuenta cta=(Cuenta)cuentaHolder.getValue();
		final Date fecha=(Date)fechaPagoHolder.getValue();		
		final String s=comentarioHolder.getValue()!=null?comentarioHolder.getValue().toString():"";
		final String ref=referenciaHolder.getValue().toString();
		getRequisicion().registrarPagoDeGastos(cta, fecha, s,ref);
	}

	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if( (getReferenciaHolder().getValue()==null) || 
				(StringUtils.isBlank(referenciaHolder.toString()))){
			support.addError("Referencia", "La referencia (Cheque/Transferencia) es obligatorio");
		}
		//Validar monedas
		Currency monedaPorPagar=getRequisicion().getPorPagar().currency();
		Cuenta cta=(Cuenta)cuentaHolder.getValue();
		if(cta!=null){
			//if(getRequisicion().getTipoDeCambio().intValue()<=1){
				//if(!cta.getMoneda().equals(monedaPorPagar)){
				
				//support.getResult().addError("La cuenta no es de tipo: "+monedaPorPagar);
			//}
		}
		
			
	}
	
	
	private class LocalHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			validate();
		}
	}
	
	private class CuentaHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			
			Cuenta cta=(Cuenta)evt.getNewValue();
			if(cta!=null){
				//System.out.println("Cuenta seleccionada.."+cta+ "  "+cta.getMoneda());
				referenciaHolder.setValue(nextCheque(cta.getId()));
			}
		}
	}
	
	
	private static String nextCheque(Long cuentaId){
		long val=ServiceLocator2.getRequisiciionesManager().nextCheque(cuentaId);
		return String.valueOf(val);
	}
	

}
