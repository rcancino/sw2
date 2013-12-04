package com.luxsoft.siipap.compras;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.springframework.util.StringUtils;

import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.Permiso;

/**
 * Menu de Acciones para el Modulo de Tesoreria
 * 
 * @author OCTAVIO
 *
 */
public enum ComprasActions {
	
	MostrarListasVigentes("Mostrar listas de precios vigentes")
	,ShowComprasView("Mostrar la vista de compras")
	,ShowListasDePrecios("Mantenimiento a listas de precios")
	,ShowProductosView("Consulta y mantenimiento de productos")	
	,TiposDeProveedorBrowser("Mostrar los tipos de proveedor")	
    ,OrdenDeCompraBrowser("Mostrar las el brwoser de ordenes de compra")
    ,AnalisisDeComprasView("Consultas de analisis de compras")
	;
	
	
	private final String descripcion;
	
	private ComprasActions(final String descripcion){
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
		for(ComprasActions k:values()){
			final Permiso p=new Permiso(k.name(),k.descripcion,Modulos.COMPRAS);
			permisos.add(p);
		}
		return permisos;
	}
	

}
