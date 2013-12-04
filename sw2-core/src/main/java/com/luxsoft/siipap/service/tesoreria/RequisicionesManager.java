package com.luxsoft.siipap.service.tesoreria;

import java.util.List;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.GenericManager;

/**
 * Servicios relacionados con entidades de {@link Requisicion}
 * 
 * @author Ruben Cancino
 *
 */
public interface RequisicionesManager extends GenericManager<Requisicion, Long>{
	
	
	public Requisicion registrarPago(final Requisicion req);
	
	
	public Requisicion cancelarPago(final Requisicion req);
	
	/**
	 * Cancela el pago de la requisicion desvinculandola del mismo
	 * 
	 * @param req
	 * @return
	 */
	//public Requisicion eliminarPago(final Requisicion req);
	
	/**
	 * Elimina una requisicion generada apartir de una {@link GFacturaPorCompra}
	 * 
	 * @param req
	 */
	public void eliminarRequisicionAutomatica(final Long id);
	
	/**
	 * Regresa una lista de las requisiciones generadas desde el modulo de gastos
	 * 
	 * @return
	 */
	public List<Requisicion> buscarRequisicionesDeGastos();
	
	
	/**
	 * Regresa una lista de las requisiciones generadas desde el modulo de gastos
	 * en el periodo indicado
	 * 
	 * @param p
	 * @return
	 */
	public List<Requisicion> buscarRequisicionesDeGastos(Periodo p);
	
	/**
	 * Busca las requisiciones por anticipos pendientes para ser aplicados en funcion de un proveedor
	 * @return
	 */
	public List<RequisicionDe> buscarAnticiposPendientes(GProveedor prov);
	
	public long nextCheque(final Long cuentaId);
	
	
	/**
	 * Regresa una lista de las requisiciones elaboradas en el modulo de compras
	 * en el periodo indicado
	 * 
	 * @param p
	 * @return
	 */
	public List<Requisicion> buscarRequisicionesDeCompras(Periodo p);
	
	public Requisicion buscarRequisicionDeCompras(final Long id);
	
	

}
