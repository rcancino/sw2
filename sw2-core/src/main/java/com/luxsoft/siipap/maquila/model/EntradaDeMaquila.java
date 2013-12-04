package com.luxsoft.siipap.maquila.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;

import com.luxsoft.siipap.inventarios.model.CostoHojeable;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;
import com.luxsoft.sw3.maquila.model.AnalisisDeHojeo;


/**
 * Entrada de material de maquila a los almacenes de operacion
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_INVENTARIO_MAQ")
public class EntradaDeMaquila extends Inventario implements MovimientoConFlete,CostoHojeable{
	
	@ManyToOne(optional=false,fetch=FetchType.LAZY)
	@JoinColumn (name="RECEPCION_ID",nullable=false)		
	private RecepcionDeMaquila recepcion;
	
	/** Propiedades utilies en la importacion inicial**/
	
	@Column (name="MAQUILA_ID")
	private Long maquilaId;
	
	@Column (name="MAQUILA_TIPO",length=3)
	private String tipoMaquila;
	
	@Column(name="REMISION_F")
	@Type(type="date")
	private Date fechaRemision;
	
	@Column(name="REMISION",length=20)
	private String  remision;
	
	@Column(name="MAQUILADOR",length=50)
	private String maquilero;
	
	@Column(name="COSTO_MP",nullable=false,scale=6,precision=16)
	private BigDecimal costoMateria=BigDecimal.ZERO;
	
	@Column(name="COSTO_FLETE",nullable=false,scale=6,precision=16)
	private BigDecimal costoFlete=BigDecimal.ZERO;
	
	@Column(name="COSTO_CORTE",nullable=false,scale=6,precision=16)
	private BigDecimal costoCorte=BigDecimal.ZERO;
	
	@Formula("(select IFNULL(sum(a.CANTIDAD),0) FROM SX_MAQ_SALIDA_HOJEADODET " +
			" a where a.INVENTARIO_ID=INVENTARIO_ID)"
			)
	private double atendido;
	
	@Formula("(select IFNULL(sum(a.CANTIDAD),0) FROM SX_MAQ_SALIDA_BOBINAS " +
	" a where a.INVENTARIO_ID=INVENTARIO_ID)")
	private double atendidoDirecto;
	
	@ManyToOne(optional=true) 
    @JoinColumn(name="ANALISIS_FLETE_ID")
	private AnalisisDeFlete analisisFlete;

	@ManyToOne(optional=true) 
    @JoinColumn(name="ANALISIS_HOJEO_ID")
	private AnalisisDeHojeo analisisHojeo;
	
	
	
	@Override
	public String getTipoDocto() {
		return "MAQ";
	}

	public RecepcionDeMaquila getRecepcion() {
		return recepcion;
	}
	
	public void setRecepcion(RecepcionDeMaquila recepcion) {
		this.recepcion = recepcion;
	}



	public Long getMaquilaId() {
		return maquilaId;
	}

	public void setMaquilaId(Long maquilaId) {
		this.maquilaId = maquilaId;
	}

	public String getTipoMaquila() {
		return tipoMaquila;
	}

	public void setTipoMaquila(String tipoMaquila) {
		this.tipoMaquila = tipoMaquila;
	}

	public Date getFechaRemision() {
		return fechaRemision;
	}

	public void setFechaRemision(Date fechaRemision) {
		this.fechaRemision = fechaRemision;
	}

	public String getRemision() {
		return remision;
	}

	public void setRemision(String remision) {
		this.remision = remision;
	}
	
	

	public String getMaquilero() {
		return maquilero;
	}

	public void setMaquilero(String maquilero) {
		this.maquilero = maquilero;
	}

	@Override
	public void setProducto(Producto producto) {
		super.setProducto(producto);
		if(producto!=null){
			if(producto.getUnidad().getUnidad().equals("MIL"))
				setTipoMaquila("MQH");
			else
				setTipoMaquila("MQB");
		}
		
		
	}

	public BigDecimal getCostoMateria() {
		return costoMateria;
	}

	public void setCostoMateria(BigDecimal costoMateria) {
		Object old=this.costoMateria;
		this.costoMateria = costoMateria;
		firePropertyChange("costoMateria", old, costoMateria);
	}

	public BigDecimal getCostoFlete() {
		return costoFlete;
	}

	public void setCostoFlete(BigDecimal costoFlete) {
		Object old=this.costoFlete;
		this.costoFlete = costoFlete;
		firePropertyChange("costoFlete", old, costoFlete);
	}
	
	public BigDecimal getImporteDelFlete(){
		BigDecimal imp=getCostoFlete().multiply(BigDecimal.valueOf(getCantidadEnUnidad()));
		return CantidadMonetaria.pesos(imp).amount();
	}

	public BigDecimal getCostoCorte() {
		return costoCorte;
	}

	public void setCostoCorte(BigDecimal costoCorte) {
		Object old=this.costoCorte;
		this.costoCorte = costoCorte;
		firePropertyChange("costoCorte", old, costoCorte);
	}

	public double getAtendido() {
		return atendido;
	}

	public void setAtendido(double atendido) {
		this.atendido = atendido;
	}	
	
	public double getAtendidoDirecto() {
		return atendidoDirecto;
	}


	public AnalisisDeFlete getAnalisisFlete() {
		return analisisFlete;
	}

	public void setAnalisisFlete(AnalisisDeFlete analisisFlete) {
		this.analisisFlete = analisisFlete;
	}
	
	

	

	public AnalisisDeHojeo getAnalisisHojeo() {
		return analisisHojeo;
	}

	public void setAnalisisHojeo(AnalisisDeHojeo analisisHojeo) {
		this.analisisHojeo = analisisHojeo;
	}

	/**
	 * Propiedad dinamica para indicar la cantidad de hojas pendientes
	 * de asignar salida de maquilador 
	 * 
	 * @return
	 */
	public double getPendiente(){
		return getCantidad()-getAtendido()-getAtendidoDirecto();
	}

	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntradaDeMaquila other = (EntradaDeMaquila) obj;
        return new EqualsBuilder()
        .append(getProducto(),other.getProducto())
        .append(getRenglon(),other.getRenglon())
        .append(getDocumento(), other.getDocumento())
        .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,35)
        .append(getProducto())
        .append(getRenglon())
        .append(getDocumento())
        .toHashCode();
    }
    
    public void actualizarCosto(){
    	setCosto(getCostoMateria().add(getCostoFlete().add(getCostoCorte())));
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
		actualizarDescripcion();
	}

	public double getAncho() {
		return ancho;
	}

	public void setAncho(double ancho) {
		double old=this.ancho;
		this.ancho = ancho;
		firePropertyChange("ancho", old, ancho);
		actualizarDescripcion();
	}
	
	public void actualizarDescripcion(){
		NumberFormat nf=new DecimalFormat("#.#");
		String desc=MessageFormat.format("{0} {1}X{2}"
				, getProducto().getDescripcion()
				,nf.format(getAncho())
				,nf.format(getLargo())
				);
		setDescripcion(desc);
	}

}
