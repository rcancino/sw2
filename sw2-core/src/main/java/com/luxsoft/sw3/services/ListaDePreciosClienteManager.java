package com.luxsoft.sw3.services;




import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.GenericManager;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;

/**
 * Service Manager para el mantenimiento de listas de precios por cliente
 * 
 * @author Ruben Cancino
 *
 */
public interface ListaDePreciosClienteManager extends GenericManager<ListaDePreciosCliente, Long>{
	
	
	public ListaDePreciosCliente copiarListaDePrecios(final Long id,Cliente c);
	
}
