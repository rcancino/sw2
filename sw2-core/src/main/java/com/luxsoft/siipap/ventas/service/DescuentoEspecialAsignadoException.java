package com.luxsoft.siipap.ventas.service;

import com.luxsoft.siipap.cxc.model.Cargo;

public class DescuentoEspecialAsignadoException extends DescuentoException{

	public DescuentoEspecialAsignadoException(Cargo cargo) {
		super("Ya existe un descuento asignado para el cargo: "+cargo.toString());
		
	}
	
	

}
