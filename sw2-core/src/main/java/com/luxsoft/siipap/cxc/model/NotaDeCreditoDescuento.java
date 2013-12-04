package com.luxsoft.siipap.cxc.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Abono de un cliente mediante nota de credito para uso exclusivo
 * de descuentos
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@DiscriminatorValue("NOTA_DES")
public class NotaDeCreditoDescuento extends NotaDeCredito{

	@Enumerated(EnumType.STRING)
    @Column (name="TIPO",nullable=true,length=15)
	private TipoDeDescuento tipoDeDescuento=TipoDeDescuento.ASIGNADO;
	
	public TipoDeDescuento getTipoDeDescuento() {
		return tipoDeDescuento;
	}
	public void setTipoDeDescuento(TipoDeDescuento tipoDeDescuento) {
		this.tipoDeDescuento = tipoDeDescuento;
	}

	public String getInfo() {
		return "DESC "+getTipoDeDescuento().name();
	}
	
	@Override
	public String getTipoSiipap() {
		return getTipoDeDescuento().equals(TipoDeDescuento.ASIGNADO)?"U":"V";
	}
	

	/**
	 * En una nota de credito por descuento el total se actualiza en funcion
	 * de sus partidas
	 * 
	 */
	@Override
	public void actualizarTotal() {
		CantidadMonetaria total=CantidadMonetaria.pesos(0);
		for(Aplicacion a:getAplicaciones()){
			CantidadMonetaria imp=CantidadMonetaria.pesos(a.getImporte().doubleValue());
			total=total.add(imp);
		}
		setTotal(total.amount());
		setImporte(MonedasUtils.calcularImporteDelTotal(total).amount());
		setImpuesto(MonedasUtils.calcularImpuesto(getImporte()));

	}

	/*
	public boolean equals(Object obj){
		NotaDeCreditoDescuento other=(NotaDeCreditoDescuento)obj;
		return new EqualsBuilder()
		.appendSuper(super.equals(other))
		.append(this.tipoDeDescuento, other.getTipoDeDescuento())
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder(27,67)
		.appendSuper(super.hashCode())
		.append(this.tipoDeDescuento)
		.toHashCode();
	}
*/

	public static enum TipoDeDescuento{
		ASIGNADO,
		FINANCIERO,
		ADICIONAL
		
	}
	

	
}
