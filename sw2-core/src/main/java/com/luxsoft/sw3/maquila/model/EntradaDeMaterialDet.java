package com.luxsoft.sw3.maquila.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.core.Producto;


/**
 * Es la entidad que representa las entradas de material 
 * al inventario de maquila, Originalmente estan dadas por bobinas
 * Mantiene una relacion bi-direccional many-to-one con RecepcionDeMaterial
 * en lo que es una clasica relacion Padre/Hijo.
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_MAQ_ENTRADASDET")
public class EntradaDeMaterialDet extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "ENTRADADET_ID")	
    protected Long id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "ENTRADA_ID",nullable=false,updatable=false)
	private EntradaDeMaterial recepcion;
	
	
	@ManyToOne(optional = false,
			fetch=FetchType.LAZY)			
	@JoinColumn(name = "ALMACEN_ID", nullable = false)
	@NotNull(message="El almacen es mandatorio")
	protected Almacen almacen;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)
	@JoinColumn(name = "PRODUCTO_ID", nullable = false)
	@NotNull
	protected Producto producto;
	
	@Column(name = "CLAVE", nullable = false)
	@Length(max = 10)
	protected String clave;

	@Column(name = "DESCRIPCION", nullable = false)
	@Length(max = 250)
	protected String descripcion;
	
	@Column(name="ENTRADA_DE_MAQUILADOR",length=15)
	private String entradaDeMaquilador;
	
	@Column(name="FECHA",nullable=false)
	@Type(type="date")
	protected Date fecha=new Date();
	
	@Column(name="METROS2",nullable=false,scale=6,precision=16)
	protected BigDecimal metros2=BigDecimal.ZERO;
	
	@Column(name="KILOS",nullable=false,scale=6,precision=16)
	protected BigDecimal kilos=BigDecimal.ZERO;
	
	@Column(name="IMPORTE")
	private BigDecimal importe;
	
	@Column(name="PRECIO_KILO")
	protected double precioPorKilo;
	
	@Column(name="PRECIO_M2")
	protected double precioPorM2;
	
	@Column(name="OBSERVACIONES")
	protected String observaciones;
	
	@Column(name="BOBINAS",nullable=false)
	private int bobinas=0;
	
	@Transient
	private Set<OrdenDeCorteDet> cortes=new HashSet<OrdenDeCorteDet>();
	
	/// KILOS
	
	@Formula("(select IFNULL(sum(a.KILOS),0) FROM SX_MAQ_ORDENESDET a where a.ENTRADADET_ID=ENTRADADET_ID)")
	private BigDecimal salidaACorteKilos=BigDecimal.ZERO;
	
	@Formula("(select IFNULL(sum(a.CANTIDAD),0) FROM SX_MAQ_SALIDA_BOBINAS a where a.ENTRADADET_ID=ENTRADADET_ID)")
	private BigDecimal salidaAMaqKilos=BigDecimal.ZERO;
	
	@Formula("KILOS" +
			"-(select IFNULL(sum(a.KILOS),0) FROM SX_MAQ_ORDENESDET a where a.ENTRADADET_ID=ENTRADADET_ID)" +
			"-(select IFNULL(sum(a.CANTIDAD),0) FROM SX_MAQ_SALIDA_BOBINAS a where a.ENTRADADET_ID=ENTRADADET_ID)"
			)
	private BigDecimal disponibleKilos=BigDecimal.ZERO;
	
	// METROS2
	
	@Formula("(select IFNULL(sum(a.METROS2),0) FROM SX_MAQ_ORDENESDET a where a.ENTRADADET_ID=ENTRADADET_ID)")
	private BigDecimal salidaACorteM2=BigDecimal.ZERO;
	
	@Formula("(select IFNULL(sum(a.CANTIDAD/(KILOS/METROS2)),0) FROM SX_MAQ_SALIDA_BOBINAS a where a.ENTRADADET_ID=ENTRADADET_ID)")
	private BigDecimal salidaAMaqM2=BigDecimal.ZERO;
	
	@Formula("METROS2" +
			"-(select IFNULL(sum(a.METROS2),0) FROM SX_MAQ_ORDENESDET a where a.ENTRADADET_ID=ENTRADADET_ID)" +
			"-(select IFNULL(sum(a.CANTIDAD/(KILOS/METROS2)),0) FROM SX_MAQ_SALIDA_BOBINAS a where a.ENTRADADET_ID=ENTRADADET_ID)")
	private BigDecimal disponibleEnM2=BigDecimal.ZERO;
	
	@ManyToOne
	@JoinTable(
			name = "SX_MAQ_ENTRADAS_ANALIZADAS",
			joinColumns = {@JoinColumn(name = "ENTRADADET_ID")},
			inverseJoinColumns = {@JoinColumn(name = "ANALISIS_ID")}
	)
	private AnalisisDeMaterial analisis;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public EntradaDeMaterial getRecepcion() {
		return recepcion;
	}

	public void setRecepcion(EntradaDeMaterial recepcion) {
		this.recepcion = recepcion;
	}
	
	public int getBobinas() {
		return bobinas;
	}

	public void setBobinas(int bobinas) {
		this.bobinas = bobinas;
	}	

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		Object old=this.producto;
		this.producto = producto;	
		firePropertyChange("producto", old, producto);
		if(producto!=null){
			this.clave=producto.getClave();
			this.descripcion=producto.getDescripcion();
		}else{
			this.clave=null;
			this.descripcion=null;
		}
	}
	
	public String getEntradaDeMaquilador() {
		return entradaDeMaquilador;
	}

	public void setEntradaDeMaquilador(String entrada) {
		this.entradaDeMaquilador = entrada;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}

	/*public CantidadMonetaria getImporte() {
		return importe;
	}

	public void setImporte(CantidadMonetaria importe) {	
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe",old,importe);
		recalcularPrecios();
	}*/
	
	

	public BigDecimal getKilos() {
		return kilos;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
		recalcularPrecios();
		//recalcularSaldos();
	}

	public void setKilos(BigDecimal kilos) {
		Object old=this.kilos;
		this.kilos = kilos;
		firePropertyChange("kilos", old, kilos);
	}

	public BigDecimal getMetros2() {
		return metros2;
	}

	public void setMetros2(BigDecimal metros2) {
		Object old=this.metros2;
		this.metros2 = metros2;
		firePropertyChange("metros2",old,metros2);
	}
	
	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		Object old=this.observaciones;
		this.observaciones = observaciones;
		firePropertyChange("observaciones", old, observaciones);
	}

	public Almacen getAlmacen() {
		return almacen;
	}

	public void setAlmacen(Almacen almacen) {
		this.almacen = almacen;
	}
	
	

	public BigDecimal getSalidaACorteM2() {
		return salidaACorteM2;
	}

	public BigDecimal getSalidaAMaqM2() {
		return salidaAMaqM2;
	}

	/**
	 * Factor de conversion  Kilos/M2
	 * 
	 * Si se require ir de Kilos a M2 se divide
	 * Si se requiere ir de M2 a Kilos se multiplica
	 * @return
	 */
	public BigDecimal getFactorDeConversion(){
		//return getMetros2().divide(getKilos(),5,RoundingMode.HALF_EVEN);
		//return getKilos().divide(getMetros2(),6,RoundingMode.HALF_EVEN);
		double val=getKilos().doubleValue()/getMetros2().doubleValue();
		return BigDecimal.valueOf(val);
	}
	
	/*public void recalcularPrecios(){
		if(getKilos()!=null && !getKilos().equals(BigDecimal.ZERO)){
			double imp=getImporte().getAmount().doubleValue();
			double kil=getKilos().abs().doubleValue();
			setPrecioPorKilo(imp/kil);
		}
		if(getMetros2()!=null && !getMetros2().equals(BigDecimal.ZERO)){
			double imp=getImporte().getAmount().doubleValue();
			double m2=getMetros2().abs().doubleValue();
			setPrecioPorM2(imp/m2);
		}
	}*/
	
	public void recalcularPrecios(){
		if(getKilos()!=null && !getKilos().equals(BigDecimal.ZERO)){
			double imp=getImporte().doubleValue();
			double kil=getKilos().abs().doubleValue();
			setPrecioPorKilo(imp/kil);
		}
		if(getMetros2()!=null && !getMetros2().equals(BigDecimal.ZERO)){
			double imp=getImporte().doubleValue();
			double m2=getMetros2().abs().doubleValue();
			setPrecioPorM2(imp/m2);
		}
	}
	
	public double getPrecioPorKilo(){
		return precioPorKilo;
	}
	public void setPrecioPorKilo(double precioPorKilo) {
		Object old=this.precioPorKilo;
		this.precioPorKilo = precioPorKilo;
		firePropertyChange("precioPorKilo",old,precioPorKilo);
	}
	
	public double getPrecioPorM2(){
		return precioPorM2;
	}
	public void setPrecioPorM2(double precioPorM2) {
		Object old=this.precioPorM2;
		this.precioPorM2 = precioPorM2;
		firePropertyChange("precioPorM2",old,precioPorM2);
	}

	public BigDecimal getSalidaACorteKilos() {
		return salidaACorteKilos;
	}

	public BigDecimal getSalidaAMaqKilos() {
		return salidaAMaqKilos;
	}

	public BigDecimal getDisponibleEnM2() {
		return disponibleEnM2;
	}

	public void setDisponibleEnM2(BigDecimal disponibleEnM2) {
		Object old=this.disponibleEnM2;
		this.disponibleEnM2 = disponibleEnM2;
		firePropertyChange("disponibleEnM2",old,disponibleEnM2);
	}

	public BigDecimal getDisponibleKilos() {
		return disponibleKilos;
	}

	public void setDisponibleKilos(BigDecimal disponibleKilos) {
		Object old=this.disponibleKilos;
		this.disponibleKilos = disponibleKilos;
		firePropertyChange("disponibleKilos",old,disponibleKilos);
	}

	public Set<OrdenDeCorteDet> getCortes() {
		return cortes;
	}

	public void setCortes(Set<OrdenDeCorteDet> cortes) {
		this.cortes = cortes;
	}
	
	public void agregarCorte(OrdenDeCorteDet corte){
		corte.setOrigen(this);
		cortes.add(corte);
	}
	
	/**
	 * Verifica si esta clase tiene ordenes de corte asociados
	 * es decir maquilado
	 * 
	 * @return
	 */
	public boolean isCortado(){
		return !getCortes().isEmpty();
	}
	
		
	public void recalcularSaldos(){
		BigDecimal m2=BigDecimal.ZERO;
		for(OrdenDeCorteDet c:getCortes()){
			if(c.getMetros2()==null)continue;
			BigDecimal cm2=c.getMetros2();
			m2=m2.add(cm2,MathContext.DECIMAL32);
		}
		Assert.isTrue(m2.compareTo(getMetros2())<=0,"Las salidas a corte no pueden superar los m2 de la entrada");
		setDisponibleEnM2(getMetros2().subtract(m2,MathContext.DECIMAL32));
		BigDecimal kilos=m2.multiply(getFactorDeConversion());
		setDisponibleKilos(getKilos().subtract(kilos,MathContext.DECIMAL32));		
	}
	
	public AnalisisDeMaterial getAnalisis() {
		return analisis;
	}
	public void setAnalisis(AnalisisDeMaterial analisis) {
		this.analisis = analisis;
	}
		
	@Override
	public boolean equals(Object o) {
		if(o==this) return true;
		if(o==null) return false;
		if(getClass()!=o.getClass())
			return false;
		EntradaDeMaterialDet other=(EntradaDeMaterialDet)o;
		return new EqualsBuilder()
		.append(this.producto, other.getProducto())
		.append(this.kilos, other.getKilos())
		.append(this.metros2, other.getMetros2())
		.append(this.importe, other.getImporte())
		.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(this.producto)
		.append(this.kilos)
		.append(this.metros2)
		.append(this.importe)
		.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append(this.id)
		.append(this.clave)
		.append(this.fecha)
		.append(getEntradaDeMaquilador())
		.append(this.kilos)
		.append(this.kilos)
		.append(this.metros2)
		.toString();
	}
	
	 @ManyToOne(optional = true,fetch=FetchType.LAZY)
		@JoinColumn(name = "INVENTARIO_ID",nullable=true)
	 private MovimientoDet movimientoDet;
	 
	 
	 

	public MovimientoDet getMovimientoDet() {
		return movimientoDet;
	}

	public void setMovimientoDet(MovimientoDet movimientoDet) {
		this.movimientoDet = movimientoDet;
	}
	

}
