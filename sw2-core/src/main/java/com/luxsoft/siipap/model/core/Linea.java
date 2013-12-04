package com.luxsoft.siipap.model.core;

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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.AccessType;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.sw3.replica.Replicable;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 
 * Permite clasificar los productos en jerarquias
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_LINEAS")
public class Linea extends BaseBean implements Replicable{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="LINEA_ID")
	private Long id;
	
	@Column(name="NOMBRE",length=200,nullable=false,unique=true)
	@NotNull @Length(max=200)
	private String nombre;
	
	@Column(length=200)
	@Length(max=200)
	private String descripcion;
	
	@ManyToOne(optional=true,fetch=FetchType.LAZY)
	@JoinColumn (name="PADRE_ID")
	private Linea padre;
	
	@AccessType (value="field")
	@OneToMany (mappedBy="padre"
		, cascade={CascadeType.PERSIST,CascadeType.MERGE}
		,fetch=FetchType.LAZY)
	private Set<Linea> children=new HashSet<Linea>();
	
	@ManyToOne(optional=true,fetch=FetchType.LAZY,cascade={CascadeType.MERGE,CascadeType.PERSIST})
	@JoinColumn (name="CORTE_ID")
	private Corte corte;
	
	public Linea(){
		
	}
	public Linea(String nomb,String desc){
		this.nombre=nomb;
		this.descripcion=desc;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		Object old=this.descripcion;
		this.descripcion = descripcion;
		firePropertyChange("descripcion", old, descripcion);
	}	

	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		Object old=this.nombre;
		this.nombre = nombre;
		firePropertyChange("nombre", old, nombre);
	}
	
	public Linea getPadre() {
		return padre;
	}
	public void setPadre(Linea padre) {
		this.padre = padre;
	}
	
	@SuppressWarnings("unchecked")
	public Set<Linea> getChildren() {
		return Collections.unmodifiableSet(children);
	}
	
	public boolean agregarChildren(final Linea linea){
		linea.setPadre(this);
		return children.add(linea);
	}
	
	public boolean eliminarChildren(final Linea linea){
		linea.setPadre(null);
		return children.remove(linea);
	}
	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(obj==this) return true;
		
		if (getClass() != obj.getClass())
			return false;		
		Linea otro=(Linea)obj;
		return new EqualsBuilder()
		.append(getPadre(), otro.getPadre())
		.append(getNombre(), otro.getNombre())
		.isEquals();
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(getPadre())
		.append(getNombre())
		.toHashCode();
	}
	
	public String toString(){
		return getNombre();
	}
	public Corte getCorte() {
		return corte;
	}
	public void setCorte(Corte corte) {
		this.corte = corte;
	}
	public Date getImportado() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setImportado(Date time) {
		// TODO Auto-generated method stub
		
	}
	public Date getReplicado() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setReplicado(Date time) {
		// TODO Auto-generated method stub
		
	}
	

}
