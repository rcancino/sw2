package com.luxsoft.siipap.swx;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.springframework.util.StringUtils;

import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.Permiso;

/**
 * Menu de Acciones para el Modulo de Gastos
 * 
 * @author OCTAVIO
 *
 */
public enum GlobalActions {
	
	// Catalogos Generales 
	SucursalBrowser("Mostrar las sucursales")
	,EmpresaBrowser("Mostrar las empresas")
	,DepartamentoBrowser("Mostrar los departamentos")
	,LineasBrowser("Consulta y mantenimiento de Líneas de productos")
	,ClasesBrowser("Consulta y mantenimiento de Clases")
	,MarcasBrowser("Consulta y mantenimiento de Marcas")
	,ProductosBrowser("Consulta y mantenimiento de productos")
	,ProveedoresBrowser("Mantenimiento a los proveedores de compras")
	,ClientesBrowser("Consulta y mantenimiento de clientes")
	
	
    
    //Inventarios
    ,ShowInventarioView("Mostrar los movimientos de inventario")
    
    //Ventas
    ,ShowVentasTaskView("Mostrar browser de operaciones de ventas")
    ,EliminarPedidos("Eliminar Pedidos")
    ,CancelarFacturas("Cancelar Facturas")
    ,ShowAnalisisDeVentasView("Analisis de ventas")
    ,ShowReportView("Mostrar Reportes")
    ,ShowBIConsultasView("Mostrar Consultas")
    ,ExitApplication("Salir de la aplicación")
    ;
    
	
	
	private final String descripcion;
	
	private GlobalActions(final String descripcion){
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
		for(GlobalActions k:values()){
			final Permiso p=new Permiso(k.name(),k.descripcion,Modulos.CORE);
			permisos.add(p);
		}
		return permisos;
	}
	
	

}
