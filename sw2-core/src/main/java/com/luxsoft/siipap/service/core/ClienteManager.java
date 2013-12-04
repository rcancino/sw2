package com.luxsoft.siipap.service.core;



import java.util.List;

import com.luxsoft.siipap.dao.core.ClienteDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.ClienteRow;
import com.luxsoft.siipap.service.GenericManager;

public interface ClienteManager extends GenericManager<Cliente, Long>{
	
	public List<Cliente> buscarClientesCredito();
	
	public Cliente buscarPorClave(final String clave);
	
	public Cliente buscarPorNombre(final String nombre);
	
	public List<ClienteRow> buscarClientes();
	
	public List<Cliente> buscarClientePorClave(final String clave);
	
	public Cliente buscarPorRfc(String rfc);

	public List<Cliente> buscarClientePorNombre(String nombre);
	
	public ClienteDao getClienteDao();
	
	public void agregarCuenta(final String cliente,final String cuenta);
	
	/**
	 * Exporta la informacion del cliente al sistema SIIPAP -DBF
	 */
	public void exportarCliente(final Cliente c);
	
	//public String buscarClaveDisponible(final Cliente c,final Sucursal suc);

}
