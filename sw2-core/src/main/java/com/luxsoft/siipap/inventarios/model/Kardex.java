package com.luxsoft.siipap.inventarios.model;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;

/**
 * JavaBean que encapsula el comportamiento y estado del concepto 
 * de Kardex.
 *  
 * 
 * @author Ruben Cancino 
 *
 */
public class Kardex {
	
	private Producto producto;
	
	private Periodo periodo;
	
	private Sucursal sucursal;
	
	private EventList<Inventario> movimientos;
	
	

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public Periodo getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
	}

	public EventList<Inventario> getMovimientos() {
		return movimientos;
	}

	public void setMovimientos(EventList<Inventario> movimientos) {
		this.movimientos = movimientos;
	}
	
	

}
