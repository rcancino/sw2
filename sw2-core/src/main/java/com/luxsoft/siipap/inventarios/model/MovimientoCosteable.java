package com.luxsoft.siipap.inventarios.model;

import java.math.BigDecimal;

import com.luxsoft.siipap.model.Sucursal;

public interface MovimientoCosteable {
	
	public String getClave();
	
	public String getDescripcion();
	
	public BigDecimal getCosto();
	
	public String getComentario();
	
	public Sucursal getSucursal() ;

}
