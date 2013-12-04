package com.luxsoft.siipap.model.tesoreria;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name="SW_CONCILIACION")
public class Conciliacion {
	
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="CONCIL_ID")
	private Long id;
	
	/**
	 * Fecha del banco
	 */
	@Column(name="FECHA",nullable=false)
	@Type(type="date")
	private Date fecha;
	
	/**
	 * Referencia Bancaria
	 */
	private String referencia;
	
	private BigDecimal importeBanco;
	
	private BigDecimal saldoBanco;
	
	@Column(name="COMENTARIO",length=30)
	private String comentario;
	
	@OneToOne
    @JoinTable(
    		name="SW_CARGOS_CONCILIADOS"
    		,joinColumns=@JoinColumn(name="CONCIL_ID")
    		,inverseJoinColumns=@JoinColumn(name="CARGOABONO_ID")
    		)
	private CargoAbono cargoabono;
	
	public Conciliacion() {}

	public Conciliacion(CargoAbono cargoabono) {		
		this.cargoabono = cargoabono;
	}

	public CargoAbono getCargoabono() {
		return cargoabono;
	}

	public void setCargoabono(CargoAbono cargoabono) {
		this.cargoabono = cargoabono;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getImporteBanco() {
		return importeBanco;
	}

	public void setImporteBanco(BigDecimal importeBanco) {
		this.importeBanco = importeBanco;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	public BigDecimal getSaldoBanco() {
		return saldoBanco;
	}

	public void setSaldoBanco(BigDecimal saldoBanco) {
		this.saldoBanco = saldoBanco;
	}
	
	

}
