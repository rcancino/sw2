package com.luxsoft.sw3.tesoreria.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
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
@Table(name="SX_TRASPASOS_CUENTAS")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TIPO",discriminatorType=DiscriminatorType.STRING,length=20)
@DiscriminatorValue("TRASPASO")
public class TraspasoDeCuenta extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="TRASPASO_ID")
	protected Long id;
	
	@Version
	private int version;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	private Date fecha=new Date();
	
	@ManyToOne(optional = false)	
	@JoinColumn(name = "CUENTA_ORIGEN_ID", nullable = false)
	@NotNull(message="La cuenta origen es mandatoria")
	private Cuenta cuentaOrigen;
	
	@ManyToOne(optional = false)	
	@JoinColumn(name = "CUENTA_DESTINO_ID", nullable = false)
	@NotNull(message="La cuenta destino es mandatoria")
	private Cuenta cuentaDestino;
	
	@Column(name="MONEDA",nullable=false,length=3)
	private Currency moneda=MonedasUtils.PESOS;
	
	@Column(name="TC",nullable=false)
	private double tc=1;
	
	@Column(name="IMPORTE",nullable=false)
	private BigDecimal importe=BigDecimal.ZERO;
	
	@Column(name="COMISION",nullable=false)
	private BigDecimal comision=BigDecimal.ZERO;
	
	@Column(name="IMPUESTO",nullable=false)
	private BigDecimal impuesto=BigDecimal.ZERO;
	/*	
	@ManyToOne(optional=false,cascade=CascadeType.ALL)
    @JoinColumn(name="RETIRO_ID", nullable=false,updatable=false)    
	private CargoAbono retiro;
	
	@ManyToOne(optional=false,cascade=CascadeType.ALL)
    @JoinColumn(name="DEPOSITO_ID", nullable=false,updatable=false)   	
	private CargoAbono deposito;
	*/
	@Column(name="COMENTARIO")
	@Length(max=250)
	private String comentario;
	
	@Column(name="REFERENCIA")
	@Length(max=100)
	private String referenciaOrigen;
	
	 @OneToMany(cascade={
			 	 CascadeType.PERSIST
			 	,CascadeType.MERGE
			 	,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY
			,mappedBy="traspaso"
			)
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
	protected Set<CargoAbono> movimientos=new HashSet<CargoAbono>();

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

	public Cuenta getCuentaOrigen() {
		return cuentaOrigen;
	}

	public void setCuentaOrigen(Cuenta cuentaOrigen) {
		Object old=this.cuentaOrigen;
		this.cuentaOrigen = cuentaOrigen;
		firePropertyChange("cuentaOrigen", old, cuentaOrigen);
		if(cuentaOrigen!=null){
			setMoneda(cuentaOrigen.getMoneda());
		}else{
			setMoneda(null);
		}
	}

	public Cuenta getCuentaDestino() {
		return cuentaDestino;
	}

	public void setCuentaDestino(Cuenta cuentaDestino) {
		Object old=this.cuentaDestino;
		this.cuentaDestino = cuentaDestino;
		firePropertyChange("cuentaDestino", old, cuentaDestino);
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
	}

	public BigDecimal getComision() {
		return comision;
	}

	public void setComision(BigDecimal comision) {
		Object old=this.comision;
		this.comision = comision;
		firePropertyChange("comision", old, comision);
		if(comision!=null){
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

	/*public CargoAbono getRetiro() {
		return retiro;
	}

	public void setRetiro(CargoAbono retiro) {
		this.retiro = retiro;
	}

	public CargoAbono getDeposito() {
		return deposito;
	}

	public void setDeposito(CargoAbono deposito) {
		this.deposito = deposito;
	}
*/
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

	public Set<CargoAbono> getMovimientos() {
		return movimientos;
	}
	
	
	public CargoAbono agregarMovimiento(Cuenta cuenta,BigDecimal importe,Clasificacion clase,Date fecha,String comentario){
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
		ca.setClasificacion(clase.name());
		ca.setComentario(comentario);
		movimientos.add(ca);
		ca.setTraspaso(this);
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
		TraspasoDeCuenta other = (TraspasoDeCuenta) obj;
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
		.append(getImporte())
		.toString();
	}
	
	public String getDescripcion(){
		return "TRASPASO ENTRE CUENTAS";
	}
	
	

}
