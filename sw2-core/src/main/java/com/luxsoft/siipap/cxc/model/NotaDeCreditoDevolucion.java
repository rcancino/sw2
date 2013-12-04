package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;

/**
 * Abono de un cliente mediante nota de credito para uso exclusivo
 * en devoluciones de material
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@DiscriminatorValue("NOTA_DEV")
public class NotaDeCreditoDevolucion extends NotaDeCredito{
	
	@ManyToOne(optional = true
			//,cascade={CascadeType.MERGE,CascadeType.PERSIST}
	)			
	@JoinColumn(name = "DEVOLUCION_ID", nullable = true)	
	private Devolucion devolucion;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "TIPO_DEV", nullable = true, length = 10)
	private TipoDeDevolucion tipoDeDevolucion;
	
	
	

	public Devolucion getDevolucion() {
		return devolucion;
	}

	public TipoDeDevolucion getTipoDeDevolucion() {
		return tipoDeDevolucion;
	}

	public void setTipoDeDevolucion(TipoDeDevolucion tipoDeDevolucion) {
		this.tipoDeDevolucion = tipoDeDevolucion;
	}

	public void setDevolucion(Devolucion devolucion) {
		this.devolucion = devolucion;
		if((devolucion!=null) && (devolucion.getVenta()!=null))
			setCliente(devolucion.getVenta().getCliente());
	}	
	
	
	public String getInfo() {
		return "DEVOLUCION";
	}
	
	public String getTipoSiipap(){
		return "J";
	}
	
	
	public static enum TipoDeDevolucion{
		TOTAL,PARCIAL
	}
	
	//@AssertTrue(message="Se requiere la devolucion")
	public boolean validarDevolucion(){
		return getDevolucion()!=null;
	}
	/*
	public boolean equals(Object obj){
		NotaDeCreditoDevolucion other=(NotaDeCreditoDevolucion)obj;
		return new EqualsBuilder()
		.appendSuper(super.equals(other))
		.append(this.devolucion, other.getDevolucion())
		.isEquals();
	}
	public int hashCode(){
		return new HashCodeBuilder(27,85)
		.appendSuper(super.hashCode())
		.append(this.devolucion)
		.toHashCode();
	}*/
	
	public void cancelar(){
		for(DevolucionDeVenta dv:getDevolucion().getPartidas()){
			dv.setNota(null);
		}
		setDevolucion(null);
		
	}
	
	@Transient
	private BigDecimal importeBruto = BigDecimal.ZERO;
	
	//@Column(name = "TOTAL", nullable = false)
	@Transient
	private BigDecimal maniobras = BigDecimal.ZERO;
	
	@Transient
	private BigDecimal cortes = BigDecimal.ZERO;
	
	//@Column(name = "TOTAL", nullable = false)
	@Transient
	private BigDecimal descuentos = BigDecimal.ZERO;

	public BigDecimal getManiobras() {
		return maniobras;
	}

	public void setManiobras(BigDecimal maniobras) {
		this.maniobras = maniobras;
	}

	public BigDecimal getDescuentos() {
		return descuentos;
	}

	public void setDescuentos(BigDecimal descuentos) {
		this.descuentos = descuentos;
	}

	public BigDecimal getImporteBruto() {
		return importeBruto;
	}

	public void setImporteBruto(BigDecimal importeBruto) {
		this.importeBruto = importeBruto;
	}

	public BigDecimal getCortes() {
		return cortes;
	}

	public void setCortes(BigDecimal cortes) {
		this.cortes = cortes;
	}
	
	

}
