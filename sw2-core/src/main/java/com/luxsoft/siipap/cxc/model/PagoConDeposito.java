package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Formula;
import org.hibernate.validator.Length;

/**
 * Entidad de pagos mediante transferencia electónica
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@DiscriminatorValue("PAGO_DEP")
public class PagoConDeposito extends Pago {
	
	@Transient
	public static final String[] TIPO={"TRANSFERENCIA","DEPOSITO"};
	
	@Column(name="TRANSFERENCIA",nullable=true)
	private BigDecimal transferencia=BigDecimal.ZERO;
	
	/**
	 * Clave alfanumerica normalmente util en las transferencias electronicas
	 * en pagos con cheque es el numero del cheque
	 */
	@Column(name="REFERENCIA",nullable=true)
	private String referenciaBancaria;
	
	@Column(name="CHEQUE",nullable=true)
	private BigDecimal cheque=BigDecimal.ZERO;
	
	@Column(name="EFECTIVO",nullable=true)
	private BigDecimal efectivo=BigDecimal.ZERO;
	
	
	@Column(name="FECHA_DEPOSITO",nullable=true)
	private Date fechaDeposito;
	
	@Formula("(select max(a.CARGOABONO_ID) FROM SW_BCARGOABONO a where a.PAGO_ID=ABONO_ID)")
	private Long ingreso;
	
	/**
	 * TODO Regresar para el POS
	 */
	@Column(name="SALVO_COBRO")
	private Boolean salvoBuenCobro=false;

	public BigDecimal getTransferencia() {
		return transferencia;
	}

	public void setTransferencia(BigDecimal transferencia) {
		Object old=this.transferencia;
		this.transferencia = transferencia;
		firePropertyChange("transferencia", old, transferencia);
		if(transferencia.doubleValue()>0){
			setCheque(BigDecimal.ZERO);
			setEfectivo(BigDecimal.ZERO);
		}
		actualizarTotal();
	}

	public BigDecimal getCheque() {
		if(cheque==null)
			cheque=BigDecimal.ZERO;
		return cheque;
	}

	public void setCheque(BigDecimal cheque) {
		Object old=this.cheque;
		this.cheque = cheque;
		firePropertyChange("cheque", old, cheque);
		if(cheque.doubleValue()>0)
			setTransferencia(BigDecimal.ZERO);
		actualizarTotal();
	}

	public BigDecimal getEfectivo() {
		if(efectivo==null)
			efectivo=BigDecimal.ZERO;
		return efectivo;
	}

	public void setEfectivo(BigDecimal efectivo) {
		Object old=this.efectivo;
		this.efectivo = efectivo;
		firePropertyChange("efectivo", old, efectivo);
		if(efectivo.doubleValue()>0)
			setTransferencia(BigDecimal.ZERO);
		actualizarTotal();
	}
	
	public void actualizarTotal(){
		if(transferencia.doubleValue()>0){
			setTotal(transferencia);
		}else
			setTotal(efectivo.add(cheque));
	}

	
	public String getReferenciaBancaria() {
		return referenciaBancaria;
	}

	public void setReferenciaBancaria(String referencia) {
		Object old=this.referenciaBancaria;
		this.referenciaBancaria = referencia;
		firePropertyChange("referenciaBancaria", old, referenciaBancaria);
	}
	
	public String getTipo(){
		if(transferencia.doubleValue()>0)
			return "TRANSF";
		else if(getEfectivo().doubleValue()>0 && getCheque().doubleValue()>0){
			return "MIXTO";
		}
		else if(getEfectivo().doubleValue()>0 && getCheque().doubleValue()==0)
			return "EFECTIVO";
		else
			return "CHEQUE"; 
	}

	@Override
	public String getInfo() {
		
		String pattern="{0} Ref:{1} ";
		String s=getTipo().equalsIgnoreCase("TRANSF")?getTipo():"Dep  "+getTipo();
		return MessageFormat.format(pattern,s, referenciaBancaria);
	}
	

	public Date getFechaDeposito() {
		return fechaDeposito;
	}

	public void setFechaDeposito(Date fechaDeposito) {
		Object old=this.fechaDeposito;
		this.fechaDeposito = fechaDeposito;
		firePropertyChange("fechaDeposito", old, fechaDeposito);
	}
	
	public Boolean getSalvoBuenCobro() {
		return salvoBuenCobro;
	}

	public void setSalvoBuenCobro(Boolean salvoBuenCobro) {
		Object old=this.salvoBuenCobro;
		this.salvoBuenCobro = salvoBuenCobro;
		firePropertyChange("salvoBuenCobro", old, salvoBuenCobro);
	}

	

	//@AssertFalse(message="La transferencia requiere referencia bancaria")
	public boolean validarTransferencia(){
		if(TIPO[0].equals(getTipo())){
			return StringUtils.isBlank(referenciaBancaria);
		}
		return false;
	}
	
	//@AssertTrue(message="El banco es mandatorio")
	public boolean validarBanco(){
		return StringUtils.isNotBlank(getBanco());
	}
	
	//@AssertTrue(message="La cuenta destino es mandatoria")
	public boolean validarCuenta(){
		return getCuenta()!=null;
	}
	
	//@AssertTrue(message="La fecha del deposito es mandatoria")
	public boolean validarFechaDeposito(){
		return getFechaDeposito()!=null;
	}
	
	@Override
	public boolean requiereAutorizacion() {
		return true;
	}
/*
	public boolean equals(Object other){
		if(other==null) return false;
		if(other==this) return true;
		if(other.getClass()!=getClass()) return false;
		PagoConDeposito pago=(PagoConDeposito)other;
		return new EqualsBuilder()
		.append(getCliente(), pago.getCliente())
		.append(getBanco(), pago.getBanco())
		.append(getCuenta(), pago.getCuenta())
		.append(getReferenciaBancaria(), pago.getReferenciaBancaria())
		.append(getTotal(),pago.getTotal())
		.append(getFecha(), pago.getFecha())
		.append(getComentario(), pago.getComentario())
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder(17,35)
		.append(getCliente())
		.append(getBanco())
		.append(getCuenta())
		.append(getReferenciaBancaria())
		.append(getTotal())
		.append(getFecha())
		.append(getComentario())
		.toHashCode();
	}
	*/

	@Transient
	@Length(max=15)
	private String solicito;

	public String getSolicito() {
		if(getLog()!=null){
			return getLog().getCreateUser();
		}
		return solicito;
	}

	public void setSolicito(String solicito) {
		Object old =this.solicito;
		this.solicito = solicito;
		firePropertyChange("solicito", old, solicito);
		if(getLog()!=null){
			getLog().setCreateUser(solicito);
		}
	}
	
	public Long getIngreso(){
		return ingreso;
	}
	

}
