package com.luxsoft.siipap.cxc.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.siipap.ventas.model.ListaDePreciosClienteDet;

/**
 * 
 * @author OCTAVIO
 *		
 */

public class ListaDePreciosClienteFormModel extends MasterDetailFormModel{
	
	private EventList<Cliente> clientes=new BasicEventList<Cliente>();
	public static Logger logger=Logger.getLogger(ListaDePreciosClienteFormModel.class);

	public ListaDePreciosClienteFormModel() {
		super(ListaDePreciosCliente.class);
		
	}
	
	public ListaDePreciosClienteFormModel(Object bean,boolean readOnly){
		super(bean, readOnly);
	}
	
	public ListaDePreciosClienteFormModel(Object bean) {
		super(bean);		
	}
	
	public ListaDePreciosCliente getLista(){
		return (ListaDePreciosCliente)getBaseBean();
	}
	
	public  boolean manejaTotalesEstandares(){
		return false;
	}
	
	@SuppressWarnings("unchecked")
	protected void init(){
		super.init();
		if(getLista().getId()!=null ){
			for(ListaDePreciosClienteDet det:getLista().getPrecios()){
				source.add(det);
			}
		}		
		//Handlers
		getModel("cliente").addValueChangeListener(new ClienteHandler());
	}
	
	public boolean clienteModificable(){
		return getLista().getId()==null;
	}
	
	public EventList<Cliente> getClientes() {
		return clientes;
	}

	public void setClientes(EventList<Cliente> clientes) {
		this.clientes = clientes;
	}

	public void loadClientes(){
		clientes.clear();
		clientes.addAll(ServiceLocator2.getClienteManager().getAll());
	}
	
	
	@SuppressWarnings("unchecked")
	public Object insertDetalle(final Object obj){
		ListaDePreciosClienteDet det=(ListaDePreciosClienteDet)obj;
		if(getLista().agregarPrecio(det)){
			source.add(det);
			return det;
		}else
			return null;		
	}  
	
	
	public boolean deleteDetalle(final Object obj){
		ListaDePreciosClienteDet part=(ListaDePreciosClienteDet)obj;
		boolean res=getLista().removerPrecio(part);
		if(res){
			return source.remove(part);
		}
		return false;
	}
	
	public void limpiarPartidas(){
		if(getLista().getId()==null){
			getLista().eliminarPrecios();
			source.clear();
			logger.debug("Partidas eliminadas");
		}
	}
	
	public void doListUpdated(ListEvent listChanges){
		int index=listChanges.getIndex();
		Object updated=listChanges.getSourceList().get(index);
		System.out.println("Cambio detectado desde el model en :"+updated);
	}
	
	
	
	private class ClienteHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			limpiarPartidas();			
		}		
	}
	

}
