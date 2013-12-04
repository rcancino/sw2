package com.luxsoft.siipap.model.core;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.luxsoft.siipap.model.Autorizacion2;

/**
 * Autorizacion para que un cliente reciba pagos contra entrega
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@DiscriminatorValue("AUT_CLIENTE_PCE")
public class AutorizacionClientePCE extends Autorizacion2{
	
	public static final String DESCRIPCION="CLIENTE AUTORIZADO PARA PAGOS CONTRA ENTREGA";
	
	@OneToOne(optional=true)
    @JoinColumn(
    	name="CLIENTE_ID", unique=true, nullable=true, updatable=true)
	private Cliente cliente;

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	public AutorizacionClientePCE() {
		setComentario(DESCRIPCION);
	}

	public AutorizacionClientePCE(Cliente cliente) {
		this();
		this.cliente = cliente;
		this.cliente.setAutorizacionPagoContraEntrega(this);
	}
	

}
