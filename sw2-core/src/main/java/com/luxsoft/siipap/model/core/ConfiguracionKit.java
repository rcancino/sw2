package com.luxsoft.siipap.model.core;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;

/**
 * Mantiene la configuracion de productos kits
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_KITCONFIG")
public class ConfiguracionKit extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="CONFIG_ID")
	private Long id;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="PRODUCTO_ID", nullable=false,updatable=false)
    @NotNull
	private Producto destino;
	
	@Column(name="CLAVE",nullable=false)
	private String clave;
	
	@CollectionOfElements(fetch= FetchType.LAZY)
	@JoinTable(name="SX_KITCONFIG_PART",joinColumns=@JoinColumn( name="CONFIG_ID"))
	private Set<Elemento> partes=new HashSet<Elemento>();

	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Producto getDestino() {
		return destino;
	}
	public void setDestino(Producto destino) {
		this.destino = destino;
		this.clave=destino.getClave();
	}

	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}
	public Set<Elemento> getPartes() {
		return partes;
	}
	
	public void setPartes(Set<Elemento> partes) {
		this.partes = partes;
	}
	

	@Override
	public boolean equals(Object o) {
		if(o==null) return false;
        if(o==this) return true;
        if(getClass()!=o.getClass()) return false;
        ConfiguracionKit otro=(ConfiguracionKit)o;
        return new EqualsBuilder()
        .append(id, otro.getId())
        .append(destino, otro.getDestino())
        .append(clave, otro.getClave())
        .isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(id)
		.append(destino)
		.append(clave)
		.toHashCode();
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(clave)
		.append(id)
		.toString();
	}



	/**
	 * 
	 * @author Ruben Cancino
	 *
	 */
	@Embeddable
	public static class Elemento{
		
		@ManyToOne (optional=false)
	    @JoinColumn (name="PRODUCTO_ID", nullable=false,updatable=false)
		private Producto producto;
		
		@Column(name="CANTIDAD",nullable=false)
		private double cantidad;
		
		public Elemento(){
		}
		
		public Elemento(Producto producto,double cantidad) {
			this.cantidad = cantidad;
			this.producto = producto;
		}


		public Producto getProducto() {
			return producto;
		}
		public void setProducto(Producto producto) {
			this.producto = producto;
		}
		public double getCantidad() {
			return cantidad;
		}
		public void setCantidad(double cantidad) {
			this.cantidad = cantidad;
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
			Elemento other = (Elemento) obj;
			if (producto == null) {
				if (other.producto != null)
					return false;
			} else if (!producto.equals(other.producto))
				return false;
			return true;
		}
		
	}

}
