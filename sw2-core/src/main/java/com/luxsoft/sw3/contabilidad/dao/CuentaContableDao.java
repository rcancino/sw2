package com.luxsoft.sw3.contabilidad.dao;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;

public interface CuentaContableDao extends GenericDao<CuentaContable, Long>{
	
	
	public CuentaContable buscarPorClave(String clave);

}
