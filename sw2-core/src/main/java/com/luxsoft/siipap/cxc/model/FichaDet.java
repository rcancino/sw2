package com.luxsoft.siipap.cxc.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.util.Assert;



@Entity
@Table(name="SX_FICHASDET")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class FichaDet implements Serializable{
	
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="FICHADET_ID")
	protected String id;
	
	@ManyToOne(optional = false)    
    @JoinColumn(name = "FICHA_ID", nullable = false, updatable = false,insertable=false)
    private Ficha ficha;
	
	@Column(name="CHEQUE",nullable=false)
	private BigDecimal cheque=BigDecimal.ZERO;
	
	@Column(name="EFECTIVO",nullable=false)
	private BigDecimal efectivo=BigDecimal.ZERO;
	
	@Column(name="BANCO")
	private String banco;
	
	@OneToOne(optional=false,cascade={CascadeType.MERGE,CascadeType.PERSIST}
	,fetch=FetchType.LAZY)
    @JoinColumn(name = "ABONO_ID",unique=true,nullable=false,updatable=false)
	private Pago pago;

	public Ficha getFicha() {
		return ficha;
	}

	public void setFicha(Ficha ficha) {
		this.ficha = ficha;
	}

	public BigDecimal getCheque() {
		if(cheque==null)cheque=BigDecimal.ZERO;
		return cheque;
	}

	public void setCheque(BigDecimal cheque) {
		this.cheque = cheque;
	}

	public BigDecimal getEfectivo() {
		if(efectivo==null)efectivo=BigDecimal.ZERO;
		return efectivo;
	}

	public void setEfectivo(BigDecimal efectivo) {
		this.efectivo = efectivo;
	}

	public BigDecimal getImporte() {
		return getCheque().add(getEfectivo());
	}
	

	public String getBanco() {
		return banco;
	}

	public void setBanco(String banco) {
		this.banco = banco;
	}

	public Pago getPago() {
		return pago;
	}

	public void setPago(Pago pago) {
		this.pago = pago;
		Assert.isTrue(pago instanceof Depositable,"El pago debe implementar Depositable");
		Depositable dep=(Depositable)pago;
		setBanco(dep.getBanco());
		setCheque(dep.getCheque());
		setEfectivo(dep.getEfectivo());
		
	}

	public String getId() {
		return id;
	}
	
	public String toString(){
		return MessageFormat.format("Dep fech:{0,date,short}"
				,getFicha()!=null?getFicha().getFecha():null);
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
		FichaDet other = (FichaDet) obj;
		if (pago == null) {
			if (other.pago != null)
				return false;
		} else if (!pago.equals(other.pago))
			return false;
		return true;
	}
	
	

}
