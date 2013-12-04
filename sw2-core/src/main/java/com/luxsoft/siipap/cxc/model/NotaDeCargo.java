package com.luxsoft.siipap.cxc.model;



import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.validator.AssertTrue;


/**
 * 
 * 
 * @author Ruben Cancino
 * TODO Hacer una catalogo de conceptos para los cargos y anexar una dependencia a esa entidad/enumeracion
 *
 */
@Entity
@DiscriminatorValue("CAR")
public class NotaDeCargo extends Cargo{
	
	@ManyToOne (optional=true)
    @JoinColumn (name="CHEQUE_ID")
	private PagoConCheque cheque;
	
		
	@Column(name="CARGO")
	private double cargo=0.0d;	

	@Override
	public String getTipoDocto() {
		return "CAR";
	} 
	
	@OneToMany(cascade={
			CascadeType.ALL,
			 CascadeType.PERSIST,
			 CascadeType.MERGE,
			 CascadeType.REMOVE
			}
			,mappedBy="notaDeCargo")	
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private Set<NotaDeCargoDet> conceptos=new HashSet<NotaDeCargoDet>();
	
	@Transient
	private boolean especial;

	public Set<NotaDeCargoDet> getConceptos() {
		return conceptos;
	}
	
	public void setConceptos(Set<NotaDeCargoDet> conceptos) {
		this.conceptos = conceptos;
	}

	public void agregarConcepto(final NotaDeCargoDet det){
		det.setNotaDeCargo(this);
		conceptos.add(det);
	}



	public double getCargo() {
		return cargo;
	}



	public void setCargo(double cargo) {
		this.cargo = cargo;
	}
	
	/*@AssertFalse(message="Cuando no hay partidas el comentario es mandatorio")
	public boolean validarComentario(){
		if(conceptos.isEmpty()){
			return StringUtils.isBlank(getComentario());
		}
		return false;
	}*/
	
	@AssertTrue(message="No ha registrado el importe del cargo")
	public boolean validarImporte(){
		return getTotal().doubleValue()>=0;
	}



	public boolean isEspecial() {
		return especial;
	}



	public void setEspecial(boolean especial) {
		this.especial = especial;
	}



	public PagoConCheque getCheque() {
		return cheque;
	}



	public void setCheque(PagoConCheque cheque) {
		this.cheque = cheque;
	}
	
	
	
	
}
