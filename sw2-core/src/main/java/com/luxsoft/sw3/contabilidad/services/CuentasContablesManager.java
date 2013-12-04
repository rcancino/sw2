package com.luxsoft.sw3.contabilidad.services;

import com.luxsoft.sw3.contabilidad.dao.CuentaContableDao;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;

public interface CuentasContablesManager {
	
	public CuentaContable salvar(CuentaContable cuenta);

	public CuentaContableDao getCuentaContableDao() ;

	public void eliminarCuentaContable(CuentaContable cuenta);
	
	public CuentaContable buscarPorClave(String clave);
	
	

}
