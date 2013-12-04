package com.luxsoft.siipap.cxc.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Entidad de pagos con tarjeta de credito
 * 
 * @author Ruben Cancino
 *
 */
@Entity
//@Table(name="SX_CXC_PAGOS_TARJETA")
@DiscriminatorValue("PAGO_TAR")
public class PagoConTarjeta extends Pago{
	
	@ManyToOne(optional = true)			
	@JoinColumn(name = "TARJETA_ID", nullable = true)
	private Tarjeta tarjeta;
	
	@ManyToOne(optional=true)
	@JoinColumn(name = "ESQUEMA_ID", nullable = true)
	private Esquema esquema;
	
	@Column(name="COMISION_TARJETA")
	private double comisionBancaria;
	
	@Column(name="AUTO_TARJETA_BANCO",nullable=true)
	private String autorizacionBancaria;

	public Tarjeta getTarjeta() {
		return tarjeta;
	}

	public void setTarjeta(Tarjeta tarjeta) {
		Object old=this.tarjeta;
		this.tarjeta = tarjeta;
		firePropertyChange("tarjeta", old, tarjeta);
		if(tarjeta!=null){
			setComisionBancaria(tarjeta.getComisionBancaria());
		}else
			setComisionBancaria(0);
	}

	public Esquema getEsquema() {
		return esquema;
	}

	public void setEsquema(Esquema esquema) {
		Object old=this.esquema;
		this.esquema = esquema;
		firePropertyChange("esquema", old, esquema);
	}

	public double getComisionBancaria() {
		return comisionBancaria;
	}

	public void setComisionBancaria(double comision) {
		double old=this.comisionBancaria;
		this.comisionBancaria = comision;
		firePropertyChange("comisionBancaria", old, comisionBancaria);
	}

	public String getAutorizacionBancaria() {
		return autorizacionBancaria;
	}

	public void setAutorizacionBancaria(String autorizacionBancaria) {
		Object old=this.autorizacionBancaria;
		this.autorizacionBancaria = autorizacionBancaria;
		firePropertyChange("autorizacionBancaria", old, autorizacionBancaria);
	}	

	@Override
	public String getInfo() {
		if(tarjeta!=null)
			return "Tar:"+tarjeta.getNombre();
		else
			return "P. TARJETA";
	}
	
	
	
	public boolean equals(Object other){
		if(other==null) return false;
		if(other==this) return true;
		if(other.getClass()!=getClass()) return false;
		if(getClass()!=other.getClass()) return false;
		PagoConTarjeta pago=(PagoConTarjeta)other;
		return new EqualsBuilder()
		.appendSuper(super.equals(pago))
		.append(this.autorizacionBancaria, pago.getAutorizacionBancaria())
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder(17,35)
		.appendSuper(super.hashCode())
		.append(this.autorizacionBancaria)
		.toHashCode();
	}
	

	@Override
	public String getAutorizacionInfo() {
		return "AUTORIZADO";
	}
	
	
}
