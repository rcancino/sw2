package com.luxsoft.sw3.maquila.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.inventarios.model.CostoHojeable;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.model.AdressLog;



/**
 * 
 * Analisis de recepciones de maquilador. Asignan costo
 * 
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_MAQ_ANALISIS_HOJEO")
public class AnalisisDeHojeo extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ANALISIS_ID")
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne (optional=false,fetch=FetchType.EAGER)
	@JoinColumn (name="PROVEEDOR_ID", nullable=false, updatable=false)
	@NotNull
	private Proveedor proveedor;
	
	@ManyToOne (optional=true
			,fetch=FetchType.EAGER,cascade={CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE}
	)
	@JoinColumn (name="CXP_ID", nullable=true, updatable=true)	
	private CXPFactura cxpFactura;
	
	@Column(name="CLAVE",nullable=false,length=4)
	private String clave;
	
	@Column (name="NOMBRE",nullable=true)
	@Length (max=250) @NotNull @NotEmpty(message="El nombre es incorrecto")	
	private String nombre;	
	
	@Column(name = "FACTURA", length = 20, nullable = false)
	@Length(max=20)
	@NotNull
	private String factura;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	@NotNull
	private Date fecha=new Date();
	
	@Column(name = "FACTURA_FECHA", nullable = false)
	@Type(type = "date")
	@NotNull
	private Date fechaFactura;
	
	@Column(name="COMENTARIO")
	@Length(max=250)
	private String comentario;
	
	
	@Column (name="IMPORTE",nullable=false)
	private BigDecimal importe=BigDecimal.ZERO;
	
	@Column (name="IMPUESTO",nullable=false)
	private BigDecimal impuesto=BigDecimal.ZERO;
	
	@Column (name="TOTAL",nullable=false)
	protected BigDecimal total=BigDecimal.ZERO;
	
	@OneToMany(mappedBy="analisisHojeo",fetch=FetchType.LAZY
			,cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private Set<EntradaDeMaquila> entradas=new HashSet<EntradaDeMaquila>();
	
	@OneToMany(mappedBy="analisisHojeo",fetch=FetchType.LAZY
			,cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private Set<TransformacionDet> transformaciones=new HashSet<TransformacionDet>();
	
	@OneToMany(mappedBy="analisisGasto",fetch=FetchType.LAZY
			,cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private Set<EntradaPorCompra> coms=new HashSet<EntradaPorCompra>();
	
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
	       @AttributeOverride(name="createdIp",	column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog=new AdressLog();
	
	@Column (name="IMPORTE_ANALIZADO",nullable=false)
	private BigDecimal analizado=BigDecimal.ZERO;
	
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
		if(proveedor!=null){
			setNombre(proveedor.getNombreRazon());
			setClave(proveedor.getClave());
		}else{
			setNombre(null);
			setClave(null);
		}
		//System.out.println("Proveedor asignado....");
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

	

	public String getFactura() {
		return factura;
	}

	public void setFactura(String factura) {
		Object old=this.factura;
		this.factura = factura;
		firePropertyChange("factura", old, factura);
	}
	
	

	public Date getFechaFactura() {
		return fechaFactura;
	}

	public void setFechaFactura(Date fechaFactura) {
		Object old=this.fechaFactura;
		this.fechaFactura = fechaFactura;
		firePropertyChange("fechaFactura", old, fechaFactura);
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

	
	
	public CantidadMonetaria getImporteMN(){		
		return CantidadMonetaria.pesos(getImporte().doubleValue());
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

	public void setImpuesto(BigDecimal impuesto) {
		Object old=this.impuesto;
		this.impuesto = impuesto;
		firePropertyChange("impuesto", old, impuesto);
	}
	
	public CantidadMonetaria getTotalCM(){
		return new CantidadMonetaria(getTotal(),CantidadMonetaria.PESOS);
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
		return getTotalCM();
	}

	

	public Set<EntradaDeMaquila> getEntradas() {
		return entradas;
	}
	
	public boolean agregarEntrada(EntradaDeMaquila e){
		e.setAnalisisHojeo(this);
		return entradas.add(e);
	}
	public boolean eliminarEntrada(EntradaDeMaquila e){
		e.setAnalisisHojeo(null);
		return entradas.remove(e);
	}
	
	public Set<TransformacionDet> getTransformaciones() {
		return transformaciones;
	}
	
	public boolean agregarTransformacion(TransformacionDet trs){
		trs.setAnalisisHojeo(this);
		return transformaciones.add(trs);
	}
	public boolean eliminarTransformacion(TransformacionDet trs){
		trs.setAnalisisHojeo(null);
		return transformaciones.remove(trs);
	}
	
	
	public Set<EntradaPorCompra> getEntradasCompras() {
		return coms;
	}
	
	public boolean agregarEntradaCompras(EntradaPorCompra e){
		e.setAnalisisGasto(this);
		return coms.add(e);
	}
	public boolean eliminarEntrada(EntradaPorCompra e){
		e.setAnalisisGasto(null);
		return coms.remove(e);
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
	
	/**
	 * Actualiza flete en las entradas al almacen
	 * 
	 */
	public void actualizarCostos(){
		/*
		double totalKilos=0;
		
		for(EntradaDeMaquila e:entradas){
			totalKilos+=(e.getCantidad()/e.getFactor())*e.getProducto().getKilos();
		}
		
		double hojeo=getImporte().doubleValue();
		
		//Asignando costo de proporcionales
		for(EntradaDeMaquila e:entradas){
			//Calculando participacion
			double kilos=e.getKilosCalculados();
			double participacion=kilos/totalKilos;
			
			double millares=e.getCantidad()/e.getFactor();
		
			
			double importeHojeo=hojeo*participacion;			
			double costHojeo=importeHojeo/millares;
			e.setCostoCorte(CantidadMonetaria.pesos(costHojeo).amount());
			
			//Total
			e.setCosto(e.getCostoMateria().add(e.getCostoFlete().add(e.getCostoCorte())));
		}
		*/
		double totalKilos=0;
		List<CostoHojeable> entradas=new ArrayList<CostoHojeable>();
		entradas.addAll(getTransformaciones());
		entradas.addAll(getEntradas());
		entradas.addAll( getEntradasCompras());
				
		for(CostoHojeable e:entradas){
			totalKilos+=(e.getCantidad()/e.getFactor())*e.getProducto().getKilos();
		}
		double hojeo=getImporte().doubleValue();
		
		//Asignando costo de proporcionales
		for(CostoHojeable e:entradas){
			//Calculando participacion
			double kilos=e.getKilosCalculados();
			
			double participacion=kilos/totalKilos;
			
			double millares=e.getCantidad()/e.getFactor();
		
			
			
			double importeHojeo=hojeo*participacion;
			
			double costHojeo=importeHojeo/millares;
			e.setCostoCorte(CantidadMonetaria.pesos(costHojeo).amount());
			e.actualizarCosto();
			//Total
			Inventario iv=(Inventario)e;
			iv.setCosto(e.getCostoMateria().add(e.getCostoFlete().add(e.getCostoCorte())));
		}
		
		//Actualizamos el analizado
		BigDecimal analizado=BigDecimal.ZERO;
		for(CostoHojeable e:entradas){
			BigDecimal cantidad=BigDecimal.valueOf(e.getCantidad()/e.getFactor());
			BigDecimal importeCorte=e.getCostoCorte().multiply(cantidad);
			analizado=analizado.add(importeCorte);
		}
		setAnalizado(MonedasUtils.calcularTotal(analizado));
	}
	
	
	public boolean equals(Object obj){
    	if(obj==null) return false;
    	if(obj==this) return true;
    	if(getClass()!=obj.getClass()) return false;
    	AnalisisDeHojeo other=(AnalisisDeHojeo)obj;
    	return new EqualsBuilder()
    	.append(id, other.getId())
    	.append(factura, other.getFactura())
    	.isEquals();
    }
	
    public int hashCode(){
    	return new HashCodeBuilder(27,93)
    	.append(id)
    	.append(factura)
    	.toHashCode();
    }
    
    public String toString(){
    	return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
    	.append(nombre)
    	.append(factura)
    	.append(fecha)
    	.append(total)
    	.toString();
    }

	public CXPFactura getCxpFactura() {
		return cxpFactura;
	}

	public void setCxpFactura(CXPFactura cxpFactura) {
		this.cxpFactura = cxpFactura;
	}

	public BigDecimal getAnalizado() {
		return analizado;
	}

	public void setAnalizado(BigDecimal analizado) {
		this.analizado = analizado;
	}
	
	
    
 
	
}
