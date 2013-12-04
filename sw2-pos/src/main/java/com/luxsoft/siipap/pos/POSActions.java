package com.luxsoft.siipap.pos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.Permiso;

/**
 * Menu de Acciones para el Kernell
 * 
 * @author Ruben Cancino
 *
 */
public enum POSActions {
	
	GeneracionDePedidos("Mantenimiento y generacion de pedidos"),
	MantenimientoDeCaja("Mantenimiento general de caja")
	;
	
	
	
	private final String descripcion;
	
	private POSActions(final String descripcion){
		this.descripcion=descripcion;
	}

	public String getDescripcion() {
		return descripcion;
	}
	
	public String getId() {
		return StringUtils.uncapitalize(name());
	}	
	
	
	public static List<Permiso> toPermisos(){
		final List<Permiso> permisos=new ArrayList<Permiso>();
		for(POSActions k:values()){
			final Permiso p=new Permiso(k.name(),k.descripcion,Modulos.INVENTARIOS);
			permisos.add(p);
		}
		return permisos;
	}
	
	

}
