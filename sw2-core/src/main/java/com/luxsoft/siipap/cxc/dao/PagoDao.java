package com.luxsoft.siipap.cxc.dao;

import java.util.List;

import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;

public interface PagoDao extends GenericDao<Pago, String>{
	
	
	/**
	 * Regresa una lista de los pagos disponibles para aplicacion
	 * 
	 * @param cliente
	 * @return
	 */
	public List<Pago> buscarPagosDisponibles(final Cliente cliente);
	
	/**
	 * Regresa los pagos registrados en el periodo
	 * 
	 * @param periodo
	 * @return
	 */
	public List<Pago> buscarPagos(final Periodo periodo);
	
	
	/**
	 * Regresa la lista de los pagos validos para administrar en tesoreria
	 * 
	 * @param periodo
	 * @return
	 */
	public List<Pago> buscarPagosEnTesoreria(final Periodo periodo);

}
