package com.luxsoft.sw3.tesoreria.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.model.tesoreria.FormaDePago;
import com.luxsoft.siipap.model.tesoreria.Origen;
import com.luxsoft.siipap.util.MonedasUtils;


@Entity
@Table(name="SX_COMISIONES_BANCARIAS")
public class ComisionBancaria extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="COMISION_ID")
	protected Long id;
	
	@Version
	private int version;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	private Date fecha=new Date();
	
	@ManyToOne(optional = false)	
	@JoinColumn(name = "CUENTA_ID", nullable = false)
	@NotNull(message="La cuenta origen es mandatoria")
	private Cuenta cuenta;
	
	
	@Column(name="MONEDA",nullable=false,length=3)
	private Currency moneda=MonedasUtils.PESOS;
	
	@Column(name="TC",nullable=false)
	private double tc=1;
	
	@Column(name="COMISION",nullable=false)
	private BigDecimal comision=BigDecimal.ZERO;
	
	@Column(name="IMPUESTO",nullable=false)
	private BigDecimal impuesto=BigDecimal.ZERO;
		
	@ManyToOne(optional=false,cascade=CascadeType.ALL)
    @JoinColumn(name="CA_COMISION_ID", nullable=false,updatable=false)    
	private CargoAbono comisionId;
	
	@ManyToOne(optional=true,cascade=CascadeType.ALL)
    @JoinColumn(name="CA_IMPUESTO_ID", nullable=true,updatable=false)   	
	private CargoAbono impuestoId;
	
	@Column(name="COMENTARIO")
	@Length(max=250)
	private String comentario;
	
	@Column(name="REFERENCIA")
	@Length(max=100)
	private String referenciaOrigen;
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Cuenta getCuenta() {
		return cuenta;
	}

	public void setCuenta(Cuenta cuenta) {
		Object old=this.cuenta;
		this.cuenta = cuenta;
		firePropertyChange("cuenta", old, cuenta);
		if(cuenta!=null){
			setMoneda(cuenta.getMoneda());
		}else{
			setMoneda(null);
		}
	}
	
	public BigDecimal getComision() {
		return comision;
	}

	public void setComision(BigDecimal comision) {
		Object old=this.comision;
		this.comision = comision;
		firePropertyChange("comision", old, comision);
		if(comision!=null){
			if(getMoneda()!=null && getMoneda().equals(MonedasUtils.PESOS))
				setImpuesto(MonedasUtils.calcularImpuesto(comision));
		}else{
			setImpuesto(BigDecimal.ZERO);
		}
	}
	

	public BigDecimal getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(BigDecimal impuesto) {
		Object old=this.impuesto;
		this.impuesto = impuesto;
		firePropertyChange("impuesto", old, impuesto);
	}

		public Currency getMoneda() {
		return moneda;
	}

	public void setMoneda(Currency moneda) {
		Object old=this.moneda;
		this.moneda = moneda;
		firePropertyChange("moneda", old, moneda);
	}
	
	public double getTc() {
		return tc;
	}

	public void setTc(double tc) {
		this.tc = tc;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public String getReferenciaOrigen() {
		return referenciaOrigen;
	}

	public void setReferenciaOrigen(String referenciaOrigen) {
		Object old=this.referenciaOrigen;
		this.referenciaOrigen = referenciaOrigen;
		firePropertyChange("referenciaOrigen", old, referenciaOrigen);
	}
	
	public CargoAbono getComisionId() {
		return comisionId;
	}

	public void setComisionId(CargoAbono comisionId) {
		this.comisionId = comisionId;
	}

	public CargoAbono getImpuestoId() {
		return impuestoId;
	}

	public void setImpuestoId(CargoAbono impuestoId) {
		this.impuestoId = impuestoId;
	}

	public CargoAbono agregarMovimiento(Cuenta cuenta,BigDecimal importe,String clase,Date fecha,String comentario){
		//Deposito
		CargoAbono ca=new CargoAbono();
		ca.setCuenta(cuenta);
		ca.setImporte(importe);
		ca.setFecha(fecha);
		ca.setAFavor(cuenta.getBanco().getEmpresa().getNombre());
		ca.setMoneda(cuenta.getMoneda());
		ca.setEncriptado(false);
		ca.setFormaDePago(FormaDePago.TRANSFERENCIA);
		ca.setOrigen(Origen.TESORERIA);
		ca.setClasificacion(clase);
		ca.setComentario(comentario);
		return ca;
	}

	public int getVersion() {
		return version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		ComisionBancaria other = (ComisionBancaria) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(getId())
		.append(getComision())
		.toString();
	}
	
	@Transient
	private List<CargoAbono> movimientos;
	
	public List<CargoAbono> getMovimientos(){
		if(movimientos==null){
			movimientos=new ArrayList<CargoAbono>();
			movimientos.add(getComisionId());
			movimientos.add(getImpuestoId());
		}
		return movimientos;
	}
	
	
}
