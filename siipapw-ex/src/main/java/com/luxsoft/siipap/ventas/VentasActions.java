package com.luxsoft.siipap.ventas;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.springframework.util.StringUtils;

import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.Permiso;

/**
 * Lista de acciones parametrizables para el modulo de Ventas
 * 
 * @author Ruben Cancino
 *
 */
public enum VentasActions {
	
	PreciosDeVenta("Muestra la vista de listas de precios de venta")
	;
	
	
	private final String descripcion;
	
	private VentasActions(final String descripcion){
		this.descripcion=descripcion;
	}

	public String getDescripcion() {
		return descripcion;
	}
	
	public String getId() {
		return StringUtils.uncapitalize(name());
	}

	public void decorate(final Action action){
		action.putValue(Action.NAME, name());
		action.putValue(Action.SHORT_DESCRIPTION, getDescripcion());
		action.putValue(Action.LONG_DESCRIPTION, getDescripcion());
	}
	
	public static List<Permiso> toPermisos(){
		final List<Permiso> permisos=new ArrayList<Permiso>();
		for(VentasActions k:values()){
			final Permiso p=new Permiso(k.name(),k.descripcion,Modulos.VENTAS);
			permisos.add(p);
		}
		return permisos;
	}
	

}
