package com.luxsoft.siipap.ventas.dao;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.ventas.model.DescPorVol;

public interface DescPorVolDao extends GenericDao<DescPorVol, Long>{
	
	public double buscarDescuentoContado(double volumen);
	
	public double buscarDescuentoContadoPostFechado(double volumen);
	
	public double buscarDescuentoCredito(double volumen);

}
