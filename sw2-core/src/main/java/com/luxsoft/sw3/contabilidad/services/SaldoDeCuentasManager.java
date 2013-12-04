package com.luxsoft.sw3.contabilidad.services;



import java.math.BigDecimal;

import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.SaldoDeCuenta;

public interface SaldoDeCuentasManager {
	
	public SaldoDeCuenta getSaldo(CuentaContable cuenta,final int year,final int mes);
	
	public void recalcularSaldos(final int year,final int mes);
	
	public SaldoDeCuenta recalcularSaldo(CuentaContable cuenta,final int year,final int mes);
	
	public BigDecimal findSaldoPorConceptoAnterior(ConceptoContable c,int year,final int mes);

}
