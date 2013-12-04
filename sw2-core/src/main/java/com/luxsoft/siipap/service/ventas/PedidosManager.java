package com.luxsoft.siipap.service.ventas;

import com.luxsoft.sw3.ventas.Pedido;

public interface PedidosManager {
	
	
	public Pedido get(String id);
	
	public Pedido salvar(Pedido pedido);
	
	
}
