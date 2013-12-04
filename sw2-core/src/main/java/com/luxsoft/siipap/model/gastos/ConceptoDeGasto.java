package com.luxsoft.siipap.model.gastos;

import java.text.MessageFormat;
import java.util.Collections;
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.AccessType;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;

/**
 * 
 * Clasificacion para organizar los productos/servicios.
 * 
 * La clasificacion puede formar una jerarquia.
 * 
 * Un bien y servicio puede estar asociado a varios bienes y
 * servicios. La relación es unidireccional 
 * 
 * GProductoServicio one-to-many  GClasicicacion
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table (name="SW_CONCEPTO_DE_GASTOS")
public class ConceptoDeGasto extends BaseBean{
	
	@Id
	@GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="CLASE_ID")
	private Long id;
	
	@Column (name="CLAVE", length=20, nullable=false,unique=true)
	@NotNull (message="La clave es obligatoria")
	@Length(min=4,max=20, message="La longitud de la clave debe estar entre 5 y 20 caracteres")
	private String clave;
	
	@Column (name="DESCRIPCION", length=150, nullable=false)
	@NotNull (message="La clave es obligatoria") @Length(max=150)
	private String descripcion;
	
	@Column (name="IETU", nullable=false)
	private boolean ietu=true;
	
	@Column (name="AUMENTO")
	private Boolean aumento=false;
	
	@Column(name="CUENTACONTABLE",nullable=true,unique=true,length=30)	
	@Length (max=30,message="El rango maximo es de 30 caracteres")
	private String cuentaContable;
	
	@Column(name="INVERSION")
	private boolean inversion=false;
	
	
	private TipoDeGasto tipo=TipoDeGasto.ADMINISTRATIVO;
	
	@ManyToOne(optional=true)
	@JoinColumn (name="PARENT_ID")
	private ConceptoDeGasto parent;
	
	@AccessType (value="field")
	@OneToMany (mappedBy="parent", cascade={CascadeType.PERSIST,CascadeType.MERGE},fetch=FetchType.LAZY)
	private Set<ConceptoDeGasto> children=new HashSet<ConceptoDeGasto>();
	
	@Column (name="TASA",precision=6,scale=4)
	private double tasa;
	
	public ConceptoDeGasto(){}

	public ConceptoDeGasto(String clave, String descripcion) {
		super();
		this.clave = clave;
		this.descripcion = descripcion;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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

	public Boolean getAumento() {
		return aumento;
	}
	public void setAumento(Boolean aumento) {
		this.aumento = aumento;
	}

	public boolean isIetu() {
		return ietu;
	}
	public void setIetu(boolean ietu) {
		this.ietu = ietu;
	}

	public TipoDeGasto getTipo() {
		return tipo;
	}
	public void setTipo(TipoDeGasto tipo) {
		this.tipo = tipo;
	}

	public ConceptoDeGasto getParent() {
		return parent;
	}
	public void setParent(ConceptoDeGasto parent) {
		Object old=this.parent;
		this.parent = parent;
		firePropertyChange("parent", old, parent);
	}
	
	public Set<ConceptoDeGasto> getChildren() {
		return Collections.unmodifiableSet(children);
	}
	
	public boolean addChilder(final ConceptoDeGasto child){
		Assert.notNull(child,"La clasificacion no debe ser nula");
		child.setParent(this);
		boolean res=children.add(child);
		if(res)
			child.setParent(this);
		return res;
	}
	
	public boolean removeChilder(final ConceptoDeGasto child){
		Assert.notNull(child,"La clasificacion no debe ser nula");
		boolean res=children.remove(child);
		if(res)
			child.setParent(null);
		return res;
	}

	public boolean equals(Object o){
		if(o==null) return false;
		if(o==this) return true;
		if(!(o instanceof ConceptoDeGasto)) return false;
		ConceptoDeGasto otro=(ConceptoDeGasto)o;
		return new EqualsBuilder()
		.append(getClave(), otro.getClave())
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder(17,35)
		.append(getClave())
		.toHashCode();
	}
	
	public String toString(){
		String pattern="{0} ({1})";
		return MessageFormat.format(pattern,getDescripcion(),getClave());
	}

	public String getCuentaContable() {
		return cuentaContable;
	}
	
	public String getCuentaOrigen(){
		if(!StringUtils.isBlank(cuentaContable)){
			return getCuentaContable();
		}else{
			if(getParent()!=null)
				return getParent().getCuentaOrigen();
		}
		return "SIN CUENTA RubroId:"+getId();
	}

	public void setCuentaContable(String cuentaContable) {
		Object old=this.cuentaContable;
		this.cuentaContable = cuentaContable;
		firePropertyChange("cuentaContable", old, cuentaContable);
	}
	
	public ConceptoDeGasto getRubroCuentaOrigen(){
		if(!StringUtils.isBlank(cuentaContable)){
			return this;
		}else{
			if(getParent()!=null)
				return getParent().getRubroCuentaOrigen();
		}
		return null;
	}
	
	public ConceptoDeGasto getRubroSegundoNivel(ConceptoDeGasto rubro){
		if(rubro.getParent()!=null){
			if(rubro.getParent().getParent()==null){
				return rubro;
			}else
				return getRubroSegundoNivel(rubro.getParent());
		}else
			return rubro;
		
		
	}
	
	public double getTasa() {
		return tasa;
	}

	public void setTasa(double tasa) {
		double old=this.tasa;
		this.tasa = tasa;
		firePropertyChange("tasa", old, tasa);
	}
	
	
	public boolean getInversion() {
		return inversion;
	}

	public void setInversion(boolean inversion) {
		this.inversion = inversion;
	}

	public ConceptoDeGasto getRubroOperativo(){
		return this;
		/*
		if(getParent()==null)
			return this;
		else{
			if(getParent().getParent()==null){
				return this;
			}else
				return getParent().getRubroOperativo();
		}
		*/
	}
	

}
