package com.luxsoft.siipap.inventario;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.Permiso;

/**
 * Menu de Acciones para el Kernell
 * 
 * @author Ruben Cancino
 *
 */
public enum InventariosActions {
	
	
	ConsultaDeCostosDeInventario("Acceso a la consulta principal de costos del inventario")
	,CalculoDeCostos("Mantenimiento de diversos costos del inventario")
	,ImportarTransformaciones("Permite importar transformaciones desde SX_INVENTARIO_MOV")
	
	//A Revision
	,RecepcionDeCompras("Recepción de material por concepto de compra")
	,ClasificacionDeMovimientos("Clasificacion de movimientos")
	,GenerarMovimiento("Generar movimiento de inventario")
	,TrasladosView("Traslados de material")
	,MaquilaView("Consultar ordenes de maquila")
	,Tranformaciones("Transformaciones")
	;
	
	
	private final String descripcion;
	
	private InventariosActions(final String descripcion){
		this.descripcion=descripcion;
	}

	public String getDescripcion() {
		return descripcion;
	}
	
	public String getId() {
		return StringUtils.uncapitalize(name());
	}	
	
	
	public static List<Permiso> toPermisos(){
		final List<Permiso> permisos=new ArrayList<Permiso>();
		for(InventariosActions k:values()){
			final Permiso p=new Permiso(k.name(),k.descripcion,Modulos.INVENTARIOS);
			permisos.add(p);
		}
		return permisos;
	}
	
	

}
