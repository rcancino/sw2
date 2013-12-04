package com.luxsoft.siipap.cxp;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.springframework.util.StringUtils;

import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.Permiso;

/**
 * Menu de Acciones para el Modulo de CxP
 * 
 * @author Ruben Cancino
 *
 */
public enum CXPActions {
	
	ShowCXPView("Mostrar CXP")
	,MantenimientoDeContrarecibos("Altas y bajas de contrarecibos")
	,ConsultasBI("Consultas de analisis para CXP")
	;
	
	
	private final String descripcion;
	
	private CXPActions(final String descripcion){
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
		for(CXPActions k:values()){
			final Permiso p=new Permiso(k.name(),k.descripcion,Modulos.CXP);
			permisos.add(p);
		}
		return permisos;
	}
	

}
