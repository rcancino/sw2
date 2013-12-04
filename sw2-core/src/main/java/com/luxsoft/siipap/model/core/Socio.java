package com.luxsoft.siipap.model.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.ventas.model.Vendedor;

@Entity
@Table(name="SX_SOCIOS")
public class Socio extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="SOCIO_ID")
	private Long id;
	
	private String clave="NX";
	
	private String nombre;
	
	private double comisionCobrador;
	
	private double comisionVendedor;
	
	private String direccion;
	
	@ManyToOne(optional = true)			
	@JoinColumn(name = "VENDEDOR_ID",nullable=true)
	private Vendedor vendedor;
	
	
	@ManyToOne(optional = true)			
	@JoinColumn(name = "CLIENTE_ID",nullable=false)
	private Cliente cliente;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		Object old=this.clave;
		this.clave = clave;
		firePropertyChange("clave", old, clave);
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		Object old=this.nombre;
		this.nombre = nombre;
		firePropertyChange("nombre", old, nombre);
	}

	public double getComisionCobrador() {
		return comisionCobrador;
	}

	public void setComisionCobrador(double comisionCobrador) {
		this.comisionCobrador = comisionCobrador;
	}

	public double getComisionVendedor() {
		return comisionVendedor;
	}

	public void setComisionVendedor(double comisionVendedor) {
		double old=this.comisionVendedor;
		this.comisionVendedor = comisionVendedor;
		firePropertyChange("comisionVendedor", old, comisionVendedor);
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		Object old=this.direccion;
		this.direccion = direccion;
		firePropertyChange("direccion", old, direccion);
	}

	public Vendedor getVendedor() {
		return vendedor;
	}

	public void setVendedor(Vendedor vendedor) {
		Object old=this.vendedor;
		this.vendedor = vendedor;
		firePropertyChange("vendedor", old, vendedor);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Socio other = (Socio) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getNombre();
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	

	
	
}
