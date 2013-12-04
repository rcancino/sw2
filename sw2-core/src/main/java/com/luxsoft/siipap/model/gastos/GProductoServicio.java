package com.luxsoft.siipap.model.gastos;

import java.text.MessageFormat;

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
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Unidad;

/*
 * 
 */
@Entity
@Table (name="SW_GPRODUCTOSERVICIO")
public class GProductoServicio extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="PRODUCTO_ID")
	private Long id;
	
	@Version
	private int version;
	
	@Column (name="CLAVE" ,length=30,nullable=true,unique=false)
	//@NotEmpty (message="La clave no puede ser nula")	
	private String clave;
	
	@Column (name="DESCRIPCION" ,length=75,nullable=false)
	@NotEmpty (message="La descripcion no puede ser nula")
	@Length(max=75)
	private String descripcion;
	
	@ManyToOne (optional=true) @JoinColumn (name="CLASE_ID")	
	private ConceptoDeGasto rubro;
	
	
	@Enumerated (value=EnumType.STRING)	@Column (name="UNIDAD",nullable=false,length=20)
	private Unidad unidad=Unidad.PIEZA;
	
	@Column (name="SERVICIO")
	private boolean servicio=false;
	
	@Column (name="INVENTARIABLE")
	private boolean inventariable=true;
	
	@Column (name="IETU" ,nullable=true)
	private boolean ietu=false;
	
	@Column (name="NACIONAL" )
	private Boolean nacional=false;
	
	@Column (name="INVERSION" )
	private Boolean inversion=false;
	
	@Column (name="CODIGO" ,length=30)
	private String codigo;
	
	@Column (name="COMENTARIO",length=150)
	private String nota;
	
	@Column(name="RETENCION",precision=4,scale=2)
	private Double retencion=0.0;

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

	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		Object old=this.clave;
		this.clave = clave;
		firePropertyChange("clave", old, clave);
	}

	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		Object old=this.descripcion;
		this.descripcion = descripcion;
		firePropertyChange("descripcion", old, descripcion);
	}

	public Unidad getUnidad() {
		return unidad;
	}
	public void setUnidad(Unidad unidad) {
		this.unidad = unidad;
	}
	
	public ConceptoDeGasto getRubro() {
		return rubro;
	}
	public void setRubro(ConceptoDeGasto rubro) {
		Object old=this.rubro;
		this.rubro = rubro;
		firePropertyChange("rubro", old, rubro);
	}

	public boolean isServicio() {
		return servicio;
	}
	public void setServicio(boolean servicio) {
		this.servicio = servicio;
	}

	public boolean isInventariable() {
		return inventariable;
	}
	public void setInventariable(boolean inventariable) {
		this.inventariable = inventariable;
	}

	public Boolean getNacional() {
		return nacional;
	}
	public void setNacional(Boolean nacional) {
		this.nacional = nacional;
	}

	public boolean isIetu() {
		return ietu;
	}
	public void setIetu(boolean ietu) {
		this.ietu = ietu;
	}
	
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Boolean getInversion() {
		return inversion;
	}
	public void setInversion(Boolean inversion) {
		this.inversion = inversion;
	}
	
	public String getNota() {
		return nota;
	}
	public void setNota(String nota) {
		this.nota = nota;
	}

	public String toString(){
		return getDescripcion();
	}
	
	public boolean equals(final Object obj){
		if(obj==null) return false;
		if(obj==this) return true;
		if(!getClass().isAssignableFrom(obj.getClass())) return false;
		GProductoServicio prod=(GProductoServicio)obj;
		return new EqualsBuilder()
		.append(getDescripcion(), prod.getDescripcion())
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder(17,35)
		.append(getDescripcion())
		.toHashCode();
	}
	
	public Double getRetencion() {
		return retencion;
	}
	public void setRetencion(Double retencion) {
		Object old=this.retencion;
		this.retencion = retencion;
		firePropertyChange("retencion", old, retencion);
	}

	

}
