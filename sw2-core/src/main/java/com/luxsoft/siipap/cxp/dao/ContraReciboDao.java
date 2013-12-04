package com.luxsoft.siipap.cxp.dao;

import java.util.List;

import com.luxsoft.siipap.cxp.model.ContraRecibo;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;

public interface ContraReciboDao extends GenericDao<ContraRecibo, Long>{
	
	public ContraRecibo buscarInicializado(final Long id);
	
	public List<ContraReciboDet> buscarPartidas(final ContraRecibo recibo);
	
	public List<ContraRecibo> buscarRecibos(final Periodo p);
	
	/**
	 * Buscar recibos pendientes de analisis
	 * 
	 * @param p
	 * @param tipo
	 * @return
	 */
	public List<ContraReciboDet> buscarRecibosPendientes(final Proveedor p,ContraReciboDet.Tipo tipo);
	
	/**
	 * Busca recibos pendientes de analisis
	 * 
	 * @param tipo
	 * @return
	 */
	public List<ContraReciboDet> buscarRecibosPendientes(ContraReciboDet.Tipo tipo);

}
