package com.luxsoft.sw3.model;

import java.math.BigDecimal;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;

/**
 * JavaBean con informacion detallada de un producto
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ProductoInfo {
	
	private Producto producto;
	
	private double existencia;
	
	
	public ProductoInfo(Producto p) {
		setProducto(p);
	}
	
	public Producto getProducto() {
		return producto;
	}
	public void setProducto(Producto producto) {
		this.producto = producto;		
	}
	
	public Existencia getExistencia(final Sucursal s){
		return (Existencia)CollectionUtils.find(producto.getExistencias(), new Predicate(){
			public boolean evaluate(Object object) {
				Existencia exis=(Existencia)object;
				return exis.getSucursal().equals(s);
			}
		});
	}
	
	public BigDecimal getPrecioContadoMN(){
		return BigDecimal.valueOf(this.producto.getPrecioContado());
	}
	
	public BigDecimal getPrecioCreditoMN(){
		return BigDecimal.valueOf(this.producto.getPrecioCredito());
	}

	public double getExistencia() {
		return existencia;
	}

	public void setExistencia(double existencia) {
		this.existencia = existencia;
	}
	
	public void setExistencia(Existencia ex){
		if(ex!=null)
			setExistencia(ex.getCantidad());
	}

}
