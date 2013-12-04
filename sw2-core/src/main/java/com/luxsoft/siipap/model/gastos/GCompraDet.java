package com.luxsoft.siipap.model.gastos;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.util.MonedasUtils;

@Entity
@Table (name="SW_GCOMPRADET")
public class GCompraDet extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="GCOMPRADET_ID")
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne(optional=false)
	@JoinColumn (name="COMPRA_ID",nullable=false)
	private GCompra compra;
	
	@ManyToOne(optional=false,cascade={CascadeType.MERGE,CascadeType.PERSIST})
	@JoinColumn (name="PRODUCTO_ID",nullable=false)
	@NotNull
	private GProductoServicio producto;
	
	@Column (name="CANTIDAD",nullable=false)
	private BigDecimal cantidad=BigDecimal.ZERO;
	
	@Column (name="ENTREGADO",nullable=false)
	private long entregado;
	
	@Column (name="PRECIO",scale=2)
	private BigDecimal precio=BigDecimal.ZERO;
	
	@Column (name="IMPORTE_BRUTO",scale=2)
	private BigDecimal importeBruto=BigDecimal.ZERO;
	
	@Column (name="DESC1",scale=4)
	private double descuento1=0;
	
	@Column (name="DESC2",scale=4)
	private double descuento2;
	
	@Column (name="DESC3",scale=4)
	private double descuento3;
	
	@Column (name="IMPORTE",scale=4)
	private BigDecimal importe=BigDecimal.ZERO;
	
	@Column (name="IMPUESTO",scale=4)
	private double impuesto=MonedasUtils.IVA.doubleValue()*100;
	
	@Column (name="IMPUESTO_IMP",scale=4)
	private BigDecimal impuestoImp=BigDecimal.ZERO;
	
	@Column (name="RET1",scale=4)
	private double retencion1;
	
	@Column (name="RET1_IMPP",scale=4)
	private BigDecimal retencion1Imp=BigDecimal.ZERO;
	
	@Column (name="RET2",scale=4)
	private double retencion2;
	
	@Column (name="RET2_IMP",scale=4)
	private BigDecimal retencion2Imp=BigDecimal.ZERO;
	
	
	
	@Column (name="COMENTARIO",length=150)
	private String comentario;
	
	@Column (name="REFERENCIA")
	private Long referencia;
	
	@ManyToOne (optional=true) @JoinColumn (name="CLASE_ID")	
	private ConceptoDeGasto rubro;
	
	@ManyToOne (optional=true)
	@JoinColumn (name="SUCURSAL_ID", nullable=true)
	private Sucursal sucursal;
	
	@Column(name="CONCEPTO_IETU",length=255)
	//@Transient
	private String conceptoContable;
	
	@Column(name = "FACTURA_REMBOLSO", length = 15)	@Length(max=15)
	private String facturaRembolso;
	
	@ManyToOne (optional=true,cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn (name="PROV_REMBOLSO_ID")
	private GProveedor proveedorRembolso;
	
	public GCompraDet() {}

	public GCompraDet(GProductoServicio producto) {		
		this.producto = producto;
	}
	
	public BigDecimal getCantidad() {
		return cantidad;
	}
	public void setCantidad(BigDecimal cantidad) {
		Object old=this.cantidad;
		this.cantidad = cantidad;
		firePropertyChange("cantidad", old, cantidad);
	}

	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public GCompra getCompra() {
		return compra;
	}

	public void setCompra(GCompra compra) {
		this.compra = compra;
	}

	public double getDescuento1() {
		return descuento1;
	}

	public void setDescuento1(double descuento1) {
		double old=this.descuento1;
		this.descuento1 = descuento1;
		firePropertyChange("descuento1", old, descuento1);
	}

	public double getDescuento2() {
		return descuento2;
	}

	public void setDescuento2(double descuento2) {
		Object old=this.descuento2;
		this.descuento2 = descuento2;
		firePropertyChange("descuento2", old, descuento2);
	}

	public double getDescuento3() {
		return descuento3;
	}

	public void setDescuento3(double descuento3) {
		Object old=this.descuento3;
		this.descuento3 = descuento3;
		firePropertyChange("descuento3", old, descuento3);
	}

	public long getEntregado() {
		return entregado;
	}

	public void setEntregado(long entregado) {
		this.entregado = entregado;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getImporte() {
		return importe;
	}
	public void setImporte(BigDecimal importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
	}

	public BigDecimal getImporteBrutoMN() {
		BigDecimal tot=getImporte().multiply(getCompra().getTc());
		return CantidadMonetaria.pesos(tot.doubleValue()).amount();
	}
	
	public BigDecimal getImporteBruto() {
		return importeBruto;
	}
	public void setImporteBruto(BigDecimal importeBruto) {
		this.importeBruto = importeBruto;
	}

	public BigDecimal getPrecio() {
		return precio;
	}
	public void setPrecio(BigDecimal precio) {
		Object old=this.precio;
		this.precio = precio;
		firePropertyChange("precio", old, precio);
	}

	public GProductoServicio getProducto() {
		return producto;
	}
	public void setProducto(GProductoServicio producto) {
		Object oldValue=this.producto;
		this.producto = producto;
		firePropertyChange("producto", oldValue, producto);
	}

	public Long getReferencia() {
		return referencia;
	}
	public void setReferencia(Long referencia) {
		this.referencia = referencia;
	}

	

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	public ConceptoDeGasto getRubro() {
		return rubro;
	}
	public void setRubro(ConceptoDeGasto rubro) {
		Object old=this.rubro;
		this.rubro = rubro;
		firePropertyChange("rubro", old, rubro);
	}

	public Sucursal getSucursal() {
		return sucursal;
	}
	public void setSucursal(Sucursal sucursal) {
		Object old=this.sucursal;
		this.sucursal = sucursal;
		firePropertyChange("sucursal", old, sucursal);
	}

	public double getImpuesto() {
		return impuesto;
	}
	public void setImpuesto(double impuesto) {
		double old=this.impuesto;
		this.impuesto = impuesto;
		firePropertyChange("impuesto", old, impuesto);
	}

	public BigDecimal getImpuestoImp() {
		return impuestoImp;
	}
	public void setImpuestoImp(BigDecimal impuestoImp) {
		Object old=this.impuestoImp;
		this.impuestoImp = impuestoImp;
		firePropertyChange("impuestoImp", old, impuestoImp);
	}
	
	public CantidadMonetaria getImpuestoMN(){
		BigDecimal val=getImpuestoImp().multiply(getCompra().getTc());
		return CantidadMonetaria.pesos(val.doubleValue());
	}

	public double getRetencion1() {
		return retencion1;
	}
	public void setRetencion1(double retencion1) {
		Object old=this.retencion1;
		this.retencion1 = retencion1;
		firePropertyChange("retencion1", old, retencion1);
	}

	public BigDecimal getRetencion1Imp() {
		return retencion1Imp;
	}
	
	public CantidadMonetaria getRetencion1MN(){
		BigDecimal tot=getRetencion1Imp().multiply(getCompra().getTc());
		return CantidadMonetaria.pesos(tot.doubleValue());
	}
	public void setRetencion1Imp(BigDecimal retencion1Imp) {
		Object old=this.retencion1Imp;
		this.retencion1Imp = retencion1Imp;
		firePropertyChange("retencion1Imp", old, retencion1Imp);
	}

	public double getRetencion2() {
		return retencion2;
	}
	public void setRetencion2(double retencion2) {
		double old=this.retencion2;
		this.retencion2 = retencion2;
		firePropertyChange("retencion2", old, retencion2);
	}
	
	public CantidadMonetaria getRetencion2MN(){
		BigDecimal tot=getRetencion2Imp().multiply(getCompra().getTc());
		return CantidadMonetaria.pesos(tot.doubleValue());
	}

	public BigDecimal getRetencion2Imp() {
		return retencion2Imp;
	}
	public void setRetencion2Imp(BigDecimal retencion2Imp) {
		Object old=this.retencion2Imp;
		this.retencion2Imp = retencion2Imp;
		firePropertyChange("retencion2Imp", old, retencion2Imp);
	}
	
	//@AssertTrue (message="El precio debe ser >0")
	public boolean validarPrecio(){
		return getPrecio().doubleValue()>0;
	}
	
	//@AssertTrue (message="La cantidad debe ser >0")
	public boolean validarCantidad(){
		return getCantidad().doubleValue()>0;
	}
	
	@AssertTrue (message="El descuento  debe ser <100")
	public boolean validarDescuentos(){
		return (getDescuento1()<100d && getDescuento2()<100d && getDescuento3()<100);
	}
	
	public void actualizar(){
/* CPG cambio para compra especial, que permita cantidad en negativo 		
		if(getCantidad().doubleValue()<=0 
				|| getPrecio()==null 
				|| getPrecio().doubleValue()<=0)
*/
		
		if(getCantidad().doubleValue()==0 
				|| getPrecio()==null 
				|| getPrecio().doubleValue()<=0)
			return;
		
		final BigDecimal cant=getCantidad();
		final BigDecimal bruto=cant.multiply(getPrecio());
		setImporteBruto(bruto);
		BigDecimal imp=MonedasUtils.aplicarDescuentosEnCascada(bruto, getDescuento1(),getDescuento2(),getDescuento3()).setScale(2, RoundingMode.HALF_EVEN);
		setImporte(imp);
		
		CantidadMonetaria importeMN=CantidadMonetaria.pesos(imp);
		CantidadMonetaria ivaMN=importeMN.multiply(getImpuesto()/100);
		setImpuestoImp(ivaMN.amount());
		
		//BigDecimal iva=getImporte().multiply(BigDecimal.valueOf(getImpuesto()/100));		
		//setImpuestoImp(iva);
		
		// Aplico las retenciones
		//BigDecimal r1=getImporte().multiply( BigDecimal.valueOf(getRetencion1()/100) );
		CantidadMonetaria r1=importeMN.multiply(getRetencion1()/100);
		setRetencion1Imp(r1.amount());
		CantidadMonetaria r2=importeMN.multiply(getRetencion2()/100);
		//BigDecimal r2=getImporte().multiply( BigDecimal.valueOf(getRetencion2()/100) );
		
		setRetencion2Imp(r2.amount());
	}
	
	public BigDecimal getSubTotal(){
		return getImporte().add(getImpuestoImp());
	}
	
	public BigDecimal getTotal(){
		return getSubTotal().subtract(getRetencion1Imp()).subtract(getRetencion2Imp());
	}
	
	public CantidadMonetaria getTotalMN(){
		BigDecimal tot=getTotal().multiply(getCompra().getTc());
		return CantidadMonetaria.pesos(tot.doubleValue());
	}

	@Override
	public int hashCode() {
		if(getId()==null)
		return new HashCodeBuilder(17,35)
		.append(getSucursal())
		.append(getRubro())				
		.append(getProducto())
		.append(getCantidad())
		.append(getPrecio())
		.append(getImporte())
		.append(getComentario())
		.toHashCode();
		else
			return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)return true;
		if (obj == null)return false;
		if (getClass() != obj.getClass())
			return false;
		final GCompraDet other = (GCompraDet) obj;
		if(getId()==null)
			return new EqualsBuilder()
			.append(getSucursal(), other.getSucursal())
			.append(getRubro(), other.getRubro())		
			.append(getProducto(), other.getProducto())
			.append(getCantidad(), other.getCantidad())
			.append(getPrecio(), other.getPrecio())
			.append(getImporte(), other.getImporte())
			.append(getComentario(), other.getComentario())
			.isEquals();
		else
			return getId().equals(other.getId());
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE,false);
	}
	
	public long getLock(){
		return System.currentTimeMillis();
	}
	
	public String getFactura(){
		if(!getCompra().getFacturas().isEmpty()){
			GFacturaPorCompra fac=getCompra().getFacturas().iterator().next();
			/**
			if(fac.getRequisiciondet()!=null){
				if(fac.getRequisiciondet().getRequisicion().getPago()!=null){
					return fac.getRequisiciondet().getDocumento();
				}else
					return "SIN PAGO";
			}else
				return "FACTURA SIN REQ";
				*/
			return fac.getDocumento();
		}
		return "SIN FACTURA";
	}
	
	public GFacturaPorCompra getFacturacion(){
		if(!getCompra().getFacturas().isEmpty()){
			GFacturaPorCompra fac=getCompra().getFacturas().iterator().next();
			return fac;
		}
		return null;
	}
	
	public CantidadMonetaria getImporteMN(){
		double val=getImporte().multiply(getCompra().getTc()).doubleValue();
		return CantidadMonetaria.pesos(val);
	}
	
	public CantidadMonetaria getImporteMNSinRetencion(){
		BigDecimal val=getImporte().subtract(getRetencion1Imp()).subtract(getRetencion2Imp());
		val=val.multiply(getCompra().getTc());
		return CantidadMonetaria.pesos(val.doubleValue());
	}
	
	@AssertTrue (message="Debe usar primer retencion 1")
	public boolean consistenciaRetenciones(){
		if(getRetencion2()>0){
			return getRetencion1()>0;
		}else
			return true;
			
	}
	
	public CantidadMonetaria getIetu(){
		if(getProducto().isIetu()){
			if(getRubro()!=null){
				if(getRubro().getId()==151654L){
					return CantidadMonetaria.pesos(0);
				}
			}
			return getImporteMN();
		}else
			return CantidadMonetaria.pesos(0);
	}
	
	public String getConceptoContable() {
		return conceptoContable;
	}
	public void setConceptoContable(String conceptoContable) {
		Object old=this.conceptoContable;
		this.conceptoContable = conceptoContable;
		firePropertyChange("conceptoContable", old, conceptoContable);
	}
	public GProveedor getProveedorRembolso() {
		return proveedorRembolso;
	}
	public void setProveedorRembolso(GProveedor proveedorRembolso) {
		Object old=this.proveedorRembolso;
		this.proveedorRembolso = proveedorRembolso;
		firePropertyChange("proveedorRembolso", old, proveedorRembolso);
	}
	public String getFacturaRembolso() {
		return facturaRembolso;
	}
	public void setFacturaRembolso(String facturaRembolso) {
		Object old=this.facturaRembolso;
		this.facturaRembolso = facturaRembolso;
		firePropertyChange("facturaRembolso", old, facturaRembolso);
	}
	
	@Column(name = "SERIE", length = 50)
	@Length(max = 50)
	private String serie;
	
	@Column(name = "MODELO", length = 50)
	@Length(max = 50)
	private String modelo;

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		Object old=this.serie;
		this.serie = serie;
		firePropertyChange("serie", old, serie);
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		Object old=this.modelo;
		this.modelo = modelo;
		firePropertyChange("modelo", old, modelo);
	}
	
	

}
