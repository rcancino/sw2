package com.luxsoft.siipap.cxc.ui;

import java.util.List;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.core.ClienteManager;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;



public class AbonoFormModel extends DefaultFormModel{
	
	
	private OrigenDeOperacion operacion;

	public AbonoFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}
	
	
	
	public Abono getAbono(){
		return (Abono)getBaseBean();
	}
	
	public boolean clienteModificable(){
		return getAbono().getId()==null;
	}


	public EventList<Cliente> getClientes() {
		return CXCUIServiceFacade.getClientes();
	}

	public List<String> getBancos(){
		return CXCUIServiceFacade.getBancos();
	}

	public void loadClientes(){
		CXCUIServiceFacade.relodClientes();
	}
	
	public boolean isMultiMonedaPermitido(){
		return false;
	}

	public OrigenDeOperacion getOperacion() {
		return operacion;
	}

	public void setOperacion(OrigenDeOperacion operacion) {
		this.operacion = operacion;
	}
	
	/**
	 * Template method para cuando cambia el cliente
	 * Por la forma en que funciona el binding actual de Clientes
	 * este metodo es para ser usado desde la forma, de lo contrario 
	 * se llamado en demasiadas ocaciones
	 * 
	 */
	public void clienteChanged(){
		
	}
	
	public Cliente getCurrentCliente(){
		return getAbono().getCliente();
	}
	
	public ClienteManager getClienteManager(){
		return ServiceLocator2.getClienteManager();
	}
	
	

}
