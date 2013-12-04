package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;

import com.luxsoft.siipap.model.core.Cliente;


/**
 * Entidad de pagos mediante transferencia electónica
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@DiscriminatorValue("PAGO_CHE")
public class PagoConCheque extends Pago implements Depositable{
	
	
	
	private boolean postFechado=false;
	
	@Column(name="VTO",nullable=true)
	private Date vencimiento=null;
	
	private long numero;
	
	@Column(name="REC_DEVUELTO")
	@Type(type="date")
	private Date recepcionDevolucion;
	
	@Formula("(select ifnull(sum(X.IMPORTE),0) FROM SX_VENTAS X where X.CHEQUE_ID=ABONO_ID)")
	private BigDecimal chequeDevuelto=BigDecimal.ZERO;
	

	@Override
	public void setCliente(Cliente cliente) {		
		super.setCliente(cliente);
		setCuentaHabiente(cliente.getNombreRazon());
	}

	public boolean isPostFechado() {
		return postFechado;
	}

	public void setPostFechado(boolean postFechado) {
		boolean old=this.postFechado;
		this.postFechado = postFechado;
		firePropertyChange("postFechado", old, postFechado);
	}
	
	public Date getVencimiento() {
		return vencimiento;
	}

	public void setVencimiento(Date vencimiento) {
		this.vencimiento = vencimiento;
	}

	public long getNumero() {
		return numero;
	}

	public void setNumero(long numero) {
		long old=this.numero;
		this.numero = numero;
		firePropertyChange("numero", old, numero);
	}
	
	@Override
	public String getDepositoInfo() {
		if(getDeposito()!=null)
			return getDeposito().toString();
		return "PENDIENTE";
	}
	
	

	/**
	 * Fecha en que el cheque devuelto es entregado por el cobrador al 
	 * departamento de Tesoreria
	 * 
	 * @return
	 */
	public Date getRecepcionDevolucion() {
		return recepcionDevolucion;
	}

	public void setRecepcionDevolucion(Date recepcionDevolucion) {
		this.recepcionDevolucion = recepcionDevolucion;
	}

	@Override
	public String getInfo() {
		return "CHEQUE: "+numero;
	}
	
	public BigDecimal getChequeDevuelto() {
		return chequeDevuelto;
	}
	
	public boolean isDevuelto(){
		return getChequeDevuelto().doubleValue()>0;
	}

	public boolean equals(Object other){
		if(other==null) return false;
		if(other==this) return true;
		if(other.getClass()!=getClass()) return false;
		PagoConCheque pago=(PagoConCheque)other;
		return new EqualsBuilder()
		.appendSuper(super.equals(pago))
		.append(getNumero(), pago.getNumero())
		.append(this.postFechado, pago.isPostFechado())
		.append(this.vencimiento, pago.getVencimiento())
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder(17,35)		
		.appendSuper(super.hashCode())
		.append(numero)
		.append(postFechado)
		.append(vencimiento)
		.toHashCode();
	}
	
	/** Implementacion de depositable ***/

	public BigDecimal getDepositable() {
		return getTotal();
	}

	public boolean isPendientesDeDeposito() {		
		return ((getDeposito()==null) && (getTotal().doubleValue()>0));
	}

	public BigDecimal getCheque() {
		return getTotal();
	}

	public BigDecimal getEfectivo() {
		return BigDecimal.ZERO;
	}
	
	@Override
	public String getAutorizacionInfo() {
		return "AUTORIZADO";
	}
	

}
