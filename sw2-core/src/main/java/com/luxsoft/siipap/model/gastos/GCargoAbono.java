package com.luxsoft.siipap.model.gastos;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Sucursal;

public interface GCargoAbono {
	
	public GProveedor getProveedor();

	public Sucursal getSucursal();

	public Departamento getDepartamento();	

	public Date getFecha();

	public Date getVencimiento();

	public Currency getMoneda();

	public BigDecimal getTc();

	public BigDecimal getTotal();

}