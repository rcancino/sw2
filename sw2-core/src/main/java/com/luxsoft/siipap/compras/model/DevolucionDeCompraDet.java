package com.luxsoft.siipap.compras.model;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.model.core.Producto;


/**
 * Salida unitaria de material del  inventario por concepto de 
 * devolucion de compra
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_INVENTARIO_DEC")
public class DevolucionDeCompraDet extends Inventario{
	
	
	@ManyToOne(optional=false,fetch=FetchType.LAZY)
	@JoinColumn (name="DEVOLUCION_ID",nullable=false)		
	private DevolucionDeCompra devolucion;
			
	
	
	public DevolucionDeCompraDet(){
	}
	
	public DevolucionDeCompraDet(final Producto prod){
		setProducto(prod);
	}


	public DevolucionDeCompra getDevolucion() {
		return devolucion;
	}
	public void setDevolucion(DevolucionDeCompra devolucion) {
		this.devolucion = devolucion;
	}


	@Override
	public String getTipoDocto() {
		return "DEC";
	}	



	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(getRenglon())
		.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;		
		if (getClass() != obj.getClass())
			return false;
		DevolucionDeCompraDet other = (DevolucionDeCompraDet) obj;
		return new EqualsBuilder()
		.append(getRenglon(), other.getRenglon())
		.isEquals();
	}	
	
	
	
}
