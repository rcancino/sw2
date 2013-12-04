package com.luxsoft.siipap.maquila.model;

import java.math.BigDecimal;
import java.util.Date;

import com.luxsoft.siipap.inventarios.model.MovimientoCosteable;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;


/**
 * Interface para el costeo de movimientos  con flete
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface MovimientoConFlete extends MovimientoCosteable{	
	
	public String getRemision() ;
	
	public Producto getProducto();
	
	public Sucursal getSucursal();
	
	public Long getDocumento();
	
	public Date getFecha();
	
	public double getKilosCalculados();
	
	public double getCantidad();
	
	public double getFactor();
	
	public String getTipoDocto();
	
	public BigDecimal getCostoFlete();	

	public void setCostoFlete(BigDecimal costoFlete);
	
	public BigDecimal getImporteDelFlete();

	public AnalisisDeFlete getAnalisisFlete();

	public void setAnalisisFlete(AnalisisDeFlete analisisFlete);
	
	public void actualizarCosto();

}
