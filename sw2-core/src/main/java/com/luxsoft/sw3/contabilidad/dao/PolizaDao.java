package com.luxsoft.sw3.contabilidad.dao;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.sw3.contabilidad.model.Poliza;

public interface PolizaDao extends GenericDao<Poliza, Long>{
	
	public Long buscarProximaPoliza(int year,int mes,String clase);
	
	public Long buscarProximaPoliza(int year,int mes,String clase,String tipo);

}
