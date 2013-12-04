package com.luxsoft.siipap.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Enumeracion temporal para el control de sucursales
 * sera sustituida por un catalogo
 * 
 * TODO Implementar un catalogo
 * 
 * @author Ruben Cancino
 *
 */
public enum Sucursales {
	
	ANDRADE("Andrade",3),
	BOLIVAR("Bolivar",5),
	QUERETARO("Queretaro",9),
	CALLE4("Calle 4",10),
	ERMITA("Ermita",11),
	TACUBA("Tacuba",12),	
	;
	
	private final String descripcion;
	private final int numero;
	
	private Sucursales(final String descripcion, final int numero) {
		this.descripcion = descripcion;
		this.numero = numero;
	}
	
	public String toString(){
		return descripcion;
	}
	
	public int getNumero(){
		return numero;
	}
	
	public Integer[] todos(){
		return new Integer[]{1,4,6,7,8};
	}
	
	public static List<Sucursales> getSucursales(){
		ArrayList<Sucursales> l=new ArrayList<Sucursales>();
		for(Sucursales c:values()){			
			l.add(c);
		}
		return l;
	}
	
	public static Sucursales getSucursal(int id){
		for(Sucursales c:values()){
			if(c.getNumero()==id)
				return c;
		}
		return null;
	}

}
