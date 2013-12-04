package com.luxsoft.siipap.kernell;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.springframework.util.StringUtils;

import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.Permiso;

/**
 * Menu de Acciones para el Kernell
 * 
 * @author Ruben Cancino
 *
 */
public enum KernellActions {
	
	MostrarKernell("Consulta general de la seguridad del sistema")
	,MostrarUsuarios("Muestra el panel de usuarios del sistema")
	,MostrarRoles("Muestra el panel de roles del sistema")
	,MostrarPermisos("Muestra el panel de permisos del sistema")
	,ActualizarPermisos("Permite actualizar los permisos de todos los modulos")
	,AgregarPermisosPorRol("Permite agregar permisos a roles")
	;
	
	
	private final String descripcion;
	
	private KernellActions(final String descripcion){
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
		for(KernellActions k:values()){
			final Permiso p=new Permiso(k.name(),k.descripcion,Modulos.KERNELL);
			permisos.add(p);
		}
		return permisos;
	}
	
	

}
