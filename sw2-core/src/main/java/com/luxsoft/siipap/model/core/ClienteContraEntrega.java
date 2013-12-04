package com.luxsoft.siipap.model.core;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.luxsoft.siipap.model.Autorizacion2;

/**
 * Registro de clientes habilitados para pago contra entrega
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_CLIENTES_COE")
public class ClienteContraEntrega {
	
	@OneToOne(optional=false)
    @JoinColumn(
    	name="CLIENTE_ID", unique=true, nullable=false, updatable=false)
	private Cliente cliente;
	
	
	private Autorizacion2 autorizacion;
	
	private String comentario;

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Autorizacion2 getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(Autorizacion2 autorizacion) {
		this.autorizacion = autorizacion;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	
	

}
