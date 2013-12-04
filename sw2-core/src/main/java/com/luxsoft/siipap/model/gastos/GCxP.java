package com.luxsoft.siipap.model.gastos;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Type;

import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.util.MonedasUtils;



@Entity
@Table (name="SW_GCXP")
@Deprecated
public class GCxP implements GCargoAbono {
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="ID")
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne (optional=false)
	@JoinColumn (name="PROVEEDOR_ID", nullable=false,updatable=false)
	private GProveedor proveedor;
	
		
	@Column (name="NOMBRE",nullable=false,length=200)
	private String nombre;
	
	@ManyToOne (optional=false)
	@JoinColumn (name="SUCURSAL_ID", nullable=false)
	private Sucursal sucursal;
	
	@ManyToOne (optional=false)
	@JoinColumn (name="DEPTO_ID", nullable=false)
	private Departamento departamento;
	
	@Enumerated(EnumType.STRING)
	@Column (name="TIPO",nullable=false, length=10)
	private GTipoDeMovimiento tipo;
	
		
	@Column (name="FECHA",nullable=false)
	@Type (type="date")
	private Date fecha=new Date();	
	
	@Column (name="VENCE",nullable=false)
	@Type (type="date")
	private Date vencimiento;
	
	@ManyToOne (optional=false)
	@JoinColumn (name="COMPRA_ID", nullable=true)
	private GCompra compra;
	
	@Column (name="MONEDA",nullable=false,length=3)
	private Currency moneda=MonedasUtils.PESOS;
	
	@Column (name="TC",nullable=false,scale=4)
	private BigDecimal tc=BigDecimal.ONE;
	
	@Column (name="IMPORTE",nullable=false,scale=2)
	private BigDecimal total=BigDecimal.ZERO;
	
	@Column (name="SALDO",nullable=false,scale=2)
	private BigDecimal saldo=BigDecimal.ZERO;
	
	@Column (name="ACUMULADO",nullable=false,scale=2)
	private BigDecimal acumulado=BigDecimal.ZERO;
	
	@Column (name="REFERENCIA",length=15)
	private String referencia;
	
	@Column (name="COMENTARIO",length=150)
	private String comentario;
	
	@Column (name="PRESUPUESTO_ID")
	private Long presupuesto;
	
	@Column (name="REQUISICIONDET_ID")
	private Long requisicionDet;
	
	@Column (name="TESORERIA_ID")
	private Long tesoreria;
	
	

	public GCxP() {}
	
	public GCxP(GCompra compra) {		
		this.compra = compra;
		GCargoAbono ca=this.compra;
		setProveedor(ca.getProveedor());
		setNombre(ca.getProveedor().getNombre());
		setSucursal(ca.getSucursal());
		setDepartamento(ca.getDepartamento());
		setFecha(ca.getFecha());
		setVencimiento(ca.getVencimiento());
		setTotal(ca.getTotal());
		setMoneda(ca.getMoneda());
		setTc(ca.getTc());
		
	}
	
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
	
	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.model.GCargoAbono#getSucursal()
	 */
	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.model.GCargoAbono#getDepartamento()
	 */
	public Departamento getDepartamento() {
		return departamento;
	}

	public void setDepartamento(Departamento departamento) {
		this.departamento = departamento;
	}

	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.model.GCargoAbono#getTipo()
	 */
	public GTipoDeMovimiento getTipo() {
		return tipo;
	}

	public void setTipo(GTipoDeMovimiento tipo) {
		this.tipo = tipo;
	}

	/* (non-Javadoc)
	* @see com.luxsoft.siipap.model.GCargoAbono#getProveedor()
	*/
	public GProveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(GProveedor proveedor) {
		this.proveedor = proveedor;
	}	

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.model.GCargoAbono#getFecha()
	 */
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.model.GCargoAbono#getVencimiento()
	 */
	public Date getVencimiento() {
		return vencimiento;
	}
	public void setVencimiento(Date vencimiento) {
		this.vencimiento = vencimiento;
	}

	/**
	 * 
	 * @return
	 */
	public GCompra getCompra() {
		return compra;
	}
	public void setCompra(GCompra compra) {
		this.compra = compra;
	}
	
	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.model.GCargoAbono#getMoneda()
	 */
	public Currency getMoneda() {
		return moneda;
	}

	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}

	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.model.GCargoAbono#getTc()
	 */
	public BigDecimal getTc() {
		return tc;
	}

	public void setTc(BigDecimal tc) {
		this.tc = tc;
	}

	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.model.GCargoAbono#getTotal()
	 */
	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal importe) {
		this.total = importe;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

	public BigDecimal getAcumulado() {
		return acumulado;
	}

	public void setAcumulado(BigDecimal acumulado) {
		this.acumulado = acumulado;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public Long getPresupuesto() {
		return presupuesto;
	}

	public void setPresupuesto(Long presupuesto) {
		this.presupuesto = presupuesto;
	}

	public Long getRequisicionDet() {
		return requisicionDet;
	}

	public void setRequisicionDet(Long requisicionDet) {
		this.requisicionDet = requisicionDet;
	}

	public Long getTesoreria() {
		return tesoreria;
	}

	public void setTesoreria(Long tesoreria) {
		this.tesoreria = tesoreria;
	}
	
	
	public boolean equals(Object o){
		if(o==null) return false;
		if(o==this) return true;
		GCxP otro=(GCxP)o;
		return new EqualsBuilder()
		.append(getProveedor(),otro.getProveedor())
		.append(getTipo(), otro.getTipo())		
		.append(getFecha(),otro.getFecha())
		.append(getCompra(), otro.getCompra())
		.append(getTotal(), otro.getTotal())
		.isEquals();		
	}
	
	public int hashCode(){
		return new HashCodeBuilder(17,35)
		.append(getProveedor())
		.append(getTipo())		
		.append(getFecha())
		.append(getCompra())
		.append(getTotal())
		.toHashCode();
	}
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append(getTipo())		
		.append(getFecha())
		.append(getCompra())
		.append(getTotal())
		.toString();
	}
	
	

}
