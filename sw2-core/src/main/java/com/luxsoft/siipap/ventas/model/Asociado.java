package com.luxsoft.siipap.ventas.model;

import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.core.Cliente;


/**
 * Entidad para hacer referencia a un cliente de un cliente
 * Actualmente solo aplica para la Union de Credito
 * 
 * @author Ruben Cancinio
 *
 */
@Entity
@Table(name="SX_SOCIOS")
public class Asociado extends BaseBean{
	
	@Id 
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column (name="SOCIO_ID")
    private Long id;    
	
	@ManyToOne(optional=false)
	@JoinColumn(name="CLIENTE_ID",nullable=false,updatable=false)
	private Cliente cliente;
	
	@Column(nullable=false,unique=true)
	@NotNull
	private String clave;
	
	@Column(nullable=false)
	private String nombre;
	
	@ManyToOne(optional=false)
	@JoinColumn(name="VENDEDOR_ID",nullable=false,updatable=false)
	private Vendedor vendedor;
	
	@Column(nullable=false)
	private String direccion;
	
	@Column(nullable=false,scale=4,precision=4)
	private double comisionVendedor;
	
	@Column(nullable=false,scale=4,precision=4)
	private double comisionCobrador;
	
	
	public Long getId() {
		return id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Vendedor getVendedor() {
		return vendedor;
	}

	public void setVendedor(Vendedor vendedor) {
		this.vendedor = vendedor;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public double getComisionVendedor() {
		return comisionVendedor;
	}

	public void setComisionVendedor(double comisionVendedor) {
		this.comisionVendedor = comisionVendedor;
	}

	public double getComisionCobrador() {
		return comisionCobrador;
	}

	public void setComisionCobrador(double comisionCobrador) {
		this.comisionCobrador = comisionCobrador;
	}

	public boolean equals(final Object obj){
		if(obj==null) return false;
		if(obj==this) return true;
		if(!getClass().isAssignableFrom(obj.getClass())) return false;
		Asociado aso=(Asociado)obj;
		return new EqualsBuilder()
		.append(getClave(), aso.getClave())
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder(17,35)
		.append(getClave())
		.toHashCode();
	}

	@Override
	public String toString() {
		String pattern="{0} ";
		return MessageFormat.format(pattern,getNombre());
	}

	
	
	

}
