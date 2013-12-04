package com.luxsoft.siipap.gastos.operaciones;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.util.MonedasUtils;

public class RecepcionDeFacturasFormModel extends DefaultFormModel{

	public RecepcionDeFacturasFormModel() {
		super(GFacturaPorCompra.class);
	}

	public RecepcionDeFacturasFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public RecepcionDeFacturasFormModel(Object bean) {
		super(bean);
	}
	
	public GFacturaPorCompra getBean(){
		return (GFacturaPorCompra)getBaseBean();
	}
	
	protected void init(){
		getModel("total").addValueChangeListener(new ImporteHandler());
	}
	
	
	
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		
		//CantidadMonetaria saldo=getBean().getCompra().getSaldoPorRevisar();
		/*
		if(getBean().getTotal().compareTo(saldo)>0)
			support.addError("Importe", "El importe no puede ser mayor a: "+saldo);
			*/
	}



	private class ImporteHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getNewValue()!=null){
				CantidadMonetaria total=(CantidadMonetaria)evt.getNewValue();				
				getBean().setImporte(MonedasUtils.calcularImporteDelTotal(total));
				getBean().setImpuesto(MonedasUtils.calcularImpuesto(getBean().getImporte()));
				getBean().actualizarSaldo();
			}
			
		}
		
	}
	
}
