package com.luxsoft.siipap.dao.gastos;

import java.util.List;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.gastos.GCxP;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.GProveedor;

/**
 * Usar {@link GFacturaPorCompra}
 * @author RUBEN
 *
 */
@Deprecated 
public interface GCxPDao extends GenericDao<GCxP, Long>{
	
	public List<GCxP> buscarPorProveedor(final GProveedor p); 

}
