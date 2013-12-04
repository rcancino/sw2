package com.luxsoft.siipap.model.tesoreria;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import javax.persistence.Column;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;


import com.luxsoft.siipap.model.AbstractJavaBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Model bean para facilitar la generacion de abonos desde 
 * la capa de presentacion (UI)
 * 
 * @author Ruben Cancino
 *
 */
public class Deposito extends AbstractJavaBean{
	
	@NotNull (message="La cuenta destino no puede ser nulo")
	private Cuenta cuenta;
	
	@NotNull
	private Sucursal sucursal;
	
	@NotNull
	private Date fecha=new Date();
	
	//@NotNull (message="El concepto no puede ser nulo")
	private Concepto concepto;
	
	private Currency moneda=MonedasUtils.PESOS;
		
	private BigDecimal importe=BigDecimal.ZERO;
	
	private BigDecimal tc=BigDecimal.ONE;
	
	@Length(max=150)
	private String comentario;
	
	
	@Length(max=100)
	private String referencia;
	
	private FormaDePago formaDePago=FormaDePago.CHEQUE;
	
	private boolean retiro=false;
	
	public Cuenta getCuenta() {
		return cuenta;
	}
	public void setCuenta(Cuenta cuenta) {
		Object old=this.cuenta;
		this.cuenta = cuenta;
		firePropertyChange("cuenta", old, cuenta);
	}
	
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}
	
	public Concepto getConcepto() {
		return concepto;
	}
	public void setConcepto(Concepto concepto) {
		Object old=this.concepto;
		this.concepto = concepto;
		firePropertyChange("concepto", old, concepto);
	}
	
	public Currency getMoneda() {
		return moneda;
	}
	public void setMoneda(Currency moneda) {
		Object old=this.moneda;
		this.moneda = moneda;
		firePropertyChange("moneda", old, moneda);
	}
	
	public BigDecimal getImporte() {
		return importe;
	}
	public void setImporte(BigDecimal importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
	}
	
	public BigDecimal getTc() {
		return tc;
	}
	public void setTc(BigDecimal tc) {
		Object old=this.tc;
		this.tc = tc;
		firePropertyChange("tc", old, tc);
	}
	
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}
	
	public FormaDePago getFormaDePago() {
		return formaDePago;
	}
	public void setFormaDePago(FormaDePago formaDePago) {
		this.formaDePago = formaDePago;
	}
	
	public boolean isRetiro() {
		return retiro;
	}
	public void setRetiro(boolean retiro) {
		this.retiro = retiro;
	}
	
	public CargoAbono toCargoAbono(){
		BigDecimal imp=isRetiro()?getImporte().abs().multiply(BigDecimal.valueOf(-1)):getImporte().abs();
		CargoAbono abono=CargoAbono.crearAbono(getCuenta()
				, imp
				, getFecha()
				, getConcepto()
				,null);
		abono.setComentario(getComentario());
		abono.setSucursal(getSucursal());
		abono.setReferencia(getReferencia());
		abono.setTc(getTc());
		if(getCuenta().getMoneda().equals(MonedasUtils.PESOS))
			abono.setTc(BigDecimal.ONE);
		return abono;
	}
	public Sucursal getSucursal() {
		return sucursal;
	}
	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
	public String getReferencia() {
		return referencia;
	}
	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}
	

	

}
