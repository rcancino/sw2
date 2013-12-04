package com.luxsoft.sw3.crm;

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
public enum CRM_Roles {
	
	CRM_USER("Acceso al las opciones de CRM")
	,MANTENIMIENTO_CLIENTES("Mantenimiento complementario de Clientes")	
	;
	
	
	private final String descripcion;
	
	private CRM_Roles(final String descripcion){
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
		for(CRM_Roles rol:values()){
			final Role r=new Role(rol.name());
			r.setDescription(rol.getDescripcion());
			r.setModulo("CRM");
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
