package com.luxsoft.siipap.model.core;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Parent;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.model.BaseBean;

@Embeddable
public class ProductoPorProveedor extends BaseBean{
	
	@Parent
	private Proveedor proveedor;
	
	@ManyToOne (optional=false)
	@JoinColumn (name="PRODUCTO_ID",nullable=false,updatable=false)
	private Producto producto;
	
	@Length (max=200)
	@Column (name="CLAVEPROV",length=200,nullable=false)
	private String claveProv;
	
	@Length (max=200)
	@Column (name="DESCRIPROV",length=200)
	private String descripcionProv;
	
	
	@Length (max=200)
	@Column (name="CODIGOPROV", length=200)
	private String codigoProv;
	
	@Column (name="PIEZAPAQ")
	private int piezasPorPaquete=0;
	
	@Column (name="PAQTARIMA")
	private int paquetesPorTarima=0;

	public ProductoPorProveedor(){}

	public ProductoPorProveedor(Proveedor proveedor,
			Producto producto) {
		super();
		this.proveedor = proveedor;
		this.producto = producto;
		setClaveProv(this.producto.getClave());
		setDescripcionProv(this.producto.getDescripcion());
	}
	public Proveedor getProveedor() {
		return proveedor;
	}
	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}


	public Producto getProducto() {
		return producto;
	}
	public void setProducto(Producto producto) {
		Object old=this.producto;
		this.producto = producto;
		firePropertyChange("producto", old, producto);
	}


	public String getClaveProv() {
		return claveProv;
	}
	public void setClaveProv(String claveProv) {
		Object old=this.claveProv;
		this.claveProv = claveProv;
		firePropertyChange("claveProv", old, claveProv);
	}


	public String getDescripcionProv() {
		return descripcionProv;
	}
	public void setDescripcionProv(String descripcionProv) {
		Object old=this.descripcionProv;
		this.descripcionProv = descripcionProv;
		firePropertyChange("descripcionProv", old, descripcionProv);
	}


	public String getCodigoProv() {
		return codigoProv;
	}
	public void setCodigoProv(String codigoProv) {
		this.codigoProv = codigoProv;
	}
	
	public int getPiezasPorPaquete() {
		return piezasPorPaquete;
	}

	public void setPiezasPorPaquete(int piezasPorPaquete) {
		this.piezasPorPaquete = piezasPorPaquete;
	}

	public int getPaquetesPorTarima() {
		return paquetesPorTarima;
	}

	public void setPaquetesPorTarima(int paquetesPorTarima) {
		this.paquetesPorTarima = paquetesPorTarima;
	}

	public boolean equals(Object o){
		if(o==null) return false;
		if(o==this) return true;
		if(getClass()!=o.getClass())
			return false;
		ProductoPorProveedor otro=(ProductoPorProveedor)o;
		return new EqualsBuilder()
		.append(getProveedor(),otro.getProveedor())
		.append(getProducto(),otro.getProducto())
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder(17,35)
		.append(getProveedor())
		.append(getProducto())
		.toHashCode();
	}

	
	@Override
	public String toString() {
		return getClaveProv();
	}
		

}
