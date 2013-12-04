package com.luxsoft.sw3.ventas;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.core.Producto;

@Entity
@Table
public class MedidaEspecial {
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="MEDIDA_ID")
	protected String id;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)
	@JoinColumn(name = "PRODUCTO_ID", nullable = false,updatable=false)
	@NotNull
	private Producto producto;

	@Column(name = "CLAVE", nullable = false,updatable=false)
	@Length(max = 10)
	private String clave;

	@Column(name = "DESCRIPCION", nullable = false)
	@Length(max = 250)
	private String descripcion;
	
	@Column(name="PRECIO_KILO",nullable=false)
	private BigDecimal precioPorKilo;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getPrecioPorKilo() {
		return precioPorKilo;
	}

	public void setPrecioPorKilo(BigDecimal precioPorKilo) {
		this.precioPorKilo = precioPorKilo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((producto == null) ? 0 : producto.hashCode());
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
		MedidaEspecial other = (MedidaEspecial) obj;
		if (producto == null) {
			if (other.producto != null)
				return false;
		} else if (!producto.equals(other.producto))
			return false;
		return true;
	}
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append(getProducto())
		.append(getPrecioPorKilo())
		.toString();
	}

}
