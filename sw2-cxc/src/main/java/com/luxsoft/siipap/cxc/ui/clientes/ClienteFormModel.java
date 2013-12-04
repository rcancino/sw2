package com.luxsoft.siipap.cxc.ui.clientes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.value.ValueHolder;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.ClienteCredito;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;

/**
 * 
 * @author Ruben Cancino
 * TODO Validar los sub modelos
 *
 */
public class ClienteFormModel extends DefaultFormModel{
	
	private PresentationModel creditoModel;
	private ValueHolder contactoChannel;
	

	public ClienteFormModel() {
		super(Cliente.class);
	}

	public ClienteFormModel(Cliente bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public ClienteFormModel(Cliente bean) {
		super(bean);
	}
	
	protected Cliente getCliente(){
		return (Cliente)getBaseBean();
	}
	public PresentationModel getCreditoModel(){
		return creditoModel;
	}
	
	protected void init(){
		pmodel.addPropertyChangeListener(PresentationModel.PROPERTYNAME_BEAN, new BeanChannelHandler());
		
		getModel("credito").addValueChangeListener(new CreditoHandler());
		initSubModels();
	}	
	
	private void initSubModels(){
		if(getCliente().getCredito()!=null)
			creditoModel=new PresentationModel(getCliente().getCredito());
		else
			creditoModel=new PresentationModel(new ClienteCredito());
		contactoChannel=new ValueHolder(null,true);
	}
	
	public ValueHolder getContactosChannel(){
		return contactoChannel;
	}
	
	public void cancelarCredito(){
		if(getCliente().getCredito()!=null){
			getCliente().getCredito().setCliente(null);
			getCliente().setCredito(null);
		}
	}
	
	
	/**
	 * Detecta cambios en el BeanChannel para actualizar los sub modelos
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class BeanChannelHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			initSubModels();
		}
		
	}
	
	private class CreditoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt){			
			if(evt.getOldValue()==null){
				getCliente().habilitarCredito();
				creditoModel.setBean(getCliente().getCredito());
			}
		}
	}

}
