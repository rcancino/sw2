package com.luxsoft.siipap.cxp.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Entidad base para los movimientos de cuenta de un Proveedor
 * son las caracteristicas generales que comparten todos los cargos y abonos
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_CXP")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TIPO",discriminatorType=DiscriminatorType.STRING,length=20)
public abstract class CXPCargoAbono extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="CXP_ID")
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne (optional=false,fetch=FetchType.EAGER)
	@JoinColumn (name="PROVEEDOR_ID", nullable=false, updatable=false)
	@NotNull
	private Proveedor proveedor;
	
	@Column(name="CLAVE",nullable=false,length=4)
	private String clave;
	
	@Column (name="NOMBRE",nullable=true)
	@Length (max=250) @NotNull @NotEmpty(message="El nombre es incorrecto")	
	private String nombre;	
	
	@Column(name = "DOCUMENTO", length = 20, nullable = false)
	@NotNull @Length(max=20)
	private String documento;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	@NotNull
	private Date fecha=new Date();
	
	@Column(name="COMENTARIO")
	@Length(max=250)
	private String comentario;
	
	@Column (name="MONEDA",length=3,nullable=false)
	@NotNull
	private Currency moneda=MonedasUtils.PESOS;
	
	@Column(name="TC",nullable=false)
	@NotNull
	private double tc=1;	
	
	@Column (name="IMPORTE",nullable=false)
	private BigDecimal importe=BigDecimal.ZERO;
	
	@Column (name="IMPUESTO",nullable=false)
	private BigDecimal impuesto=BigDecimal.ZERO;
	
	@Column (name="TOTAL",nullable=false)
	protected BigDecimal total=BigDecimal.ZERO;	
	
	@Column(name="DIFERENCIA")
	private BigDecimal diferencia=BigDecimal.ZERO;
	
	@Column(name="DIFERENCIA_FECHA")
	@Type(type="date")
	private Date diferenciaFecha;
	
	@OneToOne(mappedBy="cargoAbono",cascade={CascadeType.MERGE,CascadeType.PERSIST})
	//@Transient
	private ContraReciboDet recibo;
	
	@Column(name="CREADO_USERID",updatable=false,length=50)
	private String createUser;
	
    @Column(name="MODIFICADO_USERID",length=50)
    private String updateUser;
	
   // @Type (type="time")
	@Column(name="CREADO",updatable=false,nullable=true)
	private Date creado=new Date();
	
    @Type (type="time")
	@Column(name="MODIFICADO",updatable=false,insertable=false)
	private Date modificado;

	public Long getId() {
		return id;
	}
	
	public int getVersion() {
		return version;
	}	

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		Object old=this.proveedor;
		this.proveedor = proveedor;
		firePropertyChange("proveedor", old, proveedor);
		setNombre(proveedor!=null?proveedor.getNombreRazon():null);
		setClave(proveedor!=null?proveedor.getClave():null);
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		Object old=this.clave;
		this.clave = clave;
		firePropertyChange("clave", old, clave);
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		Object old=this.nombre;
		this.nombre = nombre;
		firePropertyChange("nombre", old, nombre);
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		Object old=this.documento;
		this.documento = documento;
		firePropertyChange("documento", old, documento);
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
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
		double old=this.tc;
		this.tc = tc;
		firePropertyChange("tc", old, tc);
	}
	
	public CantidadMonetaria getImporteMN(){		
		return CantidadMonetaria.pesos(getImporte().doubleValue()*getTc());
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
	}

	public BigDecimal getImpuesto() {
		return impuesto;
	}
	public CantidadMonetaria getImpuestoMN(){		
		return CantidadMonetaria.pesos(getImpuesto().doubleValue()*getTc());
	}

	public void setImpuesto(BigDecimal impuesto) {
		Object old=this.impuesto;
		this.impuesto = impuesto;
		firePropertyChange("impuesto", old, impuesto);
	}
	
	public CantidadMonetaria getTotalCM(){
		return new CantidadMonetaria(getTotal(),getMoneda());
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		Object old=this.total;
		this.total = total;
		firePropertyChange("total", old, total);
	}
	
	public CantidadMonetaria getTotalMN(){
		BigDecimal tipoCambio=BigDecimal.valueOf(getTc());
		return CantidadMonetaria.pesos(getTotal().multiply(tipoCambio).doubleValue());
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Date getCreado() {
		return creado;
	}

	public Date getModificado() {
		return modificado;
	}
	
    
    public ContraReciboDet getRecibo() {
		return recibo;
	}
    
    

	
    public CantidadMonetaria getDiferenciaMN() {
    	BigDecimal tipoCambio=BigDecimal.valueOf(getTc());
		return CantidadMonetaria.pesos(getDiferencia()).multiply(tipoCambio);
    }

	public BigDecimal getDiferencia() {
		if(diferencia==null)
			diferencia=BigDecimal.ZERO;
		return diferencia;
	}

	public void setDiferencia(BigDecimal diferencia) {
		this.diferencia = diferencia;
	}
	
	public BigDecimal getDiferencia(Date corte) {
		if( (getDiferenciaFecha()!=null) && (corte.compareTo(getDiferenciaFecha())<0) ){
			return BigDecimal.ZERO;
		}
		return getDiferencia();
	}

	public Date getDiferenciaFecha() {
		return diferenciaFecha;
	}

	public void setDiferenciaFecha(Date diferenciaFecha) {
		this.diferenciaFecha = diferenciaFecha;
	}

	public void setRecibo(ContraReciboDet recibo) {
		Object old=this.recibo;
		this.recibo = recibo;
		firePropertyChange("recibo", old, recibo);
	}

	public boolean equals(Object obj){
    	if(obj==null) return false;
    	if(obj==this) return true;
    	if(getClass()!=obj.getClass()) return false;
    	CXPCargoAbono other=(CXPCargoAbono)obj;
    	return new EqualsBuilder()
    	.append(nombre, other.getNombre())
    	.append(documento, other.getDocumento())
    	.append(fecha, other.getFecha())
    	.append(creado, other.getCreado())
    	.isEquals();
    }
	
    public int hashCode(){
    	return new HashCodeBuilder(27,93)
    	.append(nombre)
    	.append(documento)
    	.append(fecha)
    	.append(creado)
    	.toHashCode();
    }
    
    public String toString(){
    	return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
    	.append(nombre)
    	.append(documento)
    	.append(fecha)
    	.append(total)
    	.append(moneda)
    	.toString();
    }

}
