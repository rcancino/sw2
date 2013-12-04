package com.luxsoft.sw3.maquila.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Formula;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.core.Producto;


/**
 * Recepcion unitaria de corte
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_MAQ_RECEPCION_CORTEDET")
public class RecepcionDeCorteDet extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "RECEPCIONDET_ID")
	private Long id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "RECEPCION_ID",nullable=false,updatable=false)
	private RecepcionDeCorte recepcion;
	
	@ManyToOne(optional = false,cascade={CascadeType.MERGE,CascadeType.PERSIST})
	@JoinColumn(name = "ORDENDET_ID",nullable=false,updatable=false)
	private OrdenDeCorteDet corte;
	
	@ManyToOne(optional = false,fetch=FetchType.LAZY)			
	@JoinColumn(name = "ALMACEN_ID", nullable = false)
	@NotNull(message="El almacen es mandatorio")
	private Almacen almacen;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)
	@JoinColumn(name = "PRODUCTO_ID", nullable = false)
	@NotNull(message="El producto destino es mandatorio")
	private Producto producto;
	
	@ManyToOne(optional = false,fetch=FetchType.LAZY)
	@JoinColumn(name = "ENTRADADET_ID",nullable=false)
	@NotNull(message="La entrada origen es mandatoria")
	private EntradaDeMaterialDet origen;
	
	@Column(name="FECHA",nullable=false)
	@NotNull
	private Date fecha=new Date();	
	
	@Column(name="METROS2",nullable=false,scale=6,precision=16)
	protected BigDecimal metros2=BigDecimal.ZERO;
	
	@Column(name="KILOS",nullable=false,scale=6,precision=16)
	protected BigDecimal kilos=BigDecimal.ZERO;
	
	@Column(name="ENTRADA")
	private double entrada;
	
	@Column(name = "COSTO", nullable = true)	
	private double costo;
	
	@Column(name="M2MILLAR")
	private double metros2PorMillar;
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="origen")
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private Set<SalidaDeHojasDet> salidas=new HashSet<SalidaDeHojasDet>();
	
	@Formula("ENTRADA-(select IFNULL(sum(a.CANTIDAD),0) FROM SX_MAQ_SALIDA_HOJEADODET   a where a.RECEPCIONDET_ID=RECEPCIONDET_ID)")
	public double disponible;
	
	public RecepcionDeCorteDet(){}
	
	public RecepcionDeCorteDet(final OrdenDeCorteDet orden){
		setCorte(orden);
		setMetros2(orden.getMetros2());
		setKilos(orden.getKilos());
		setAncho(orden.getAncho());
		setLargo(orden.getLargo());
		setEspecial(orden.isEspecial());
	}

	public RecepcionDeCorte getRecepcion() {
		return recepcion;
	}

	public void setRecepcion(RecepcionDeCorte recepcion) {
		this.recepcion = recepcion;
	}

	public OrdenDeCorteDet getCorte() {
		return corte;
	}

	public void setCorte(OrdenDeCorteDet corte) {
		this.corte = corte;
		if(corte!=null){
			setOrigen(corte.getOrigen());
			setProducto(corte.getDestino());
			setMetros2PorMillar(corte.getDestino().getMetros2PorMillar());
			if(producto.isMedidaEspecial()){
				setMetros2PorMillar(getHojeoTeorico());
			}
			setAlmacen(corte.getAlmacen());
		}else{
			setOrigen(null);
			setProducto(null);
			setMetros2PorMillar(0);
			setAlmacen(null);
		}
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Almacen getAlmacen() {
		return almacen;
	}

	public void setAlmacen(Almacen almacen) {
		Object old=this.almacen;
		this.almacen = almacen;
		firePropertyChange("almacen", old, almacen);
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public EntradaDeMaterialDet getOrigen() {
		return origen;
	}

	public void setOrigen(EntradaDeMaterialDet origen) {
		this.origen = origen;
	}

	public BigDecimal getMetros2() {
		return metros2;
	}

	public void setMetros2(BigDecimal metros2) {
		Object old=this.metros2;
		this.metros2 = metros2;
		firePropertyChange("metros2", old, metros2);
	}

	public BigDecimal getKilos() {
		return kilos;
	}

	public void setKilos(BigDecimal kilos) {
		Object old=this.kilos;
		this.kilos = kilos;
		firePropertyChange("kilos", old, kilos);
	}

	public double getEntrada() {
		return entrada;
	}

	public void setEntrada(double entrada) {
		double old=this.entrada;
		this.entrada = entrada;
		firePropertyChange("entrada", old, entrada);
	}

	public double getCosto() {
		return costo;
	}

	public void setCosto(double costo) {
		double old=this.costo;
		this.costo = costo;
		firePropertyChange("costo", old, costo);
	}
	
	public double getCostoPorMillar(){
		if(getEntrada()>0)
			return getCosto()/(getEntrada()/1000);
		else
			return 0;
	}

	public Long getId() {
		return id;
	}
	
	public double getMetros2PorMillar() {
		return metros2PorMillar;
	}

	public void setMetros2PorMillar(double metros2PorMillar) {
		double old=this.metros2PorMillar;
		this.metros2PorMillar = metros2PorMillar;
		firePropertyChange("metros2PorMillar", old, metros2PorMillar);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}
	

	public double getDisponible() {
		return disponible;
	}

	public void recalcularKilos() {
		if(getOrigen()!=null){
			BigDecimal factor=getOrigen().getFactorDeConversion();
			setKilos(getMetros2().multiply(factor));
		}
		
	}

	public void recalcularMetros() {
		if(getOrigen()!=null ){
			double factor=getOrigen().getFactorDeConversion().doubleValue();
			if(factor>0){
				double metros=getKilos().doubleValue()/factor;
				setMetros2(BigDecimal.valueOf(metros));
			}else
				setMetros2(BigDecimal.ZERO);
		}		
	}
	
	/**
	 * 
	 * @return
	 */
	@AssertTrue(message="La cantidad recibida debe ser > 0")
	public boolean validarEntrada(){
		return getEntrada()>0;
	}
	
	public double getM2Teoricos(){
		double millares=getEntrada()/getProducto().getUnidad().getFactor();
		double res=getMetros2PorMillar()*millares;
		return res;
	}
	
	public double getMerma(){
		return getMetros2().doubleValue()-getM2Teoricos();
	}
	
	public double getMermaPor(){
		if(getM2Teoricos()>0){
			double merma=getMerma();
			return merma*100/getMetros2().doubleValue();
		}else
			return 0;
		
	}
	
	public double getHojeoTeorico(){
		
		if(getMetros2PorMillar()>0){
			double res=getMetros2().doubleValue()/getMetros2PorMillar();
			return res*getProducto().getUnidad().getFactor();
		}else if(getProducto().isMedidaEspecial()){
			double res=(getCorte().getAncho()*getCorte().getLargo())/10d;
			//System.out.println("Calculando hojeao teorico: "+res);
			return res;
		}else
			return 0;
	}
	
	public Set<SalidaDeHojasDet> getSalidas() {
		return salidas;
	}

	public boolean agregarSalida(SalidaDeHojasDet salida){
		salida.setOrigen(this);
		return salidas.add(salida);
	}
	
	public boolean eliminarSalida(SalidaDeHojasDet salida){
		salida.setOrigen(null);
		return salidas.remove(salida);
	}
	
	

	@Override
	public boolean equals(Object o) {
		if(o==this) return true;
		if(o==null) return false;
		if(getClass()!=o.getClass())
			return false;
		RecepcionDeCorteDet other=(RecepcionDeCorteDet)o;
		return new EqualsBuilder()
		.append(this.corte.getId(), other.getCorte().getId())
		.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(this.corte.getId())
		.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)		
		.append(this.producto)
		.append(this.fecha)
		.append(this.entrada)
		.toString();
	}
	
	/**
	 * Util para generar salidas en UI
	 */
	@Transient
	private double cantidadDeSalida;

	public double getCantidadDeSalida() {
		return cantidadDeSalida;
	}

	public void setCantidadDeSalida(double cantidadDeSalida) {
		double old=this.cantidadDeSalida;
		this.cantidadDeSalida = cantidadDeSalida;
		firePropertyChange("cantidadDeSalida", old, cantidadDeSalida);
	}
	
	@Column(name="ESPECIAL")	
	private boolean especial=false;
	
	@Column(name = "LARGO", scale = 3)
	private double largo = 0;
	
	
	@Column(name = "ANCHO", scale = 3)
	private double ancho = 0;
	
	
	public boolean isEspecial() {
		return especial;
	}

	public void setEspecial(boolean especial) {
		this.especial = especial;
	}

	public double getLargo() {
		return largo;
	}

	public void setLargo(double largo) {
		double old=this.largo;
		this.largo = largo;
		firePropertyChange("largo", old, largo);
	}

	public double getAncho() {
		return ancho;
	}

	public void setAncho(double ancho) {
		double old=this.ancho;
		this.ancho = ancho;
		firePropertyChange("ancho", old, ancho);
	}

}
