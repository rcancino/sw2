package com.luxsoft.siipap.dao.tesoreria;

import java.util.List;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;

/**
 * DAO para la persistencia y acceso a Requisiciones en y desde
 * la base de datos
 * 
 * @author Ruben Cancino
 *
 */
public interface RequisicionDao extends GenericDao<Requisicion , Long>{
	
		
	
	/**
	 * Regresa una lista de las requisiciones elaboradas en el modulo de gastos
	 * 
	 * @return
	 */
	public List<Requisicion> buscarRequisicionesDeGastos();
	
	/**
	 * Regresa una lista de las requisiciones elaboradas en el modulo de gastos
	 * en el periodo indicado
	 * 
	 * @param p
	 * @return
	 */
	public List<Requisicion> buscarRequisicionesDeGastos(Periodo p);
	
	/**
	 * 
	 * @param proveedor
	 * @return
	 */
	public List<RequisicionDe> buscarAnticiposDisponibles(GProveedor proveedor);
	
	
	
	/**
	 * Regresa una lista de las requisiciones elaboradas en el modulo de compras
	 * en el periodo indicado
	 * 
	 * @param p
	 * @return
	 */
	public List<Requisicion> buscarRequisicionesDeCompras(Periodo p);
	
	public Requisicion buscarRequisicionDeCompras(Long id);

}
