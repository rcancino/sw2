package com.luxsoft.siipap.ventas.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.hibernate.annotations.Parent;

/**
 * Entidad auxiliar para las ventas de tipo credito
 * 
 * @author Ruben Cancino
 *
 */
@Embeddable
public class VentaCredito implements Serializable{
	
	static final long serialVersionUID = 42L;
	
	@Parent
	private Venta venta;
	
    	
	/*

	public String getId() {
		return id;
	}
	*/

	public Venta getVenta() {
		return venta;
	}
	
	/**
	 * La venta origen de la operacion
	 * 
	 * @param venta
	 */
	public void setVenta(Venta venta) {
		this.venta = venta;
	}
	

	
	
	

}
