package com.luxsoft.siipap.cxc.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Cargo para diferencias cambiaria,otros productos
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@DiscriminatorValue("DIF")
public class CargoPorDiferencia extends Cargo{
	
	@Enumerated(EnumType.STRING)
	@Column (name="DIF_CONCEPTO",nullable=true,length=15)
	private TipoDiferencia tipoDiferencia;

	@Override
	public String getTipoDocto() {
		return "DIF";
	}
	
	
	
	public TipoDiferencia getTipoDiferencia() {
		return tipoDiferencia;
	}



	public void setTipoDiferencia(TipoDiferencia tipoDiferencia) {
		this.tipoDiferencia = tipoDiferencia;
	}



	public static enum TipoDiferencia{
		CAMBIARIA,PRODUCTO,GASTO
	}

}
