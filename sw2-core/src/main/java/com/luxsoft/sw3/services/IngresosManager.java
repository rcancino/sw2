package com.luxsoft.sw3.services;

import java.math.BigDecimal;
import java.util.Date;

import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.sw3.tesoreria.model.CorreccionDeFicha;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;




/**
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface IngresosManager {
	
	public CorteDeTarjeta registrarCorte(CorteDeTarjeta corte);
	
	public CorteDeTarjeta actualizarCorte(CorteDeTarjeta corte);
	
	public CargoAbono actualizarComisionDeAmex(Long cargoabono_id ,BigDecimal importe);
	
	/**
	 * Elimina un corte de tarjeta y todas sus entidades relacionadas
	 * 
	 * @param id
	 */
	public void eliminarCorte(Long id);
	
	public void eliminarCorte(CorteDeTarjeta corte);
	
	
	public Ficha registrarIngresoPorFicha(Ficha ficha);
	
	public CorreccionDeFicha registrarCorreccionDeFicha(CorreccionDeFicha co);
	
	public PagoConDeposito registrarIngreso(PagoConDeposito pago);
	
	public PagoConTarjeta registrarIngreso(PagoConTarjeta pago);
	
	public void correccionDeFecha(final CargoAbono ca,final Date fecha);
	
	public CargoAbono correccionDeFechaCobro(final CargoAbono cheque,final Date fecha);
	
	public void correccionDeFechaDeposito(CargoAbono ca, Date fechaDeposito);

}
