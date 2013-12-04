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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.AccessType;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

import com.luxsoft.siipap.model.BaseBean;



/**
 * Catalogo de tipo de proveedores
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table (name="SW_GTIPOS_DE_ROVEEDOR")
public class GTipoProveedor extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="GTIPO_ID")
	private Long id;
	
	
	@Length (max=20)
	@NotEmpty (message="La clave no es opcional")
	@Column (unique=true)
	private String clave;
	
	 
	@Length (max=100)
	@NotEmpty (message="La descripcion no es opcional")
	private String descripcion;
	
	@ManyToOne (optional=true)
	@JoinColumn (name="PARENT_ID")
	private GTipoProveedor parent;
	
	@AccessType (value="field")
	@OneToMany (mappedBy="parent", cascade={CascadeType.PERSIST,CascadeType.MERGE},fetch=FetchType.LAZY)
	private Set<GTipoProveedor> children=new HashSet<GTipoProveedor>();
	
	
	public GTipoProveedor(){}
		
	public GTipoProveedor(String clave, String descripcion) {
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
	
	
	
	public GTipoProveedor getParent() {
		return parent;
	}

	public void setParent(GTipoProveedor padre) {
		Object old=this.parent;
		this.parent = padre;
		firePropertyChange("parent", old, parent);
	}

	@SuppressWarnings("unchecked")
	public Set<GTipoProveedor> getChildren() {
		return Collections.unmodifiableSet(children);
	}
	public void addChildren(final GTipoProveedor tipo){
		if(tipo==null)
			throw new IllegalArgumentException("GTipo hijo nulo");
		if(tipo.getParent()!=null)
			tipo.getParent().removeChildren(tipo);
		tipo.setParent(this);
		children.add(tipo);
	}
	
	public void removeChildren(final GTipoProveedor tipo){
		if(tipo==null)
			throw new IllegalArgumentException("GTipo hijo nulo");
		children.remove(tipo);
		tipo.setParent(null);
	}

	@Override
	public boolean equals(Object o) {
		if(o==null)return false;
		if(o==this)return true;
		if(!(o instanceof GTipoProveedor))
			return false;
		GTipoProveedor otro=(GTipoProveedor)o;
		return new EqualsBuilder()
		.append(getClave(), otro.getClave())
		.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(getClave())
		.toHashCode();
	}
	
	@Override
	public String toString() {
		String pattern="{0} ({1} {2})";
		return MessageFormat.format(pattern, getClave(),getDescripcion(),getParent()!=null?"Padre: "+getParent().getClave():"");
	}
	
	

}
