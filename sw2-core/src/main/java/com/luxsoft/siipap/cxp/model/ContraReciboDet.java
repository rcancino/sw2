package com.luxsoft.siipap.cxp.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.util.MonedasUtils;

@Entity
@Table(name="SX_CXP_RECIBOS_DET")
public class ContraReciboDet extends BaseBean{
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "RECIBODET_ID")
	private Long id;
	
	@ManyToOne(optional=false)
	@JoinColumn (name="RECIBO_ID",nullable=false)	
	private ContraRecibo recibo;

	@Column(name = "DOCUMENTO",length=30)
	@Length(max=30)
	private String documento;
	
	@Column (name="FECHA",nullable=false)
	@Type (type="date")
	@NotNull
	private Date fecha=new Date();
	
	@Column (name="VTO",nullable=false)
	@Type (type="date")
	@NotNull
	private Date vencimiento=new Date();
	
	@Column (name="TC",scale=4,precision=12)
	private BigDecimal tc=BigDecimal.ONE; 
	
	@Column (name="TOTAL",nullable=false)
	private BigDecimal total=BigDecimal.ZERO;
	
	@Column (name="MONEDA",length=3)
	private Currency moneda=MonedasUtils.PESOS;
	
	@OneToOne(cascade={CascadeType.ALL})
	/*@JoinTable(name="SX_CXP_RECIBOS_CXP"
		,joinColumns=@JoinColumn(name="RECIBODET_ID")
		,inverseJoinColumns=@JoinColumn(name="CXP_ID")
		)*/
	@JoinColumn(name="CXP_ID")
	private CXPCargoAbono cargoAbono;
	
	@Enumerated(EnumType.STRING)
    @Column (name="TIPO",nullable=false,length=12)
    @NotNull(message="El tipo es mandatorio")
	private Tipo tipo=Tipo.FACTURA;
	
	@Column(name="REQUISICION_DET")
	private Long requisicion;
	
	public Long getId() {
		return id;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}

	public BigDecimal getTc() {
		return tc;
	}

	public void setTc(BigDecimal tc) {
		this.tc = tc;
	}
	
	public BigDecimal getTotal() {
		return total;
	}
	
	public void setTotal(BigDecimal total) {
		Object oldvalue=this.total;
		this.total = total;
		firePropertyChange("total", oldvalue, total);
	}

	public ContraRecibo getRecibo() {
		return recibo;
	}

	public void setRecibo(ContraRecibo contraRecibo) {
		this.recibo = contraRecibo;
	}
	
	public Currency getMoneda() {
		return moneda;
	}
	public void setMoneda(Currency moneda) {
		Object old=this.moneda;
		this.moneda = moneda;
		firePropertyChange("moneda", old, moneda);
	}

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		Object old=this.tipo;
		this.tipo = tipo;
		firePropertyChange("tipo", old, tipo);
	}

	public Date getVencimiento() {
		return vencimiento;
	}

	public void setVencimiento(Date vencimiento) {
		Object old=this.vencimiento;
		this.vencimiento = vencimiento;
		firePropertyChange("vencimiento", old, vencimiento);
	}

	public CXPCargoAbono getCargoAbono() {
		return cargoAbono;
	}

	public void setCargoAbono(CXPCargoAbono cargoAbono) {
		this.cargoAbono = cargoAbono;
	}

	//@AssertTrue (message="La cantidad debe ser >0")
	public boolean validarCantidad(){
		return getTotal().doubleValue()>0;
	}
	
	public String getEstado(){
		if(getCargoAbono()!=null){
			switch (tipo) {
			case FACTURA:
			case CARGO:
				return "AUTORIZADO (Por pagar)";
			case CREDITO:
				return "RECIBIDO (Por aplicar)";
			default:
				break;
			}
			if(getTipo().equals(Tipo.FACTURA)||getTipo().equals(Tipo.CARGO)){
				
			}
		}
		return "PENDIENTE";
		
	}
	

	@Override
	public boolean equals(Object o) {
		if (o == null)	return false;
		if (o == this)	return true;
		if (getClass()!=o.getClass()) return false;
		ContraReciboDet otro = (ContraReciboDet) o;
		return new EqualsBuilder()
			.append(tipo, otro.getTipo())
			.append(documento, otro.getDocumento())
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 35)
		.append(getTipo())
		.append(getDocumento())
		.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(documento)
		.append(tipo)
		.append(fecha)
		.append(getTotal())
		.toString();
	}
	
	public void setRequisicion(Long requisicion) {
		this.requisicion = requisicion;
	}
	public Long getRequisicion() {
		return requisicion;
	}
	
	public Long getAnalisis(){
		if(getCargoAbono()!=null){
			if(getCargoAbono() instanceof CXPFactura){
				CXPFactura fac=(CXPFactura)getCargoAbono();
				return fac.getId();
			}
			return null;
		}else 
			return null;
		
	}
	
	public static enum Tipo {
		FACTURA
		,CREDITO
		,CARGO
	}
	
}