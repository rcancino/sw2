package com.luxsoft.siipap.cxc;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.springframework.util.StringUtils;

import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.Permiso;

/**
 * Menu de Acciones para el Modulo de CxC
 * 
 * @author Ruben Cancino
 *
 */
public enum CXCActions {
	
	NotasDeCredito("Notas de Credito")
	,CuentasPorCobrar("Cuenta Por Cobrar")
	,ListasDePrecios("Listas de Precios Cargo")
	,EstadoDeCuentaReport("Estado de cuenta por cliente")
	,RegistrarDepositos("Registrar depositos en tesoreria")
	,RegistrarPagos("Registro y mantenimiento de pagos")
	,PagarConCheque("Registrar pago con cheque")
	,PagarConTarjeta("Registrar pago con tarjeta")
	,PagarConDeposito("Registrar pago con Deposito/Transferencia")
	,PagarConEfectivo("Registrar pago con efectivo")
	,ModificarPago("Modificar pago de cualquier tipo ")
	,AplicarPago("Aplicar pago")
	,CancelarAbono("Cancela el un abono")
	,CancelarNotaDeCargo("Cancela nota de cargo")
	,CancelarAplicacion("Cancela una aplicacion")
	,SolicitudDeAutorizacionParaAbono("Solicitud de autorizacion para uso de abono")
	,SolicitudDeCancelacion("Solicitud para  la de cancelacion de un abono")
	,GenerarNotaDeDescuento("Generar notas de credito para descuento en ventas")
	,GenerarNotaDeBonificacion("Generar notas de credito para bonificación sobre ventas")
	,GenerarNotaDeDevolucion("Generar notas de credito para devolución en ventas")
	,GenerarNotaDeCargo("Generar notas de cargo")
	,GenerarPagoDiferencias("Generar un pago de diferencias")
	,RefrescarSeleccion("Refrescar seleccion")
	,RevisionyCobro("Revision y Cobro")
	,ActualizarDescuentosAFactura("Actualiza los descuentos a las facturas de precio bruto con saldo")
	,AplicarDescuentoEspecialAFactura("Permite generar un descuento especial a las facturas de precio bruto")
	,CancelarDescuentoEspecial("Permite cancelar un descuent especial")
	,AdministracionRevisionCobro("Mantenimiento a revisión y cobro de cuentas por cobrar")
	,RecepcionDeCuentasPorCobrar("Recepcion de cuentas por cobrar en el depto")
	,CancelarRecepcionDeCuentasPorCobrar("Cancelar recepcion de cuentas por cobrar")
	,RecalcularRevisionCobro("Recalcula las fechas de revision y cobro para CXC")
	,ConsultarDisponibles("Consultar disponibles del cliente")
	,GenerarPolizasContablesCxC("Genera polizas contables")
	,GenerarChequeDevuelto("Generar cargo por cheque devuelto")
	,ConsultaDeClientes("Consulta del catalogo de clientes")
	,MantenimientoClientes("Consulta y mantenimiento de clientes")
	,MantenimientoDeComisiones("Mantenimiento de comisiones de Vendedor y Cobrador")
	,JuridicoAlta("Alta de cargos en jurídico")
	,JuridicoView("Consulta de traspaso a jurídico")
	,JuridicoBaja("Cancelar un traspaso a jurídico")
	,ImprimirNotaDeCargo("Permite imprimir nota de cargo")
	,GenerarFichasDeDeposito("Genera fichas de deposito para pagos depositables")
	,ReportesDePagos("Consulta de reportes de pagos")
	,ReportesDeDepositos("Consulta de reportes de depositos")
	,CancelarEnvioDeValores("Cancelar envio de deposito al medio de valores")
	,ReportesDeCarteraDeCheques("Ejecutar reportes de la cartera de cheques devueltos")
	,ReportesDeCarteraDeJuridico("Ejecutar reportes de la cartera de jurídico")
	,ConsultasDeAnalisis("Consultas de análisis")
	,ReportesGenerales("Reportes generales de CxC")
	,ConsultaDeDepositos("Acceso basico a la consulta de depositos")
	,CXC_DEPOSITOS_PANEL("Acceso a la sub consulta de depositos")
	,CarteraDeCheques("Acceso a la sub consulta de de cartera de cheques devueltos")
	
	,CXC_CarteraDeCredito("Consulta de cartera de credito")
	,CXC_RevisionCobro("Consulta de revisión y cobro")
	,CXC_Abonos("Abonos")
	,CXC_AntiguedadSaldos("Consulta de Antigüedad de saldos")
	,CXC_EstadoMovimientos("Consulta de Estado de movimientos")
	,CXC_Comisiones("Consulta de comisiones")
	,CXC_Juridico("Consulta de juridico")
	;
	
	
	private final String descripcion;
	
	private CXCActions(final String descripcion){
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
		for(CXCActions k:values()){
			final Permiso p=new Permiso(k.name(),k.descripcion,Modulos.CXC);
			permisos.add(p);
		}
		return permisos;
	}

}
