package com.luxsoft.siipap.cxp.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.validator.AssertTrue;



/**
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@DiscriminatorValue("NOTA")
public class CXPNota extends CXPAbono{
	
	@Column(name="DESCUENTO")
	private Double descuento;
	
	@Enumerated(EnumType.STRING)
    @Column (name="CONCEPTO_NOTA",nullable=true,length=25)
	private Concepto concepto=Concepto.DESCUENTO;

	public Double getDescuento() {
		return descuento;
	}

	public void setDescuento(Double descuento) {
		Object old=this.descuento;
		this.descuento = descuento;
		firePropertyChange("descuento", old, descuento);
	}	
	
	public Concepto getConcepto() {
		return concepto;
	}

	public void setConcepto(Concepto concepto) {
		Object old=this.concepto;
		this.concepto = concepto;
		firePropertyChange("concepto", old, concepto);
	}
	
	@Override
	public String getInfo() {
		return "Nota por: "+getConcepto();
	}

	@Override
	public String getTipoId() {
		if( (concepto!=null) &&  (concepto.equals(Concepto.BONIFICACION)))
			return "BONIFICACION";
		return "NOTA";
	}


	@AssertTrue(message="El concepto de la nota es mandatorio")
	public boolean validarConcepto(){
		return this.concepto!=null;
	}
	
	@AssertTrue(message="Se requiere el importe")
	public boolean validarImporte(){
		return getTotal().doubleValue()>0;
	}

	public static enum Concepto {
		DEVLUCION
		,DESCUENTO_FINANCIERO
		,DESCUENTO
		,DESCUENTO_ANTICIPO
		,BONIFICACION
	}

	

}
