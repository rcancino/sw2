/*
 *  Copyright 2008 Ruben Cancino Ramos.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.luxsoft.siipap.ventas.model;

import java.math.BigDecimal;
import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.core.Producto;


/**
 * Entidad para el estado de las partidas de una venta
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name = "SX_VENTASDET")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class VentaDet2 {
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="VENTADET_ID")
	private String id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "VENTA_ID", nullable = false, updatable = false,insertable=false)
    private Venta venta;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "PRODUCTO_ID", nullable = false)
    private Producto producto;
    
    @Column(name="CLAVE",nullable=false,updatable=false)
    @NotNull  @Length(max=10)
    private String clave;
    
    @Column(name="DESCRIPCION",nullable=false)
    @NotNull  @Length(max=250)
    private String descripcion;
    
    @Column(name="CANTIDAD",nullable=false)
    private double cantidad;
    
    @Column(name="FACTORU",nullable=false)
	private double factor;
    
    @Column(name = "PRECIO_L", scale=6,precision=14,nullable=false)
    private BigDecimal precioLista;
    
    @Column(name = "PRECIO", scale=6,precision=14,nullable=false)
    private BigDecimal precio; 
    
    @Column(name = "IMPORTE", scale=6,precision=14,nullable=false)
    private BigDecimal importe=BigDecimal.ZERO;
    
    
    @Column(name = "DSCTO",scale=6,precision=6,nullable=false)
    private double descuento = 0;
    
    @Column(name = "SUBTOTAL", scale=6,precision=14,nullable=false)
    private BigDecimal subTotal=BigDecimal.ZERO;
    
    @Column(name = "DSCTO_NOTA",scale=6,precision=6,nullable=false)
    private double descuentoNota=0;
    
    
    @Column(name = "IMPORTE_NETO", scale=6,precision=14,nullable=false)
    private BigDecimal importeNeto=BigDecimal.ZERO;
   
    
    @Column(name = "CORTES", nullable=false)
    private int cortes=0;
    
    @Column(name = "PRECIO_CORTES", scale=6,precision=14,nullable=false)
    private BigDecimal precioCorte=BigDecimal.ZERO;
    
    @Column(name = "DSCTO_ORIG",scale=6,precision=6,nullable=false)
    private double descuentoOriginal=0;
    
   
    /** Propiedades por compatibilidad con replica de SIIPAP DBF **/    
    
    
    @Column(name="SERIE",length=1)
    private String serie;
    
    @Column(name="TIPO",length=1)
    private String tipo;
    
    private int numero=0;
    
    @Column(name="RGL")
    private int renglon=0;
    
    @Column(name="SIIPAPWIN_ID",nullable=true)
    private Long siipapWinId;
    

    public VentaDet2() {
    }

    public String getId() {
		return id;
	}

	public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }    

    
	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
		this.clave=producto.getClave();
		this.descripcion=producto.getDescripcion();
		this.factor=producto.getUnidad().getFactor();
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

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public BigDecimal getPrecioLista() {
		return precioLista;
	}

	public void setPrecioLista(BigDecimal precioLista) {
		this.precioLista = precioLista;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		this.descuento = descuento;
	}

	public BigDecimal getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(BigDecimal subTotal) {
		this.subTotal = subTotal;
	}

	public double getDescuentoNota() {
		return descuentoNota;
	}

	public void setDescuentoNota(double descuentoNota) {
		this.descuentoNota = descuentoNota;
	}

	public BigDecimal getImporteNeto() {
		return importeNeto;
	}	

	public double getDescuentoOriginal() {
		return descuentoOriginal;
	}

	public void setDescuentoOriginal(double descuentoOriginal) {
		this.descuentoOriginal = descuentoOriginal;
	}

	public void setImporteNeto(BigDecimal importeNeto) {
		this.importeNeto = importeNeto;
	}

	public int getCortes() {
		return cortes;
	}

	public void setCortes(int cortes) {
		this.cortes = cortes;
	}

	public BigDecimal getPrecioCorte() {
		return precioCorte;
	}

	public void setPrecioCorte(BigDecimal precioCorte) {
		this.precioCorte = precioCorte;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}
	
	public int getRenglon() {
		return renglon;
	}

	public void setRenglon(int renglon) {
		this.renglon = renglon;
	}
	
	

	public Long getSiipapWinId() {
		return siipapWinId;
	}

	public void setSiipapWinId(Long siipapWinId) {
		this.siipapWinId = siipapWinId;
	}
	
	/**
	 * Regresa el importe de la provision historica 
	 * de la partida
	 * @return
	 */
	public BigDecimal getProvision(){
		BigDecimal provision=BigDecimal.ZERO;
		if(descuentoNota>0){
			BigDecimal descNota=BigDecimal.valueOf(descuentoNota/100);
			provision=importe.multiply(descNota);
		}
		return provision.abs();
	}

	public boolean isEquals(Object other){
		if(other==null) return false;
		if(other==this) return true;
		if(getClass()!=other.getClass()) return false;
		VentaDet2 det=(VentaDet2)other;
		return new EqualsBuilder()
		.append(clave, det.getClave())
		.append(cantidad, det.getCantidad())
		.append(precio,det.getPrecio())
		.isEquals();
		
	}
	
	public int hashCode(){
		return new HashCodeBuilder(17,35)
		.append(clave)
		.append(cantidad)
		.append(precio)
		.toHashCode();
	}
	
	public String toString(){
		String pattern="{0} {1} {2} {3}";
		return MessageFormat.format(pattern, clave,descripcion,cantidad,importeNeto);
	}
    
}
