package com.luxsoft.siipap.model.gastos;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Parent;
import org.hibernate.validator.Length;

@Embeddable
public class GProductoPorProveedor {
	
	@Parent
	private GProveedor proveedor;
	
	@ManyToOne (optional=false)
	@JoinColumn (name="PRODUCTO_ID",nullable=false,updatable=false)
	private GProductoServicio producto;
	
	@Length (max=200)
	@Column (name="CLAVEPROV",length=200,nullable=false)
	private String claveProv;
	
	@Length (max=200)
	@Column (name="DESCRIPROV",length=200)
	private String descripcionProv;
	
	
	@Length (max=200)
	@Column (name="CODIGOPROV", length=200)
	private String codigoProv;

	public GProductoPorProveedor(){}

	public GProductoPorProveedor(GProveedor proveedor,
			GProductoServicio producto) {
		super();
		this.proveedor = proveedor;
		this.producto = producto;
		setClaveProv(this.producto.getClave());
		setDescripcionProv(this.producto.getDescripcion());
	}
	public GProveedor getProveedor() {
		return proveedor;
	}
	public void setProveedor(GProveedor proveedor) {
		this.proveedor = proveedor;
	}


	public GProductoServicio getProducto() {
		return producto;
	}
	public void setProducto(GProductoServicio producto) {
		this.producto = producto;
	}


	public String getClaveProv() {
		return claveProv;
	}
	public void setClaveProv(String claveProv) {
		this.claveProv = claveProv;
	}


	public String getDescripcionProv() {
		return descripcionProv;
	}
	public void setDescripcionProv(String descripcionProv) {
		this.descripcionProv = descripcionProv;
	}


	public String getCodigoProv() {
		return codigoProv;
	}
	public void setCodigoProv(String codigoProv) {
		this.codigoProv = codigoProv;
	}
	
	public boolean equals(Object o){
		if(o==null) return false;
		if(o==this) return true;
		GProductoPorProveedor otro=(GProductoPorProveedor)o;
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
		

}
