package com.luxsoft.siipap.inventarios.dao;

import java.util.List;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.inventarios.model.Kit;
import com.luxsoft.siipap.inventarios.model.KitDet;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.ConfiguracionKit;

public interface KitDao extends GenericDao<Kit, Long>{
	
	/**
	 * Busca todos los movimientos de entrada y salida generados
	 * por creacion de productos kit
	 * 
	 * @param p
	 * @return
	 */
	public List<Kit> buscarMovimientsKit(final Periodo p);
	
	
	/**
	 * Arma el numero adecuado de salidas para atender la entrada indicada
	 * del producto kit
	 *
	 * @param config
	 * @param target
	 * @return
	 */
	public List<KitDet> prepararSalidas(final ConfiguracionKit config,final KitDet target);
	
	

}
