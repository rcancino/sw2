package com.luxsoft.sw3.tesoreria.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.cxc.model.PagoConTarjeta;

@Entity
@Table(name="SX_CORTE_TARJETASDET")
public class CorteDeTarjetaDet {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="CORTEDET_ID")
	protected Long id;
	
	@Version
	private int version;
	
	@ManyToOne(optional=false)
	@JoinColumn (name="CORTE_ID",nullable=false,updatable=false)
    private CorteDeTarjeta corte;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)	
	@JoinColumn(name = "ABONO_ID", nullable = false,unique=true)
	@NotNull(message="La pago  es mandatoria")
	private PagoConTarjeta pago;	
		
	@Column(name="COMENTARIO")
	@Length(max=250)
	private String comentario;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public CorteDeTarjeta getCorte() {
		return corte;
	}

	public void setCorte(CorteDeTarjeta corte) {
		this.corte = corte;
	}

	public PagoConTarjeta getPago() {
		return pago;
	}

	public void setPago(PagoConTarjeta pago) {
		this.pago = pago;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pago == null) ? 0 : pago.hashCode());
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
		CorteDeTarjetaDet other = (CorteDeTarjetaDet) obj;
		if (pago == null) {
			if (other.pago != null)
				return false;
		} else if (!pago.equals(other.pago))
			return false;
		return true;
	}

	
	public String toString(){
		return new ToStringBuilder(this)
		.append(getPago().getInfo())
		.toString();
	}
	
}
