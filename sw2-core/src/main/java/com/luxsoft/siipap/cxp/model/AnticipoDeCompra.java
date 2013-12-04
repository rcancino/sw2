package com.luxsoft.siipap.cxp.model;

import java.math.BigDecimal;
import java.util.Currency;
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
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.model.AdressLog;

/**
 * Entidad para controlar la generacion y administracion de un anticipo a proveedores
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_CXP_ANTICIPOS")
public class AnticipoDeCompra extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ANTICIPO_ID")
	private Long id;
	
	@Version
	private int version;
	
	@Column(name = "DOCUMENTO", length = 20, nullable = false)
	@NotNull @Length(max=20)
	private String documento;
	
	@ManyToOne (optional=false)
	@JoinColumn (name="PROVEEDOR_ID", nullable=false, updatable=false)
	@NotNull
	private Proveedor proveedor;
	
	@ManyToOne(optional=false,cascade={CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE})
	@JoinColumn (name="FACTURA_ID",nullable=false)
	private CXPFactura factura;
	
	@ManyToOne(optional=true
			,cascade={CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE}
	)
	@JoinColumn (name="NOTA_ID",nullable=true)	
	private CXPNota nota;
	
	@ManyToOne(optional=true
			,cascade={CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE}
	)
	@JoinColumn (name="DESCUENTO_NOTA_ID",nullable=true)	
	private CXPNota descuentoNota;
	
	@Column(name="DOCTO_DESC_F",length=30)
	private String documentoNota;
	
	@Column(name="DOCTO_DESC_COMERCIAL",length=30)
	private String documentoDescuentoComercial;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	@NotNull
	private Date fecha=new Date();
	
	@Column (name="MONEDA",length=3,nullable=false)
	@NotNull
	private Currency moneda=MonedasUtils.PESOS;
	
	@Column(name="TC",nullable=false)
	@NotNull
	private double tc=1;
	
	@Column(name="COMENTARIO")
	@Length(max=250)
	private String comentario;
		
	
	@Column (name="IMPORTE",nullable=false)
	private BigDecimal importe=BigDecimal.ZERO;
	
	@Formula("(select sum(Z.IMPORTE) FROM SX_ANALISIS Z where Z.CXP_ID in " +
			"(select X.CXP_ID FROM SX_CXP X where X.ANTICIPO_ID=ANTICIPO_ID) )")
	private BigDecimal aplicado=BigDecimal.ZERO;
	
	@Column(name="DESCUENTO")
	private double descuento;
	
	@Column(name="DESCUENTO_FINANCIERO")
	private double descuentoFinanciero;
	
	@OneToMany(fetch=FetchType.LAZY ,mappedBy="anticipo",cascade={
			CascadeType.PERSIST,CascadeType.MERGE
	})
	private Set<CXPFactura> facturas=new HashSet<CXPFactura>();
	
	@Column(name="DIFERENCIA")
	private BigDecimal diferencia=BigDecimal.ZERO;
	
	@Column(name="DIFERENCIA_FECHA")
	@Type(type="date")
	private Date diferenciaFecha;
	
	
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

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public CXPFactura getFactura() {
		return factura;
	}

	public void setFactura(CXPFactura factura) {
		this.factura = factura;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
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

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
	}

	public Set<CXPFactura> getFacturas() {
		return facturas;
	}

	public void setFacturas(Set<CXPFactura> facturas) {
		this.facturas = facturas;
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
	}
	

	public CXPNota getNota() {
		return nota;
	}

	public void setNota(CXPNota nota) {
		this.nota = nota;
	}

	public boolean equals(Object obj){
    	if(obj==null) return false;
    	if(obj==this) return true;
    	if(getClass()!=obj.getClass()) return false;
    	AnticipoDeCompra other=(AnticipoDeCompra)obj;
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
    	.append(fecha)
    	.append(importe)
    	.toString();
    }

	public BigDecimal getAplicado() {
		if(aplicado==null)
			aplicado=BigDecimal.ZERO;
		//return MonedasUtils.calcularTotal(aplicado);
		return aplicado;
	}

	public void setAplicado(BigDecimal aplicado) {
		Object old=this.aplicado;
		this.aplicado = aplicado;
		firePropertyChange("aplicado", old, aplicado);
	}
	
	
	public double getDescuentoFinanciero() {
		return descuentoFinanciero;
	}

	public void setDescuentoFinanciero(double descuentoFinanciero) {
		double old=this.descuentoFinanciero;
		this.descuentoFinanciero = descuentoFinanciero;
		firePropertyChange("descuentoFinanciero", old, descuentoFinanciero);
	}

	public BigDecimal getImporteDescuentoFinanciero(){
		if(getDescuentoFinanciero()>0){
			CantidadMonetaria imp=CantidadMonetaria.pesos(getImporte());
			if(getDescuentoNota()!=null){
				imp=imp.subtract(getDescuentoNota().getTotalCM());//getImporte().subtract(getDescuentoNota().getTotal());
			}
			CantidadMonetaria desc= imp.multiply(getDescuentoFinanciero()).divide(100);
			return desc.amount();
		}else
			return BigDecimal.ZERO;
	}
	
	
	
	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		double old=this.descuento;
		this.descuento = descuento;
		firePropertyChange("descuento", old, descuento);
	}

	public BigDecimal getImporteDescuento(){
		if(getDescuento()>0){
			BigDecimal descuento= MonedasUtils.aplicarDescuentosEnCascada(getImporte(), getDescuento());
			return getImporte().subtract(descuento);
		}else
			return BigDecimal.ZERO;
	}

	public BigDecimal getDisponible(){
		BigDecimal importeFacturado=MonedasUtils.calcularImporteDelTotal(getImporte());
		if(getFactura()!=null){
			importeFacturado=getFactura().getImporteMN().amount();
		}
		
		if(getDescuentoNota()!=null){
			importeFacturado=importeFacturado.subtract(getDescuentoNota().getImporteMN().amount().abs());
		}else if(getDescuentoNota()==null && getId()==null){
			BigDecimal descuentoCalculado=getImporteDescuento();
			importeFacturado=importeFacturado.subtract(descuentoCalculado);
		}
		BigDecimal disponible=importeFacturado
				.subtract(getAplicado())
				.subtract(getDiferencia());
		return disponible;
	}
	
	
	public CXPNota getDescuentoNota() {
		return descuentoNota;
	}

	public void setDescuentoNota(CXPNota descuentoNota) {
		this.descuentoNota = descuentoNota;
	}

	@AssertTrue(message="Importe invalido ")
	public boolean validarImporte(){
		return getImporte().doubleValue()>0;
	}
	
	

	public String getDocumentoNota() {
		return documentoNota;
	}

	public void setDocumentoNota(String documentoNota) {
		Object old=this.documentoNota;
		this.documentoNota = documentoNota;
		firePropertyChange("documentoNota", old, documentoNota);
	}
	
	
	
	public String getDocumentoDescuentoComercial() {
		return documentoDescuentoComercial;
	}

	public void setDocumentoDescuentoComercial(String documentoDescuentoComercial) {
		Object old=this.documentoDescuentoComercial;
		this.documentoDescuentoComercial = documentoDescuentoComercial;
		firePropertyChange("documentoDescuentoComercial", old, documentoDescuentoComercial);
	}

	public BigDecimal getDiferencia() {
		if(diferencia==null)
			diferencia=BigDecimal.ZERO;
		return diferencia;
	}

	public void setDiferencia(BigDecimal diferencia) {
		this.diferencia = diferencia;
		setDiferenciaFecha(new Date());
	}

	public Date getDiferenciaFecha() {
		return diferenciaFecha;
	}

	public void setDiferenciaFecha(Date diferenciaFecha) {
		this.diferenciaFecha = diferenciaFecha;
	}
	
	
}
