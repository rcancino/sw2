package com.luxsoft.siipap.cxp.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.model.AdressLog;

/**
 * Entidiad que representa el analisis o revision de una o mas facturas de proveedor
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_ANALISIS")
public  class AnalisisDeFactura extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ANALISIS_ID")
	private Long id;
	
	@Version
	private int version;
	/*
	@Column(name = "DOCUMENTO", length = 20, nullable = false)
	@NotNull @Length(max=20)
	private String documento;
	
	@ManyToOne (optional=false)
	@JoinColumn (name="PROVEEDOR_ID", nullable=false, updatable=false)
	@NotNull
	private Proveedor proveedor;*/
	
	@ManyToOne(optional=false,cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn (name="CXP_ID",nullable=false,updatable=false)
	private CXPFactura factura;
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="REQUISICION_DET")
	private RequisicionDe requisicionDet;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	@NotNull
	private Date fecha=new Date();
	
	@Column(name = "PRIMER_ANALISIS", nullable = false)
	private boolean primerAnalisis=true;
	
	@Column(name="COMENTARIO")
	@Length(max=250)
	private String comentario;
		
	
	@Column (name="IMPORTE",nullable=false)
	private BigDecimal importe=BigDecimal.ZERO;
	
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="analisis")
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)	
	private Set<AnalisisDeFacturaDet> partidas=new HashSet<AnalisisDeFacturaDet>();
	
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createdIp",	column=@Column(name="CREADO_IP" ,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(name="MODIFICADO_IP",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(name="CREADO_MAC",nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(name="MODIFICADO_MAC",nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog=new AdressLog();
	
	public Long getId() {
		return id;
	}
	
	public int getVersion() {
		return version;
	}	
	/*
	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		Object old=this.documento;
		this.documento = documento;
		firePropertyChange("documento", old, documento);
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		Object old=this.proveedor;
		this.proveedor = proveedor;
		firePropertyChange("proveedor", old, proveedor);
	}*/

	public CXPFactura getFactura() {
		return factura;
	}

	public void setFactura(CXPFactura factura) {
		Object old=this.factura;
		this.factura = factura;		
		firePropertyChange("factura", old, factura);
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
	
	public BigDecimal getImporte() {
		return importe;
	}
	
	public CantidadMonetaria getImporteMN(){
		CantidadMonetaria im=CantidadMonetaria.pesos(getImporte());
		return im.multiply(getFactura().getTc());
	}
	
	public BigDecimal getTotal(){
		return MonedasUtils.calcularTotal(getImporte());
	}

	public void setImporte(BigDecimal importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
	}
	
	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}
	
	public AdressLog getAddresLog() {
		return addresLog;
	}

	public void setAddresLog(AdressLog addresLog) {
		this.addresLog = addresLog;
	}
	
	public Set<AnalisisDeFacturaDet> getPartidas() {
		return partidas;
	}
	
	public boolean agregarPartida(final AnalisisDeFacturaDet det){
		Assert.notNull(det);
		det.setAnalisis(this);
		return partidas.add(det);
	}
	
	public boolean eliminarPartida(final AnalisisDeFacturaDet det){
		det.setAnalisis(null);
		return partidas.remove(det);
	}	

	public boolean equals(Object obj){
    	if(obj==null) return false;
    	if(obj==this) return true;
    	if(getClass()!=obj.getClass()) return false;
    	AnalisisDeFactura other=(AnalisisDeFactura)obj;
    	return new EqualsBuilder()
    	.append(getId(), other.getId())
    	.isEquals();
    }
	
    public int hashCode(){
    	return new HashCodeBuilder(27,93)
    	.append(getId())
    	.toHashCode();
    }
    
    public String toString(){
    	return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
    	.append("Analisis:",id)
    	.append("Importe:", importe)
    	.append("Fecha: "+fecha)
    	.toString();
    }
    
    public void actualizarTotalesAnalizado(){
		BigDecimal importe=BigDecimal.ZERO;
		for(AnalisisDeFacturaDet det:getPartidas()){
			importe=importe.add(det.getImporte());
		}
		CantidadMonetaria imp=CantidadMonetaria.pesos(importe.doubleValue());
		setImporte(imp.amount());
	}

	public RequisicionDe getRequisicionDet() {
		return requisicionDet;
	}

	public void setRequisicionDet(RequisicionDe requisicionDet) {
		this.requisicionDet = requisicionDet;
	}

	public boolean isPrimerAnalisis() {
		return primerAnalisis;
	}

	public void setPrimerAnalisis(boolean primerAnalisis) {
		this.primerAnalisis = primerAnalisis;
	}
    
	public void actualizarCostos(){
		for(AnalisisDeFacturaDet det:getPartidas()){
			det.getEntrada().actualizarCosto();
		}
	}
	
	public String getPrimeraSucursal(){
		return getPartidas().iterator().next().getEntrada().getSucursal().getNombre();
	}
	

}
