package com.luxsoft.siipap.cxc.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.cxc.dao.PagoDao;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.CargoPorDiferencia;
import com.luxsoft.siipap.cxc.model.ChequeDevuelto;
import com.luxsoft.siipap.cxc.model.Juridico;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.cxc.model.PagoDeDiferencias;
import com.luxsoft.siipap.cxc.model.PagoEnEspecie;
import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.model.Autorizacion2;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Cobrador;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;

/**
 * Manager para la interaccion con todos los servicios relacionados
 * con las cuentas por pagar
 * 
 * @author Ruben Cancino
 *
 */
public interface CXCManager {
	
	public Cargo getCargo(final String id);
	
	/**
	 * Forma generica de buscar un abono
	 * 
	 * @param id
	 * @return
	 */
	public Abono getAbono(final String id);
	
	public Cargo save(final Cargo bean);
	
	/**
	 * Persiste una lista de cargos
	 * 
	 * @param cargos
	 * @return
	 */
	public List<Cargo> salvar(final List<Cargo> cargos);
	
	/**
	 * Regresa una lista actualizada de las cuentas por cobrar
	 * 
	 * @param origen
	 * @return
	 */
	public List<Cargo> buscarCuentasPorCobrar(OrigenDeOperacion origen);
	
	public List<Cargo> buscarCuentasPorCobrar();
	
	
	/**
	 * Busca las cuentas por cobrar para un cliente y del tipo indicado
	 * si el origen es nulo debe regresar todas las cuentas por cobrar para el cliente
	 * 
	 * @param cliente
	 * @param origen
	 * @return
	 */
	public List<Cargo> buscarCuentasPorCobrar(Cliente cliente,OrigenDeOperacion origen);
	
	/**
	 * Busca los abonos disponibles para aplicar por el cliente
	 * 
	 * @param cliente
	 * @return
	 */
	public List<Abono> buscarDisponibles(Cliente cliente);
	
	
	
	/**
	 * Persiste cualquier tipo de abono en la base de datos
	 * 
	 * @param a El abono a persistir
	 * @return El abono perfectamente inicializado en todas sus propiedades
	 */
	public Abono salvarAbono(final Abono a);
	
	/**
	 * Persiste un pago en efectivo
	 * 
	 * @param pago
	 * @return
	 */
	public PagoConEfectivo salvarPago(PagoConEfectivo pago);
	
	/**
	 * Persiste un pago con tarjeta
	 * @param pago
	 * @return
	 */
	public PagoConTarjeta salvarPago(PagoConTarjeta pago);
	
	/**
	 * Persiste un pago con cheque
	 * @param pago
	 * @return
	 */
	public PagoConCheque salvarPago(PagoConCheque pago);
	
	/**
	 * Persiste un pago con deposito
	 * 
	 * @param pago
	 * @return
	 */
	public PagoConDeposito salvarPago(PagoConDeposito pago);
	
	
	/**
	 * Lista de tarjetas registradas para pago
	 * 
	 * @return
	 */
	public List<Tarjeta> buscarTarjetas();
	
/** Manejo de listas de precios Cargo***/
	
	public ListaDePreciosCliente buscarListaDePrecios(Long id);
	
	public List<ListaDePreciosCliente> buscarListasDePrecios();
	
	public List<ListaDePreciosCliente> buscarListasDePreciosVigentes();
	
	//public List<ListaDePreciosCliente> buscarListasDePrecios(final Periodo p);
	
	public ListaDePreciosCliente buscarListaVigente(final Cliente p);
	
	public ListaDePreciosCliente salvarLista(ListaDePreciosCliente lp);
	
	/**
	 * Elimina la lista de precios 
	 *  
	 * @param id
	 */
	public void eliminarLista(Long id);
	
	/**
	 * Genera una copia de la lista de precios indicada
	 * 
	 * @param id
	 * @return
	 */
	public ListaDePreciosCliente copiar(Long id);
	
	
	/**
	 * Regresa la lista de cobradores activos
	 * 
	 * @return
	 */
	public List<Cobrador> getCobradores();
	
	/**
	 * Regresa la lista de vendedores activos
	 * 
	 * @return
	 */
	public List<Cobrador> getVendedores();
	
	
	/***********************************/
	
	public PagoDao getPagoDao();

	/**
	 * Persiste una nota de credito
	 * 
	 * Debe actualizar la propiedad de descuentos en las aplicacciones existentes
	 * 
	 * @param nota
	 * @return
	 */
	public NotaDeCredito salvarNota(final NotaDeCredito nota);
	
	
	
	
	/**
	 * Busca los abonos del periodo para el tipo de opracion indicado
	 * 
	 * @param p El periodo del abono
	 * @param tipo El tipo de operacion al que pertenecen los abonos
	 * @return La lista de abonos 
	 */
	public List<Abono> buscarAbonos(final Periodo p,OrigenDeOperacion tipo);
	
	
	/**
	 * Busca los cargos relacionados con  el abono
	 * 
	 * @param abono
	 * @return
	 */
	public List<Cargo> buscarCargos(final Abono abono);
	
	/**
	 * Busca las aplicaciones relacionadas con el abono
	 * 
	 * @param abono
	 * @return
	 */
	public List<Aplicacion> buscarAplicaciones(final Abono abono);
	
	
	
	
	/**
	 * Cancela una nota de credito
	 * 
	 * @param id
	 * @param aut
	 */
	public void cancelarNota(final String notaId,final Autorizacion2 aut);
	
	
	
	/**
	 * Cancela un pago si es posible
	 * 
	 * @param id
	 * @param aut
	 */
	public void cancelarPago(final String id,final Autorizacion2 aut);
	
	/**
	 * Cancela una aplicacion
	 * 
	 * @param id
	 */
	public void cancelarAplicacion(final String id);
	
	/**
	 * Cancela una nota de cargo 
	 * 
	 * @param id
	 */
	public void cancelarNotaDeCargo(final String id);
	
	public void cancelarChequeDevuelto(final String id);
	
	/**
	 * Obtiene el monto de los pagos (abonos tipo Pago) aplicados a un cargo
	 * @param c
	 * @return
	 */
	public BigDecimal sumarPagos(final Cargo c);
	
	
	public EstadoDeCuentaManager getEstadoDeCuentaManager();
	
	public int buscarProximaNota();
	
	public int buscarProximaNotaDeCargo();
	
	/**
	 * Actualiza la fecha de revision y cobro para toda la cuenta por pagar
	 * y la regresa para facilitar la
	 * 
	 */
	public List<Cargo> actualizarRevisionYCobro();
	
	/**
	 * Genera el pago por diferencia a una factura/cargo
	 * 
	 * @param cargo
	 * @return
	 */
	public PagoDeDiferencias generarPagoPorDiferencia(final Cargo cargo,final boolean cambiaria);
	
	/**
	 * Genera el pago por incobrabilidad
	 * 
	 * @param cargo
	 * @return
	 */
	public PagoDeDiferencias generarPagoPorIncobrabilidad(final Cargo cargo,final String comentario);
	
	
	public PagoEnEspecie generarPagoEnEspecie(final Cargo cargo,String comentario);
	/**
	 * Salda un abono por diferencia cambiaria
	 * 
	 * @param abono
	 * @param cambiaria
	 * @return
	 */
	public Abono generarAplicacionPorDiferencia(final Abono abono,final Date fecha,final CargoPorDiferencia.TipoDiferencia tipo);
	
	/**
	 * Genera el cargo correspondiente por cheque devuelto 
	 * 
	 * @param pago
	 * @return
	 */
	public ChequeDevuelto generarChequeDevuelto(final PagoConCheque pago,final Date fecha);
	
	/**
	 * Genera la nota automatica para un cheque devuelto
	 * 
	 * @param cargo
	 * @return
	 */
	public NotaDeCargo generarNotaDeCargoPorChequeDevuelto(final ChequeDevuelto cargo);
	
	/**
	 * 
	 * @param jur
	 * @return
	 */
	public Juridico generarJuridico(Juridico jur);
	
	/**
	 * 
	 * @param jur
	 */
	public void cancelarJuridico(Juridico jur);

}
