package com.luxsoft.siipap.tesoreria.movimientos;

import com.jgoodies.binding.PresentationModel;
import com.luxsoft.siipap.model.tesoreria.EstadoDeCuenta;
import com.luxsoft.siipap.service.ServiceLocator2;

public class EstadoDeCuentaModel extends PresentationModel{
	
	
	public EstadoDeCuentaModel(){
		super(new EstadoDeCuenta());		
	}
	
	public EstadoDeCuenta getEstadoDeCuenta(){
		return (EstadoDeCuenta)getBean();
	}
	
	public void generarEstado(){		 
		if( isValid()){
			ServiceLocator2.getCargoAbonoDao().generarEstadoDeCuenta(getEstadoDeCuenta());
		}
	}

	public boolean isValid(){
		return ( (getValue("cuenta")!=null) && (getValue("fechaInicial")!=null) && (getValue("fechaFinal")!=null));
	}
	
	/*
	private class SaldoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			firePropertyChange("saldoFinal", 0, getEstadoDeCuenta().getSaldoFinal());
		}
	}
	*/

}
