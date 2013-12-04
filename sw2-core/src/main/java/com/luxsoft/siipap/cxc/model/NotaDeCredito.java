package com.luxsoft.siipap.cxc.model;



import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cascade;


/**
 * Abono de un cliente mediante nota de credito
 * 
 * @author Ruben Cancino
 *
 */
@Entity
public abstract class NotaDeCredito extends Abono{
	
	
	
	@Column(name="DESCUENTO",nullable=true)
	private double descuento=0;
	
	
	@Column(name="COMENTARIO2")
	private String comentario2;

	@Column(name="IMPRESO",nullable=true)	
	private Date impreso;
	
	@Column(name="APLICABLE",nullable=true)	
	private Boolean aplicable=Boolean.FALSE;
	
	@OneToMany(cascade={
			CascadeType.ALL,
			 CascadeType.PERSIST,
			 CascadeType.MERGE,
			 CascadeType.REMOVE
			}
			,mappedBy="nota")	
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	protected Set<NotaDeCreditoDet> conceptos=new HashSet<NotaDeCreditoDet>();
		
	
	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		double old=this.descuento; 
		this.descuento = descuento;
		firePropertyChange("descuento", old, descuento);
	}
	
	public String getComentario2() {
		return comentario2;
	}

	public void setComentario2(String comentario2) {
		Object old=this.comentario2;
		this.comentario2 = comentario2;
		firePropertyChange("comentario2", old, comentario2);
	}

	public Date getImpreso() {
		return impreso;
	}

	public void setImpreso(Date impreso) {
		this.impreso = impreso;
	}

	
	
	public Boolean getAplicable() {
		if(aplicable==null)
			aplicable=Boolean.FALSE;
		return aplicable;
	}

	public void setAplicable(Boolean aplicable) {
		this.aplicable = aplicable;
	}

	public String getTipoSiipap(){
		return "?";
	}

	
	
	/**
	 * Comoditi para actualizar algunas columnas en la tabla
	 * de aplicaciones
	 * Normalmente llamado desde el dao de persistencia
	 * 
	 */
	public void actualizarDetalleEnAplicaciones(){
		for(Aplicacion a:getAplicaciones()){
			if(a!=null)
				a.actualizarDetalle();
		}
	}

	@Override
	public boolean requiereAutorizacion() {
		return getAplicable()?true:false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)		
		.appendSuper(super.hashCode())
		//.append(this.getFolio())
		.append(this.id)
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
		final NotaDeCredito other = (NotaDeCredito) obj;
		return new EqualsBuilder()
		.appendSuper(super.equals(other))
		//.append(this.getFolio(),other.getFolio())
		.append(this.getId(), other.getId())
		.isEquals();
	}
	
	/**
	 * Abstract method para preparar la cancelacion de una nota de credito
	 * 
	 */
	public void cancelar(){
		
	}

	public Set<NotaDeCreditoDet> getConceptos() {
		return conceptos;
	}

	public void setConceptos(Set<NotaDeCreditoDet> conceptos) {
		this.conceptos = conceptos;
	}
	
	public NotaDeCreditoDet agregarConcepto(){
		NotaDeCreditoDet det=new NotaDeCreditoDet();
		det.setNota(this);
		conceptos.add(det);
		return det;
	}
	
}
