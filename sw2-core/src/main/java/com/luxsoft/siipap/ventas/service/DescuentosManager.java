package com.luxsoft.siipap.ventas.service;

/**
 * @Deprecated NO SE USA
 */
import com.luxsoft.siipap.ventas.model.DescuentoEspecial;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Tareas de administracion relacionadas con los descuentos de ventas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface DescuentosManager {
	
	/**
	 * Busca el descuento especial para la venta indicada
	 * 
	 * @param v La venta de la que se requiere el descuento
	 * @return El descuento especial o nulo si no existe
	 */
	public DescuentoEspecial buscarDescuentoEspecial(final Venta v);
	
	/**
	 * Aplica un descuento especial
	 * 
	 * @param d El descuento especial solicitado
	 * 
	 * @return El descuento si fue exitosa su persistencia
	 */
	public DescuentoEspecial asignarDescuentoEspecial(final DescuentoEspecial d);
	
	/**
	 * Facade para controlar la administracion de los descuentos
	 * debe delegar a otros metodos la actualizacion
	 * 
	 * @param v
	 */
	public void actualizarDescuento(final Venta v);
	
	/**
	 * Actualiza el descuento de la venta y sus partidas para ventas de credito
	 * a precio bruto
	 * 
	 * @param v
	 */
	public void actualizarDescuentoCredPrecioBruto(final Venta v);
	
	/**
	 * Actualiza de manera adecuada el descuento para  ventas de credito
	 * por escala
	 *  
	 * @param v
	 */
	public void actualizarDescuentoCreditoEscala(final Venta v);
	
	/**
	 * Aplica un descuento especial a una venta
	 * 
	 * @param de
	 * @param ventaId
	 */
	public void aplicarDescuentoEspecial(final DescuentoEspecial de, final String ventaId);
	
	/**
	 * Cancela el descuento especial aplicado a una venta
	 * 
	 * @param descuentoId
	 */
	public void cancelarDescuentoEspecial(final Long descuentoId);

	
}
