package com.luxsoft.sw3.inventarios;

import java.text.MessageFormat;

import com.luxsoft.siipap.model.core.Producto;

/**
 * RuntimeException detonada cuando la existencia es 0  
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ExistenciasAgotadasException extends RuntimeException{
	
	private String clave;
	private String descripcion;
	
	
	
	public ExistenciasAgotadasException(String clave) {
		super(MessageFormat.format("Existencia agotada para {0}", clave));
		this.clave = clave;
		this.descripcion=clave;
	}


	public ExistenciasAgotadasException(final Producto producto){
		super(MessageFormat.format("Existencia agotada para {0} ({1})", producto.getDescripcion(),producto.getClave()));
		this.clave=producto.getClave();
		this.descripcion=producto.getDescripcion();
		
	}


	public String getClave() {
		return clave;
	}


	public String getDescripcion() {
		return descripcion;
	}
	
	

}
