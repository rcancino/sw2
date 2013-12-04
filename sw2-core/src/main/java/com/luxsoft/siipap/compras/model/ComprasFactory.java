package com.luxsoft.siipap.compras.model;




/**
 * Factory methods para la creacion de beans relacionados con el modulo de compras
 *  Permite centralizar las reglas para la generacion de beans relacionados con Compras
 *  
 * 
 * @author Ruben Cancino. 
 *
 */
public class ComprasFactory {
	
	/**
	 * Factory method para crear partidas de compra consistentes con las reglas de negocios
	 * 
	 * @param compra
	 * @return
	 */
	public static CompraDet crearPartida(Compra compra){
		final CompraDet det=new CompraDet();
		det.setSucursal(compra.getSucursal());
		det.setCompra(compra);
		return det;
	}
	
	public static EntradaPorCompra crearEntrada(CompraDet det){
		if(det==null){
            throw new IllegalArgumentException("No puede generar una compra det nula");
        }
		
        if(!det.getProducto().isInventariable())
            return null;
        EntradaPorCompra e=new EntradaPorCompra();
        e.setSucursal(det.getSucursal());
        e.setProducto(det.getProducto());
        e.setUnidad(det.getUnidad());
        e.setCantidad(det.getPendiente());
        det.agregarEntrada(e);
        e.setCostoUltimo(det.getCostoMN().amount());        
        return e;
	}
	
	
}
