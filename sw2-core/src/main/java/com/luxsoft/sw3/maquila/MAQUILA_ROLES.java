package com.luxsoft.sw3.maquila;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.springframework.util.StringUtils;

import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.service.ServiceLocator2;


/**
 * Catalogo de roles parametrizados para el modulo de CRM
 * 
 * @author Ruben Cancino
 *
 */
public enum MAQUILA_ROLES {
	
	MAQUILA_USER("Acceso al las opciones de Maquila")
	,MANTENIMIENTO_CATALOGOS_MAQ("Mantenimiento a los catalogos de maquila")	
	,MOVIMIENTO_DE_BOBINAS("Mantenimiento de movimientos de bobinas")
	;
	
	
	private final String descripcion;
	
	private MAQUILA_ROLES(final String descripcion){
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
	
	public static List<Role> toRoles(){
		final List<Role> permisos=new ArrayList<Role>();
		for(MAQUILA_ROLES rol:values()){
			final Role r=new Role(rol.name());
			r.setDescription(rol.getDescripcion());
			r.setModulo("MAQUILA");
			permisos.add(r);
		}
		return permisos;
	}
	
	public static void generarRoles(){
		for(Role rol:toRoles()){
			try {
				ServiceLocator2.getUniversalDao().save(rol);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		generarRoles();
	}

}
