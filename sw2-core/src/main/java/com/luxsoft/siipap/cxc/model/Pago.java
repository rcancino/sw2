package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.ventas.model.Cobrador;

/**
 * Abono o pago mediante  documento monetario
 * 
 * @author Ruben Cancino
 *
 */
@Entity
public abstract class Pago extends Abono{
	
	@Column(name="CUENTA_CLIENTE",nullable=true)
	@Length(max=50)
	private String cuentaDelCliente;
	
	@Column(name="CUENTA_HABIENTE",nullable=true)
	@Length(max=255)
	private String cuentaHabiente;
	
	@ManyToOne(optional = true,fetch=FetchType.EAGER)			
	@JoinColumn(name = "CUENTA_ID", nullable = true)
	private Cuenta cuenta;

	@ManyToOne(optional = true)			
	@JoinColumn(name = "COBRADOR_ID", nullable = true)
	private Cobrador cobrador;
	
	@Column(name="BANCO",nullable=true,length=100)
	@Length(max=50)
	private String banco;
	
	@Column(name="ANTICIPO",nullable=true)
	private Boolean anticipo=false;
	
	@OneToOne(mappedBy="pago",fetch=FetchType.EAGER)
	private FichaDet deposito;
	
	@Column(name="ENVIADO")
	private Boolean enviado=Boolean.FALSE;
	
	/*
	@ManyToOne(optional = true
			,fetch=FetchType.EAGER
			//,cascade={CascadeType.MERGE,CascadeType.PERSIST}
	)			
	@JoinColumn(name = "CARGOABONO_ID", nullable = true)
	*/
	//@Transient
	//private CargoAbono ingreso;
	
	public String getBanco() {
		return banco;
	}

	public void setCliente(final Cliente c){
		super.setCliente(c);
		if(c!=null)
			setCobrador(c.getCobrador());
	}
	
	public void setBanco(String banco) {
		Object old=this.banco;
		this.banco = banco;
		firePropertyChange("banco", old, banco);
	}

	@AssertTrue(message="El total debe ser mayor a 0 ")
	public boolean importeValido(){
		if(getId()==null){
			return getTotal().doubleValue()>0;
		}else
			return true;				
	}
	
	/**
	 * La cuenta de banco del cliente
	 * 
	 * @return
	 */
	public String getCuentaDelCliente() {
		return cuentaDelCliente;
	}

	/**
	 * Fija la cuenta de banco del cliente, origen del pago
	 * 
	 * @param cuentaDelCliente
	 */
	public void setCuentaDelCliente(String cuentaDelCliente) {
		this.cuentaDelCliente = cuentaDelCliente;
	}

	public String getCuentaHabiente() {
		return cuentaHabiente;
	}

	public void setCuentaHabiente(String cuentaHabiente) {
		Object old=this.cuentaHabiente;
		this.cuentaHabiente = cuentaHabiente;
		firePropertyChange("cuentaHabiente", old, cuentaHabiente);
	}
	
	public Boolean isAnticipo() {
		if(anticipo==null)
			anticipo=Boolean.FALSE;
		return anticipo;
	}

	public void setAnticipo(Boolean anticipo) {
		this.anticipo = anticipo;
	}
	
	public Boolean getAnticipo(){
		return isAnticipo();
	}
	
	public String getDepositoInfo() {
		if(getCuenta()!=null)
			return getCuenta().toString();
		else
			return "ND";
	}
	
	public FichaDet getDeposito() {
		return deposito;
	}

	public void setDeposito(FichaDet deposito) {
		this.deposito = deposito;
	}
	
	/**
	 * La cuenta a la que se registro el deposito
	 * del cliente
	 * 
	 * @return
	 */
	public Cuenta getCuenta() {
		return cuenta;
	}

	public void setCuenta(Cuenta cuenta) {
		Object old=this.cuenta;
		this.cuenta = cuenta;
		firePropertyChange("cuenta", old, cuenta);
	}
	
	public Cobrador getCobrador() {
		return cobrador;
	}

	public void setCobrador(Cobrador cobrador) {
		
		Object old=this.cobrador;
		this.cobrador = cobrador;
		firePropertyChange("cobrador", old, cobrador);
	}

	public Boolean isEnviado() {
		if(enviado==null)
			enviado=Boolean.FALSE;			
		return enviado;
	}

	public void setEnviado(Boolean enviado) {
		Object old=this.enviado;
		this.enviado = enviado;
		firePropertyChange("enviado", old, enviado);
	}
	
	public Boolean getEnviado() {
		return enviado;
	}
	
	

	public boolean equals(Object obj){
		if(obj==null) return false;
		if(obj==this) return true;
		if(getClass()!=obj.getClass()) return false;
		Pago other=(Pago)obj;
		return new EqualsBuilder()		
		.append(getId(), other.getId())
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder(19,75)		
		.append(getId())
		.toHashCode();
	}
	
	public Aplicacion findPrimeraAplicacion(Date fecha){
		Aplicacion pAplicacion=(Aplicacion)CollectionUtils.find(getAplicaciones(), new Predicate() {					
			public boolean evaluate(Object object) {
				Aplicacion aa=(Aplicacion)object;
				return DateUtils.isSameDay(aa.getFecha(), getPrimeraAplicacion());
			}
		});	
		return pAplicacion;
	}

	
}
