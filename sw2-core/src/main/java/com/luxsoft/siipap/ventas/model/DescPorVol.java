package com.luxsoft.siipap.ventas.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

/**
 * Descuento general por volumen
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Table(name="SX_DESC_VOL",uniqueConstraints=@UniqueConstraint(
		columnNames={"TIPO","VIGENCIA","IMPORTE","DESCUENTO"}
))
public class DescPorVol {
	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="DESCUENTO_ID")
	private Long id;
	
	@Enumerated(EnumType.STRING)
    @Column (name="TIPO",nullable=false,length=7)
	private Tipo tipo;
	
	@Column(name="ACTIVO",nullable=false)
	private boolean activo=false;
	
	@Column(name="VIGENCIA",nullable=false)
	private Date vigencia;
	
	@Column(name="IMPORTE",nullable=false)
	private BigDecimal importe=BigDecimal.ZERO;
	
	private double descuento;
	
	@Column(name="CREADO",updatable=false,insertable=false)
	@Generated(GenerationTime.INSERT)
	private Date creado;
	
	
	@Column(name="MODIFICADO",updatable=false,insertable=false)
	@Generated(GenerationTime.ALWAYS)
	private Date modificado;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public Date getVigencia() {
		return vigencia;
	}

	public void setVigencia(Date vigencia) {
		this.vigencia = vigencia;
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

	public Date getCreado() {
		return creado;
	}
	
	public Date getModificado() {
		return modificado;
	}



	public static enum Tipo{
		CREDITO,
		CONTADO
	}

}
