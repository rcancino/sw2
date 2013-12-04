package com.luxsoft.sw3.services;

import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.GenericManager;
import com.luxsoft.sw3.ventas.Pedido;

public interface PedidosManager extends GenericManager<Pedido, String>{
	
	/**
	 * Verifica si un pedido es modificable
	 * 
	 * @param pedido
	 * @return
	 */
	public boolean isModificable(final Pedido pedido);
	
	/**
	 * 
	 * @param pedido
	 * @return
	 */
	public boolean isFacturable(final Pedido pedido);
	
	/**
	 * Localiza el descuento adecuado para el pedido en cuestion 
	 * 
	 * @param pedido
	 * 
	 */
	public void asignarDescuento(final Pedido pedido);
	
	/**
	 * Regresa la lista de los pedidos pendientes por facturar
	 * 
	 * @param sucursal
	 * @return
	 */
	public List<Pedido> buscarPendientes(final Sucursal sucursal);
	
	/**
	 * Regresa la lista de los pedidos disponibles para facturar
	 * 
	 * @param sucursal
	 * @return
	 */
	public List<Pedido> buscarFacturables(final Sucursal sucursal);
	
	public List<Pedido> buscarFacturables(final Sucursal sucursal,Pedido.Tipo tipo);
	
	public Pedido buscarPorFolio(Long folio);
	
	/**
	 * Aplica las comisiones aplicables  al pedido sin persistirlo
	 * 
	 * @param pedido
	 */
	//public void aplicarComisiones(final Pedido pedido);
	
	/**
	 * Regla de negocio para determinar si el pedido califica para un descuento especial
	 * 
	 * @return
	 */
	public boolean calificaParaDescuentoEspecial(final Pedido pedido);
	
	/**
	 * Calcula el costo del flete, si este aplica
	 * 
	 * @param pedido
	 */
	public void aplicarFlete(final Pedido pedido);
	
	
	/**
	 * Actualiza la fecha de impresion del pedido.
	 * 
	 * @param pedido
	 * @return
	 */
	public Pedido actualizarFechaDeImpresion(final Pedido pedido,final Date fecha);
	
	public boolean calificaPagoContraEntrega(final Pedido pedido);
	
	/**
	 * Actualiza los importes del pedido en moficiaciones de forma de pago, pero no persiste el pedido
	 * 
	 * @param pedido
	 */
	public void actualizarFormaDePago(final Pedido pedido);
	
	/**
	 * Elimina los pedidos anteriores a la fecha indicada
	 * 
	 * @param fecha
	 * @return
	 */
	public int eliminarPedidos(final Date antesDe);
	
	public Pedido generarCopia(final String pedidoId,Date credao,String user);

}
