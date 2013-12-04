package com.luxsoft.siipap.cxc.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.model.EsquemaPorTarjeta;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * FormModel para el pago con tarjeta de credito
 * 
 * @author Ruben Cancino
 *
 */
public class PagoConTarjetaFormModel extends PagoFormModel{
	
	
	private EventList<EsquemaPorTarjeta> esquemas=new BasicEventList<EsquemaPorTarjeta>();
	
	private ValueModel esquemaHolder=new ValueHolder(null);

	public PagoConTarjetaFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
		getModel("tarjeta").addValueChangeListener(new TarjetaHandler());
		
		esquemaHolder.addValueChangeListener(new EsquemaHandler());
	}
	
	
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		
		super.addValidation(support);
		if(getPagoConTarjeta().getCuenta()==null){
			support.getResult().addError("La cuenta destino es mandatoria");
		}
		if((getPagoConTarjeta().getSucursal()!=null )&& (getPagoConTarjeta().getSucursal().getId()==1) ){
			support.getResult().addError("La sucursal 1 (Oficinas) no es permitida para Pago con Tarjeta");
		}
	}



	public PagoConTarjeta getPagoConTarjeta(){
		return (PagoConTarjeta)getPago();
	}
	

	public EventList<EsquemaPorTarjeta> getEsquemas() {
		return esquemas;
	}
	
	
	public ValueModel getEsquemaHolder() {
		return esquemaHolder;
	}

	public List<Tarjeta> getTarjetas(){
		return getManager().buscarTarjetas(); 
	}
	

	public CXCManager getManager(){
		return ServiceLocator2.getCXCManager();
	}
	
	
	
	
	/**
	 * Detecta los cambios en el tipo de tarjeta 
	 * Resetea el esquema y actualiza la lista de esquemas 
	 * disponibles
	 * 
	 * @author Ruben Cancino
	 * TODO Esta es una regla de negocios importante que deberia estar
	 *		 localizada en algun otro lado. Pensar en un diseño tipo Flow con listeners
	 */
	private class TarjetaHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			setValue("esquema", null);
			esquemas.clear();
			Tarjeta t=(Tarjeta)evt.getNewValue();
			if(t!=null){
				for(EsquemaPorTarjeta e:t.getEsquemas()){
					esquemas.add(e);
				}
				esquemas.add(0,null);
			}			
		}		
	}
	
	/**
	 * Detecta los cambios de esquema para actualziar la comisión
	 * 
	 * @author Ruben Cancino
	 *	TODO Esta es una regla de negocios importante que deberia estar
	 *		 localizada en algun otro lado. Pensar en un diseño tipo Flow con listeners
	 */
	private class EsquemaHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			final EsquemaPorTarjeta ep=(EsquemaPorTarjeta)evt.getNewValue();
			if(ep!=null){
				setValue("comisionBancaria", ep.getComisionBancaria());
				setValue("esquema",ep.getEsquema());
			}else{
				if(getValue("tarjeta")!=null)
					setValue("comisionBancaria", getPagoConTarjeta().getTarjeta().getComisionBancaria());
				setValue("comisionBancaria", new Double(0));
				setValue("esquema",null);
			}
		}
	}
	
	
}
