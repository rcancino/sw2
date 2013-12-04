package com.luxsoft.siipap.ventas.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.luxsoft.siipap.model.core.Cliente;

/**
 * Descuento por volumen para un cliente en especifico
 *  
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_DESC_VOL_CLIE",uniqueConstraints=@UniqueConstraint(
		columnNames={"TIPO","VIGENCIA","IMPORTE","DESCUENTO","CLIENTE_ID"}
))
public class DescPorVolCliente extends DescPorVol{
	
	@ManyToOne(optional=false)
	@JoinColumn(name="CLIENTE_ID",nullable=false,updatable=false)
	private Cliente cliente;

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	

}
