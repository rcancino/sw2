package com.luxsoft.siipap.ventas.service;


import java.util.List;

import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVenta;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVentaDet;


public interface ListaDePreciosVentaManager {
	
	public ListaDePreciosVenta get(Long  listaId);
	
	public ListaDePreciosVenta salvar(ListaDePreciosVenta lista);
	
	public void eliminar(ListaDePreciosVenta lsita);	
	
	public List<ListaDePreciosVentaDet> buscarPartidas(ListaDePreciosVenta lista);
	
	public ListaDePreciosVenta aplicar(ListaDePreciosVenta lista,User user);
	
	public ListaDePreciosVenta copiar(ListaDePreciosVenta lista);

}
