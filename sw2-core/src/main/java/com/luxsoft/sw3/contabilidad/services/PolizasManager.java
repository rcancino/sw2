package com.luxsoft.sw3.contabilidad.services;

import com.luxsoft.sw3.contabilidad.dao.PolizaDao;
import com.luxsoft.sw3.contabilidad.model.Poliza;

public interface PolizasManager {
	
	public boolean existe(Poliza poliza);
	
	public Poliza salvarPoliza(Poliza poliza);
	
	public Poliza cancelarPoliza(Poliza poliza);
	
	public PolizaDao getPolizaDao();

}
