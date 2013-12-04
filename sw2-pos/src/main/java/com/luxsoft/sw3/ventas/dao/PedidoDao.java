package com.luxsoft.sw3.ventas.dao;

import java.util.List;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.sw3.ventas.Pedido;

public interface PedidoDao extends GenericDao<Pedido,String>{
	
	/**
	 * Regresa la lista de los pedidos pendientes por facturar
	 * 
	 * @param sucursal
	 * @return
	 */
	public List<Pedido> buscarPendientes(final Sucursal sucursal);
	
	
	public List<Pedido> buscarFacturables(final Sucursal sucursal);
	
	public List<Pedido> buscarFacturables(final Sucursal sucursal,Pedido.Tipo tipo);
	
	public Pedido buscarPorFolio(Long folio);

}
