package com.luxsoft.sw3.maquila.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.core.Producto;

/**
 * Salida de material cortado del inventario hacia las sucursales y/o punto de venta
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_MAQ_SALIDA_HOJEADODET")
public class SalidaDeHojasDet {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "SALIDADET_ID")
	private Long id;
	
	@Column(name="FECHA",nullable=false)
	private Date fecha=new Date();
	
	/*@ManyToOne(optional = false)
	@JoinColumn(name = "SALIDA_ID",nullable=false,updatable=false)
	private SalidaDeHojas salida;*/
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "RECEPCIONDET_ID",nullable=false,updatable=false)
	private RecepcionDeCorteDet origen;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "INVENTARIO_ID",nullable=false,updatable=false)
	private EntradaDeMaquila destino;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER
			,cascade={CascadeType.MERGE,CascadeType.PERSIST})
	@JoinColumn(name = "PRODUCTO_ID", nullable = false)
	@NotNull(message="El producto destino es mandatorio")
	private Producto producto;
	
	@Column(name="CANTIDAD")
	private double cantidad;
	
	@Column(name = "COSTO", nullable = true)	
	private double costo;	
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	

	public SalidaDeHojasDet() {}

/*
	public SalidaDeHojas getSalida() {
		return salida;
	}


	public void setSalida(SalidaDeHojas salida) {
		this.salida = salida;
	}*/


	public RecepcionDeCorteDet getOrigen() {
		return origen;
	}


	public void setOrigen(RecepcionDeCorteDet origen) {
		if(getProducto()!=null)
			Assert.isTrue(origen.getProducto().equals(getProducto()));
		this.origen = origen;
		
	}


	public EntradaDeMaquila getDestino() {
		return destino;
	}


	public void setDestino(EntradaDeMaquila destino) {
		this.destino = destino;
	}


	public double getCantidad() {
		return cantidad;
	}

	public double getCantidadMillares(){
		return cantidad/1000;
	}

	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}


	public double getCosto() {
		return costo;
	}


	public void setCosto(double costo) {
		this.costo = costo;
	}


	public String getComentario() {
		return comentario;
	}


	public void setComentario(String comentario) {
		this.comentario = comentario;
	}


	public Long getId() {
		return id;
	}

	

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Producto getProducto() {
		return producto;
	}


	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public BigDecimal getCostoPorMillar(){
		if(getCantidadMillares()>0)
			return BigDecimal.valueOf(getCosto()/getCantidadMillares()).setScale(2,RoundingMode.HALF_EVEN);
		return BigDecimal.ZERO;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,75)
		.append(getProducto())
		.append(getOrigen())
		.toHashCode();
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SalidaDeHojasDet other = (SalidaDeHojasDet) obj;
		return new EqualsBuilder()
		.append(getProducto(), other.getProducto())
		.append(getOrigen(), other.getOrigen())
		.isEquals();
	}

		

	

}
