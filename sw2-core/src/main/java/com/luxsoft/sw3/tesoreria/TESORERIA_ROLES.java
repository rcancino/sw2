package com.luxsoft.sw3.tesoreria;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.springframework.util.StringUtils;

import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.service.ServiceLocator2;


/**
 * Catalogo de roles para tesoreria
 * 
 * @author Ruben Cancino
 *
 */
public enum TESORERIA_ROLES {
	
	TESORERIA_USER("Usuario de tesoreria")
	,SolicitudesDeDepositosView("Consulta de solicitudes para depositos")
	,AUTORIZACION_DEPOSITOS("Autorización de depositos")
	,CONTROL_DE_INGRESOS("Mantenimiento de ingresos a las cuentas de banco")
	;
	
	
	private final String descripcion;
	
	private TESORERIA_ROLES(final String descripcion){
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
		for(TESORERIA_ROLES rol:values()){
			final Role r=new Role(rol.name());
			r.setDescription(rol.getDescripcion());
			r.setModulo("TESORERIA");
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
