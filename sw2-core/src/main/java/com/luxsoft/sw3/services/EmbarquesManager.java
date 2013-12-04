package com.luxsoft.sw3.services;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.embarque.Entrega;

public interface EmbarquesManager {
	
	public Embarque getEmbarquer(String id);
	
	public Embarque salvarEmbarque(final Embarque e,final Sucursal sucursal);
	
	public Entrega salvarEntrega(final Entrega e,final Sucursal sucursal);
	
	//public void eliminarEntrega(final Entrega e,final Sucursal sucursal);

}
