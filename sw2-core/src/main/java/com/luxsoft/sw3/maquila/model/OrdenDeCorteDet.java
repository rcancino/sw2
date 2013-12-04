package com.luxsoft.sw3.maquila.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Formula;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.core.Producto;


@Entity
@Table(name="SX_MAQ_ORDENESDET") 
public class OrdenDeCorteDet extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "ORDENDET_ID")	
    protected Long id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "ORDEN_ID",nullable=false,updatable=false)
	private OrdenDeCorte orden;
	
	@Column(name="FECHA",nullable=false)
	@NotNull
	protected Date fecha=new Date();
	
	@ManyToOne(optional = false,
			fetch=FetchType.LAZY)			
	@JoinColumn(name = "ALMACEN_ID", nullable = false)
	@NotNull(message="El almacen es mandatorio")
	protected Almacen almacen;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)
	@JoinColumn(name = "PRODUCTO_DESTINO_ID", nullable = false)
	@NotNull(message="El producto destino es mandatorio")
	private Producto destino;
	
	@Column(name = "DESTINO_CLAVE", nullable = false)
	@Length(max = 10)
	protected String claveDestino;

	@Column(name = "DESTINO_DESCRIPCION", nullable = false)
	@Length(max = 250)
	protected String descripcionDestino;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)
	@JoinColumn(name = "ENTRADADET_ID",nullable=false)
	private EntradaDeMaterialDet origen;
	
	@Column(name="METROS2",nullable=false,scale=6,precision=16)
	protected BigDecimal metros2=BigDecimal.ZERO;
	
	@Column(name="KILOS",nullable=false,scale=6,precision=16)
	protected BigDecimal kilos=BigDecimal.ZERO;
	
	@Column(name="MILLARES_ESTIMADOS")
	private double millaresEstimados;
	
	@Column(name="MILLARES_ENTREGADOS")
	private double millaresEntregados;
	
	@Column(name = "COSTO_ESTIMADO", nullable = true)	
	private double costoEstimado;
	
	@Column(name = "COSTO", nullable = true)	
	private double costo;
	
	@Column(name="PRECIO_HOJ")
	private double precioPorKiloHojeado=0;
	
	@Column(name="ENTRADA_DE_MAQUILADOR",length=15)
	private String entradaDeMaquilador;
	
	@Column(name="COMENTARIO")
	protected String comentario;
	
	@Formula("(select IFNULL(sum(a.ENTRADA),0) FROM SX_MAQ_RECEPCION_CORTEDET a where a.ORDENDET_ID=ORDENDET_ID)")
	private BigDecimal recibido=BigDecimal.ZERO;
	
	@Formula("(select IFNULL(max(a.RECEPCIONDET_ID),0) FROM SX_MAQ_RECEPCION_CORTEDET a where a.ORDENDET_ID=ORDENDET_ID)")
	private Long recepcionId;
	
	@Column(name="ESPECIAL")	
	private boolean especial=false;
	
	@Column(name = "LARGO", scale = 3)
	private double largo = 0;
	
	
	@Column(name = "ANCHO", scale = 3)
	private double ancho = 0;
	
	public Producto getDestino() {
		return destino;
	}
	
	public void setDestino(Producto destino) {
		Object old=this.destino;
		this.destino = destino;
		firePropertyChange("destino", old, destino);
		if(destino!=null){
			setClaveDestino(destino.getClave());
			NumberFormat nf=new DecimalFormat("#.#");
			String desc=MessageFormat.format("{0} {1}X{2}"
					, destino.getDescripcion()
					,nf.format(destino.getAncho())
					,nf.format(destino.getLargo())
					);
			setDescripcionDestino(desc);
		}else{
			setClaveDestino(null);
			setDescripcionDestino(null);
		}
	}	
	
	public String getClaveDestino() {
		return claveDestino;
	}
	public void setClaveDestino(String claveDestino) {
		this.claveDestino = claveDestino;
	}
	public String getDescripcionDestino() {
		return descripcionDestino;
	}
	public void setDescripcionDestino(String descripcionDestino) {
		this.descripcionDestino = descripcionDestino;
	}
	
	public EntradaDeMaterialDet getOrigen() {
		return origen;
	}

	public void setOrigen(EntradaDeMaterialDet origen) {
		Object old=this.origen;
		this.origen = origen;
		firePropertyChange("origen", old, origen);
	}

	public OrdenDeCorte getOrden() {
		return orden;
	}
	public void setOrden(OrdenDeCorte orden) {
		this.orden = orden;
	}
	
	public double getPrecioPorKiloHojeado() {
		return precioPorKiloHojeado;
	}
	public void setPrecioPorKiloHojeado(double precioPorKiloHojeado) {
		this.precioPorKiloHojeado = precioPorKiloHojeado;
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
		this.almacen = almacen;
	}

	public BigDecimal getMetros2() {
		if(metros2==null)
			metros2=BigDecimal.ZERO;
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

	public double getMillaresEstimados() {
		return millaresEstimados;
	}

	public void setMillaresEstimados(double millaresEstimados) {
		this.millaresEstimados = millaresEstimados;
	}

	public double getMillaresEntregados() {
		return millaresEntregados;
	}

	public void setMillaresEntregados(double millaresEntregados) {
		this.millaresEntregados = millaresEntregados;
	}

	public double getCostoEstimado() {
		return costoEstimado;
	}

	public void setCostoEstimado(double costoEstimado) {
		this.costoEstimado = costoEstimado;
	}

	public double getCosto() {
		return costo;
	}

	public void setCosto(double costo) {
		this.costo = costo;
	}

	public String getEntradaDeMaquilador() {
		return entradaDeMaquilador;
	}

	public void setEntradaDeMaquilador(String entradaDeMaquilador) {
		this.entradaDeMaquilador = entradaDeMaquilador;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public Long getId() {
		return id;
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
	
	//@AssertTrue(message="Kilos > a disponible en la entrada")
	public boolean validarDisponibleKilos(){
		if(getOrigen()!=null){
			return getOrigen().getDisponibleKilos().compareTo(getKilos())>=0;
		}else
			return false;
	}	

	public BigDecimal getRecibido() {
		return recibido;
	}
	
	

	public Long getRecepcionId() {
		return recepcionId;
	}

	@Override
	public boolean equals(Object o) {
		if(o==this) return true;
		if(o==null) return false;
		if(getClass()!=o.getClass())
			return false;
		OrdenDeCorteDet other=(OrdenDeCorteDet)o;
		return new EqualsBuilder()
		//.append(this.destino, other.getDestino())
		//.append(this.kilos, other.getKilos())
		.append(this.origen, other.getOrigen())
		.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		//.append(this.destino)
		//.append(this.kilos)
		.append(this.origen)
		.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append(this.id)
		.append(this.destino)
		.append(this.fecha)
		.append(getEntradaDeMaquilador())
		.append(this.kilos)
		.append(this.kilos)
		.append(this.metros2)
		.toString();
	}	
	
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
