package com.luxsoft.siipap.gastos;

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
public enum GasActions {
	
	TiposDeProveedorBrowser("Mostrar los tipos de proveedor"),
	ProveedoresBrowser("Mostrar los proveedores de gastos"),
	ConceptosBrowser("Mostrar los conceptos de gastos"),
	ProductosBrowser("Mostrar los productos y/o servicios"),
	SucursalBrowser("Mostrar las sucursales"),
	EmpresaBrowser("Mostrar las empresas"),
	DepartamentoBrowser("Mostrar los departamentos"),
    INPCBrowser("Inidice de precios al consumidor"),
    ClasificacionDeActivosBrowser("Clasificación de Activo Fijo"),
    ConsignatarioBrowser("Consignatarios"),
    OrdenDeCompraBrowser("Mostrar las el brwoser de ordenes de compra"),
    
    showCancelacionDeCompra("Cancelacion De Compra"),
    showModificacionDeCompra("Modificacion de Compra"),
    showAutorizacionDeCompra("Autorizaciones"),
    showEliminacionDeRequisicion("Eliminacion De Requisicion"),
    showRecepcion("Recepcion"),
    showProveedores("Proveedores"),
    showPagosProveedor("Pagos"),
    showCargosProveedor("Cargos"),
    showPolizasProveedor("Polizas"),
    
    showEstadoDeCuenta("Estado de Cuenta"),
    
    showAnalisisIETU("Analisis IETU"),
    
    showComprasView("Mostrar consulta de compras"),
    showResumenDeGastos("Mostrar resumen de gastos")
    
    ,ActivoFijoBrowser("Activo Fijo")
    ,ShowActivoFijoFiscalView("Mantenimiento a Activo Fijo")
    ,ShowActivoFijoResumen1("Resumen a Activo Fijo (1)")
    ,RegistrarActivoManual("Registro de A.F manual")
    ,RegistrarActivoPorProveedor("Registrar A.F. por proveedor")
    ,RegistrarActivoPorCompra("Registrar A.F. por O.Compra")
    ,EnajenarActivo("Enajenar A.F.")
    ,ActualizarFiscal("Actualizar información fiscal")
    ,ShowPagosView("Consulta de Pagos ")
    ,GenerarPolizaEgresoCompras("Generar Poliza de egresos compras")
    ,ShowRequisicionesView("Requisiciones")
    ,ShowRequisicionesBrowser("Requisiciones")
    ,RegistrarFacturas("Registrar facturas de compras")
    ,GenerarRequisiciones("Generar requisiciones")
    ,AplicarAnticipoDePago("Aplicar paro de requisición")
    ,RevisionDeRequisicion("Revisión de requisición")
    ,CancelarRevisionDeRequisicion("Cancelar revisión de requisición")
    ,ShowPolizasView("Mostrar consulta de polizas")
    ,GenerarPolizaDeGastos("Generar poliza de gastos")
    ,LiberarCheques("Liberación de Cheques")
    ,ShowAnalisisDeGastos("Mostrar consulta de ánalisis de gastos")
    ,ShowContraRecibos("Mostrar vista de Contra Recibos")
	;
	
	
	
	private final String descripcion;
	
	private GasActions(final String descripcion){
		this.descripcion=descripcion;
	}

	public String getDescripcion() {
		return descripcion;
	}
	
	public String getId() {
		String modulo="gastos.";
		return modulo+StringUtils.uncapitalize(name());
	}

	public void decorate(final Action action){
		action.putValue(Action.NAME, name());
		action.putValue(Action.SHORT_DESCRIPTION, getDescripcion());
		action.putValue(Action.LONG_DESCRIPTION, getDescripcion());
	}
	
	
	public static List<Permiso> toPermisos(){
		final List<Permiso> permisos=new ArrayList<Permiso>();
		for(GasActions k:values()){
			final Permiso p=new Permiso(k.name(),k.descripcion,Modulos.GASTOS);
			permisos.add(p);
		}
		return permisos;
	}
	

}
