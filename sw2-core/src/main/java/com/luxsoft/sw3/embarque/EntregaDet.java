package com.luxsoft.sw3.embarque;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.ventas.model.VentaDet;

/**
 * Detalle de la entrega, es util solo para las entregas parciales 
 * 
 * @author Ruben Cancino Ramos
 * 
 */
@Entity
@Table(name="SX_ENTREGAS_DET")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)

public class EntregaDet extends BaseBean{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="ENTREGADET_ID")
	private String id;
	
	//@ManyToOne(optional = false,fetch=FetchType.EAGER)
   	@ManyToOne(optional = false)
	@JoinColumn(name = "ENTREGA_ID",nullable=false,updatable=false)
	private Entrega entrega;

   	
   	@ManyToOne(optional = false)
	@JoinColumn(name = "VENTADET_ID", nullable = false, updatable = false)
	private VentaDet ventaDet;

	@Column(name = "FACTURA", nullable = false)
	private Long factura;

	@ManyToOne(optional = false,fetch=FetchType.LAZY)
	@JoinColumn(name = "PRODUCTO_ID", nullable = false)
	@NotNull
	private Producto producto;

	@Column(name = "PRODUCTO", nullable = false, length = 20)
	private String clave;

	@Column(name = "DESCRIPCION", nullable = false, length = 250)
	private String descripcion;

	@Column(name = "CANTIDAD", nullable = false)
	private double cantidad;
	
	@Column(name = "ENTREGADO_ANTERIOR", nullable = false)
	private double entregaAnterior;

	@Column(name = "VALOR", nullable = false)
	private BigDecimal valor;
	
	
	public EntregaDet(){
		
	}
	
	public EntregaDet(VentaDet ventaDet,double entregado) {
		super();		
		this.ventaDet = ventaDet;
		setProducto(ventaDet.getProducto());
		setFactura(ventaDet.getDocumento());
		setEntregaAnterior(entregado);
		setCantidad(getPorEntregar());
	}

	public String getId() {
		return id;
	}

	public Entrega getEntrega() {
		return entrega;
	}



	public void setEntrega(Entrega entrega) {
		this.entrega = entrega;
	}
	

	public VentaDet getVentaDet() {
		return ventaDet;
	}

	public void setVentaDet(VentaDet ventaDet) {
		this.ventaDet = ventaDet;
	}

	public Long getFactura() {
		return factura;
	}

	public void setFactura(Long factura) {
		this.factura = factura;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
		if(producto!=null){
			setClave(producto.getClave());
			setDescripcion(producto.getDescripcion());
		}
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
		if(cantidad<0)
			cantidad=Math.abs(cantidad);
		
		double old=this.cantidad;
		this.cantidad = cantidad;
		firePropertyChange("cantidad", old, cantidad);
	}
	
	public double getPorEntregar(){
		return Math.abs(getVentaDet().getCantidad())-getEntregaAnterior();
	}

	public double getEntregaAnterior() {
		return entregaAnterior;
	}

	public void setEntregaAnterior(double entregaAnterior) {
		this.entregaAnterior = entregaAnterior;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
	
	public void actualizar(){
		double canti=this.cantidad/this.ventaDet.getFactor();
		CantidadMonetaria precio=CantidadMonetaria.pesos(ventaDet.getPrecio());
		
		CantidadMonetaria impBruto=precio.multiply(canti).abs();
		CantidadMonetaria descuento=impBruto.multiply(ventaDet.getDescuento()/100);
		setValor(impBruto.subtract(descuento).amount());
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((ventaDet == null) ? 0 : ventaDet.hashCode());
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
		EntregaDet other = (EntregaDet) obj;
		if (ventaDet == null) {
			if (other.ventaDet != null)
				return false;
		} else if (!ventaDet.equals(other.ventaDet))
			return false;
		return true;
	}
	
	@AssertTrue(message="La cantidad de entrega no puede superar al pendiente por entregar")
	public boolean validarCantidad(){
		return this.cantidad<=getPorEntregar();
	}
	
	public String toString(){
		return this.descripcion+"  "+this.cantidad;
	}

}
