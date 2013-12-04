package com.luxsoft.sw3.ventas;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.springframework.util.StringUtils;

import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.service.ServiceLocator2;


public enum VentasRoles {
	
	GERENCIA_DE_VENTAS_CORPORATIVO("Operaciones exclusivas de Gerencia de ventas corporativa")
	,DIRECCION_DE_VENTAS("Servicios exclusivos para operar por dirección de ventas")
	,LISTAS_DE_PRECIOS_VENTA("Generación y mantenimiento de listas de precios")
	,GERENTE_SUCURSAL("Gerente de sucursal")
	;
	
private final String descripcion;
	
	private VentasRoles(final String descripcion){
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
		for(VentasRoles rol:values()){
			final Role r=new Role(rol.name());
			r.setDescription(rol.getDescripcion());
			r.setModulo("VENTAS");
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
