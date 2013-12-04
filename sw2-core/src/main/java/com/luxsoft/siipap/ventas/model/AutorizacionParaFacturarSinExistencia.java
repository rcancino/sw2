package com.luxsoft.siipap.ventas.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.luxsoft.siipap.model.Autorizacion2;

/**
 * Autorizacion para ventas que sin existencias suficientes
 * 
 * @author pato
 *
 */
@Entity
@DiscriminatorValue("FAC_SIN_EXIS")
public class AutorizacionParaFacturarSinExistencia extends Autorizacion2{
	
	//@OneToOne(mappedBy="autorizacionSinExistencia")
	//private Venta venta;
	
	public AutorizacionParaFacturarSinExistencia(){
		setComentario("AUTORIZACION PARA FACTURAR SIN EXISTENCIAS");
	}
/*
	public Venta getVenta() {
		return venta;
	}

	public void setVenta(Venta venta) {
		this.venta = venta;
	}
	
	*/

}
