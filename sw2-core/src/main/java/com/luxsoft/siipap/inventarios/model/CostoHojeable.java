package com.luxsoft.siipap.inventarios.model;

import java.math.BigDecimal;

import com.luxsoft.siipap.maquila.model.MovimientoConFlete;

public interface CostoHojeable extends MovimientoConFlete{
	
	public BigDecimal getCostoMateria();
	
	public void setCostoCorte(BigDecimal costoCorte);
	
	public BigDecimal getCostoCorte();

}
