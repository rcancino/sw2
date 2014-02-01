package com.luxsoft.siipap.cxc;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.springframework.util.StringUtils;

import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.service.ServiceLocator2;


public enum CXCRoles {
	
	CarteraDeContado("Consulta de cartera de contado"),
	COBRANZA_CONTADO("Operacioens de cobranza en ventas de contado")
	,ADMINISTRADOR_COBRANZA_CREDITO("Administrador de cobranza de credito")
	,GERENCIA_DE_CREDITO("Role para las operaciones gerenciales de credito")
	,MODIFICACION_LINEA_DE_CREDITO("Autorizaciones especiales de línea de credito")
	,MODIFICACION_ATRASO_MAXIMO("Autorizaciones para modificaciones de atraso maximo")
	,ADMINISTRACION_CHECKPLUS("Administracion de Checkplus")
	,AUTORIZAR_MODIFICACIONES_DE_DATOS("Autorizar modificaciones de datos")
	,DESBLOQUEO_POR_SALDO_CHEQUES_DEVUELTOS("DESBLOQUEO POR SALDO CHEQUES DEVUELTOS")
	,FACTURACION_DOLARES("Facturacion en Dolares")
	,DIRECCION_COMERCIAL("Direccion comercial")
	,LISTA_DE_PRECIOS_CLIENTES("Consulta a lista de precios cliente")
	,MODIFICACION_TIPO_VENCIMIENTO("Cambia en tipo de vencimiento de un cliente")
	;
	
private final String descripcion;
	
	private CXCRoles(final String descripcion){
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
		for(CXCRoles rol:values()){
			final Role r=new Role(rol.name());
			r.setDescription(rol.getDescripcion());
			r.setModulo("CXC");
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
