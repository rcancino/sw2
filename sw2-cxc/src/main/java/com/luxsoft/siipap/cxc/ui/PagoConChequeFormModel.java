package com.luxsoft.siipap.cxc.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxc.model.PagoConCheque;

public class PagoConChequeFormModel extends PagoFormModel{

	public PagoConChequeFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);	
		getModel("cliente").addValueChangeListener(new CuentasWatcher());
		
	}
	
	public PagoConCheque getPagoConCheque(){
		return (PagoConCheque)getPago();
	}
	
	private boolean chequePostFechado=false;
	
	public boolean permitirPostFechado(){
		return chequePostFechado;
	}
	
	private EventList<String> cuentas=new BasicEventList<String>();
	
	public EventList<String> getCuentas() {
		return cuentas;
	}
	

	@Override
	public void clienteChanged() {
		//logger.info("Agregando cuentas disponibles para el cliente");
		super.clienteChanged();
		if(getCurrentCliente()!=null){
			cuentas.addAll(getClienteManager().getClienteDao().buscarCuentasRegistradas(getCurrentCliente().getClave()));
			if(getCurrentCliente().getCredito()!=null){
				setValue("postFechado", getCurrentCliente().getCredito().isChequePostfechado());
			}
		}
	}

	/**
	 * Detecta cambios en el cliente para limpiar las cuentas del mismo
	 * 
	 * @author Ruben Cancino
	 *
	 */	
	private class CuentasWatcher implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			cuentas.clear();
		}
	}
	
	

}
