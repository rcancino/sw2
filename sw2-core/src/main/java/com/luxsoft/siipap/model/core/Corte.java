package com.luxsoft.siipap.model.core;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "SX_CORTES")
public class Corte implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "CORTE_ID")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "TIPO", nullable = false, length = 15)
	private Tipo tipo;

	@Column(name = "PRECIO_CREDITO")
	private BigDecimal precioCredito = BigDecimal.ZERO;

	@Column(name = "PRECIO_CONTADO")
	private BigDecimal precioContado = BigDecimal.ZERO;

	@Column(name = "MAXIMO_HOJAS")
	private int maximoDeHojas = 1000;
	
	@Column(name="LINEA_ID")
	private Long linea;

	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="corte")
	@Fetch(FetchMode.SUBSELECT)
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private Set<MedidaPorCorte> medidas=new HashSet<MedidaPorCorte>();
	
	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public BigDecimal getPrecioCredito() {
		return precioCredito;
	}

	public void setPrecioCredito(BigDecimal precioCredito) {
		this.precioCredito = precioCredito;
	}

	public BigDecimal getPrecioContado() {
		return precioContado;
	}

	public void setPrecioContado(BigDecimal precioContado) {
		this.precioContado = precioContado;
	}

	public int getMaximoDeHojas() {
		return maximoDeHojas;
	}

	public void setMaximoDeHojas(int maximoDeHojas) {
		this.maximoDeHojas = maximoDeHojas;
	}

	public Long getId() {
		return id;
	}
	
	
	
	public Long getLinea() {
		return linea;
	}

	public void setLinea(Long linea) {
		this.linea = linea;
	}
	
	

	public Set<MedidaPorCorte> getMedidas() {
		return medidas;
	}
	
	public void agregarMedida(int cortes,double min,double max){
		MedidaPorCorte m=new MedidaPorCorte();
		m.setCortes(cortes);
		m.setLongitudMinima(min);
		m.setLongitudMaxima(max);
		agregarMedida(m);
	}

	public boolean agregarMedida(MedidaPorCorte m){
		m.setCorte(this);
		return medidas.add(m);
	}

	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + maximoDeHojas;
		result = prime * result
				+ ((precioContado == null) ? 0 : precioContado.hashCode());
		result = prime * result
				+ ((precioCredito == null) ? 0 : precioCredito.hashCode());
		result = prime * result + ((tipo == null) ? 0 : tipo.hashCode());
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
		Corte other = (Corte) obj;
		if (maximoDeHojas != other.maximoDeHojas)
			return false;
		if (precioContado == null) {
			if (other.precioContado != null)
				return false;
		} else if (!precioContado.equals(other.precioContado))
			return false;
		if (precioCredito == null) {
			if (other.precioCredito != null)
				return false;
		} else if (!precioCredito.equals(other.precioCredito))
			return false;
		if (tipo == null) {
			if (other.tipo != null)
				return false;
		} else if (!tipo.equals(other.tipo))
			return false;
		return true;
	}



	public static enum Tipo {
		SENCILLO, DOBLE, HOJAS,ESPECIAL
	}

}
