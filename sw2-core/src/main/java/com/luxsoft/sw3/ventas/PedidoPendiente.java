package com.luxsoft.sw3.ventas;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Entidad simple para  registrar si un pedido requiere autorizacion 
 * Permite hasta 2 campos para almacenar las razones por las
 * cuales el pedido requiere ser autorizado.
 * El uso de los campos es indeterminado y se pueden segun se requiera
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_PEDIDOS_PENDIENTES")
public class PedidoPendiente {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@OneToOne(optional=false)
    @JoinColumn(name = "PEDIDO_ID",unique=true,nullable=false,updatable=false)
	private Pedido pedido;
	
		
	@Column(name="COMENTARIO")
	private String comentario;
	
	@Column(name="COMENTARIO2")
	private String comentario2;
	
	
		
	public Pedido getPedido() {
		return pedido;
	}



	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public String getComentario() {
		return comentario;
	}



	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	

	public String getComentario2() {
		return comentario2;
	}



	public void setComentario2(String comentario2) {
		this.comentario2 = comentario2;
	}


	public Long getId() {
		return id;
	}

}
