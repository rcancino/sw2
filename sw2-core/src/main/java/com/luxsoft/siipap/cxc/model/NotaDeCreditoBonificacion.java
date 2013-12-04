package com.luxsoft.siipap.cxc.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.validator.NotNull;

/**
 * Abono de un cliente mediante nota de credito para uso exclusivo
 * de bonificaciones
 * 
 * @author Ruben Cancino
 *
 */
@Entity
//@Table(name="SX_CXC_NOTAS_BONIFIC")
@DiscriminatorValue("NOTA_BON")
public class NotaDeCreditoBonificacion extends NotaDeCredito{
	
	@Enumerated(EnumType.STRING)
    @Column (name="CONCEPTO",nullable=true,length=15)
    @NotNull(message="El concepto es mandatorio")
	private Concepto concepto=Concepto.BONIFICACION;
	
	
	@Enumerated(EnumType.STRING)
    @Column (name="MODO_CALCULO",nullable=true,length=15)
    @NotNull(message="El modo de calculo es mandatorio")
	private ModeloDeCalculo modo=ModeloDeCalculo.DESCUENTO;
	
	
	public NotaDeCreditoBonificacion() {}
	
	public NotaDeCreditoBonificacion(Concepto concepto) {
		this.concepto = concepto;
	}

	public Concepto getConcepto() {
		return concepto;
	}

	public void setConcepto(Concepto concepto) {
		Object old=this.concepto;
		this.concepto = concepto;
		firePropertyChange("concepto", old, concepto);
	}	
	
	public ModeloDeCalculo getModo() {
		return modo;
	}

	public void setModo(ModeloDeCalculo modo) {
		Object old=this.modo;
		this.modo = modo;
		firePropertyChange("modo", old, modo);
	}

	
	@Override
	public String getInfo() {
		return getConcepto().name();
	}
	

	@Override
	public String getTipoSiipap() {
		return "L";
	}




	public static enum Concepto{
		RECLAMACION,
		BONIFICACION,
	}
	
	/**
	 * Enumeracion que define las posibles modalidades para la forma
	 * del calculo tanto del importe de la nota de credito
	 * como del importe de sus partidas
	 * 
	 * @author Ruben Cancino
	 *
	 */
	public static enum ModeloDeCalculo{
		
		/**
		 * El importe de las aplicaciones es en funcion al descuento
		 * establecido en la nota de credito y al saldo de la cuenta por cobrar
		 * 
		 * El total de la nota de credito es la suma de los importes de de 
		 * las aplicaciones
		 * 
		 * Como resultado del anterior la nota REQUIERE de aplicaciones para
		 * poder persistir
		 */
		DESCUENTO		
		
		
		/**
		 * El total de la nota de credito es definido independiente de las 
		 * aplicaciones
		 * 
		 * El importe de las aplicaciones es calculado prorrateando el total
		 * de la nota.
		 * 
		 * En este modo no es obligatorio la existencia de aplicaciones
		 * 
		 *  
		 */
		,PRORREATAR
		
	}
	
}
