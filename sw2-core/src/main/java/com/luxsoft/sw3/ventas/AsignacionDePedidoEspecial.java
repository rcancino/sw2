package com.luxsoft.sw3.ventas;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.NotNull;

/**
 * Entidad para registrar los pedidos especiales atendidos por entradas por compra
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_PEDIDOSDET_ASIG_ESP")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
	,parameters={
			@Parameter(name="separator",value="-")
		}
	)
public class AsignacionDePedidoEspecial {
	

	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="ID")
	protected String id;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)
	@JoinColumn(name = "PEDIDODET_ID", nullable = false)
	@NotNull
	private PedidoDet pedidoDet;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "TIPO", nullable = false, length = 5)
	private Tipo tipo=Tipo.COM;
	
	
	@Column(name="ENTRADA_ID",nullable=false)
	@NotNull
	private String entrada;
	
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PedidoDet getPedidoDet() {
		return pedidoDet;
	}

	public void setPedidoDet(PedidoDet pedidoDet) {
		this.pedidoDet = pedidoDet;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public String getEntrada() {
		return entrada;
	}

	public void setEntrada(String entrada) {
		this.entrada = entrada;
	}		

	
	public static enum Tipo{
		COM,MAQ
	}

}
