package com.luxsoft.siipap.tesoreria;

import javax.swing.Action;

import org.springframework.util.StringUtils;

/**
 * Menu de Acciones para el Modulo de Tesoreria
 * 
 * @author OCTAVIO
 *
 */
public enum TesoreriaActions {
	
	ShowInstitucionBancaria("Institucion Bancaria")
	,ShowCuentaBancaria("Cuenta Bancaria")
	,MantenimientoDeconceptos("Mantenimiento a conceptos de ingresos/egresos")
	,MantenimientoDeTarjetas("Mantenimiento de tarjetas de credito/debito")
	,ConsultarRequisiciones("Consultar Vista de Requisiciones")
	,ShowRevision("Revision")
	,ShowAutorizacionDeRequisiciones("Autorizacion de Requisiciones")
	
	,ShowAnalisisDePagosView("Analisis de Pagos")
	,ShowMovimientosView("Movimientos de cuentas")
	,ShowRequisicionesView("Mantenimiento a requisiciones")
	,ImportarDepositosTask("")
	,AutorizarRequisicion("Autorizar requisición")
	,PagarRequisicion("Pagar requisición ")
	,CancelarAutorizacion("Cancelar autorizacion")
	,CancelarPagoDeRequisicion("Cancelar pago de requisición")
	,EstadoDeCuenta("Mostrar estado de cuenta")
	,ShowDepositosView("Revision de depostios a importar")
	;
	
	
	private final String descripcion;
	
	private TesoreriaActions(final String descripcion){
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
	
	

}
