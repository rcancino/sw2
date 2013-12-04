package com.luxsoft.sw3.services;

import java.util.List;

import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.sw3.caja.Caja;

public interface CorteDeCajaManager {
	
	public Caja registrarCorteDeCaja(Caja caja);
	
	public Caja registrarCambioDeCheque(Caja caja);
	
	public Caja registrarCambioDeTarjeta(Caja source);
	
	public Caja registrarCorteDeCajaCheque(Caja caja,List<Ficha> fichasParaCheque);
	
	public Caja registrarCorteDeCajaTarjeta(Caja caja);
	
	public Caja registrarCorteDeCajaDeposito(Caja caja);

}
