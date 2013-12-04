/*
 * Created on 27/10/2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.luxsoft.siipap.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.hibernate.validator.Length;







/**
 * 
 * @author Ruben Cancino
 * 
 */
@Embeddable
public class Direccion implements Serializable{
	
	@Transient
	private transient PropertyChangeSupport suport=new PropertyChangeSupport(this);
	
	@Column(name="CALLE", length=150,nullable=true)
	@Length(max=150,message="Longitud Máxima 150")
	private String calle="";
	
	@Column(name="NUMERO",length=10,nullable=true)
	@Length(max=10,message="Longitud Máxima 10")
	private String numero="";	
	
	@Column(name="NUMEROINT",length=10,nullable=true)
	@Length(max=10,message="Longitud Máxima 10")
	private String numeroInterior="";	
	
	@Column(name="COLONIA",length=100,nullable=true)
	@Length(max=150)
	private String colonia="";
	
	@Column(name="CP",length=6,nullable=true)
	@Length(max=6)
	private String cp="";
	
	@Column(name="DELMPO",length=150,nullable=true)
	@Length(max=150)
	private String municipio="";
	
	
	@Column(name="CIUDAD",length=150,nullable=true)
	@Length(max=150) 
	private String ciudad="";
			
	@Column(name="ESTADO",length=150,nullable=true)
	@Length(max=150) 
	private String estado="";
	
	
	@Length(max=150)
	@Column(name="PAIS")
	private String pais="México";
	
		
	@Column(name="LOCALE")
	private Locale localidad=new Locale("es","mx");

	public Direccion (){
	}
	
	public String getCalle() {
		return calle;
	}
	public void setCalle(String calle) {
		Object old=this.calle;
		this.calle = calle;
		firePropertyChange("calle", old, calle);
	}
	
	public String getCiudad() {
		return ciudad;
	}
	public void setCiudad(String ciudad) {
		Object old=this.ciudad;
		this.ciudad = ciudad;
		firePropertyChange("ciudad", old, ciudad);
	}
	
	public String getColonia() {
		return colonia;
	}
	public void setColonia(String colonia) {
		Object old=this.colonia;
		this.colonia = colonia;
		firePropertyChange("colonia", old, colonia);
	}
	
	public String getCp() {
		return cp;
	}
	public void setCp(final String cp) {
		Object old=this.cp;
		this.cp = cp;
		firePropertyChange("cp", old, cp);
	}
	
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		Object old=this.estado;
		this.estado = estado;
		firePropertyChange("estado", old, estado);
	}

	public String getMunicipio() {
		return municipio;
	}
	public void setMunicipio(String municipio) {
		String old=this.municipio;
		this.municipio = municipio;
		firePropertyChange("municipio", old, municipio);
	}
	
	public String getNumero() {
		return numero;
	}
	public void setNumero(String numero) {
		Object old=this.numero;
		this.numero = numero;
		firePropertyChange("numero", old, numero);
	}	
	
	public String getNumeroInterior() {
		return numeroInterior;
	}
	public void setNumeroInterior(String numeroExterior) {
		Object old=this.numeroInterior;
		this.numeroInterior = numeroExterior;
		firePropertyChange("numeroInterior", old, numeroExterior);
	}
	
	public String getPais() {
		return pais;
	}
	public void setPais(String pais) {
		Object old=this.pais;
		this.pais = pais;
		firePropertyChange("pais", old, pais);
	}
	
	public Locale getLocalidad() {
		return localidad;
	}
	public void setLocalidad(Locale localidad) {
		this.localidad = localidad;
	}

	public void firePropertyChange(final String propertyName,Object oldValue, final Object newValue){
		suport.firePropertyChange(propertyName, oldValue, newValue);
	}
    
    @Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((calle == null) ? 0 : calle.hashCode());
		result = PRIME * result + ((ciudad == null) ? 0 : ciudad.hashCode());
		result = PRIME * result + ((colonia == null) ? 0 : colonia.hashCode());
		result = PRIME * result + ((cp == null) ? 0 : cp.hashCode());
		result = PRIME * result + ((estado == null) ? 0 : estado.hashCode());
		result = PRIME * result + ((localidad == null) ? 0 : localidad.hashCode());
		result = PRIME * result + ((municipio == null) ? 0 : municipio.hashCode());
		result = PRIME * result + ((numero == null) ? 0 : numero.hashCode());
		result = PRIME * result + ((numeroInterior == null) ? 0 : numeroInterior.hashCode());
		result = PRIME * result + ((pais == null) ? 0 : pais.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Direccion other = (Direccion) obj;
		if (calle == null) {
			if (other.calle != null)
				return false;
		} else if (!calle.equals(other.calle))
			return false;
		if (ciudad == null) {
			if (other.ciudad != null)
				return false;
		} else if (!ciudad.equals(other.ciudad))
			return false;
		if (colonia == null) {
			if (other.colonia != null)
				return false;
		} else if (!colonia.equals(other.colonia))
			return false;
		if (cp == null) {
			if (other.cp != null)
				return false;
		} else if (!cp.equals(other.cp))
			return false;
		if (estado == null) {
			if (other.estado != null)
				return false;
		} else if (!estado.equals(other.estado))
			return false;
		if (localidad == null) {
			if (other.localidad != null)
				return false;
		} else if (!localidad.equals(other.localidad))
			return false;
		if (municipio == null) {
			if (other.municipio != null)
				return false;
		} else if (!municipio.equals(other.municipio))
			return false;
		if (numero == null) {
			if (other.numero != null)
				return false;
		} else if (!numero.equals(other.numero))
			return false;
		if (numeroInterior == null) {
			if (other.numeroInterior != null)
				return false;
		} else if (!numeroInterior.equals(other.numeroInterior))
			return false;
		if (pais == null) {
			if (other.pais != null)
				return false;
		} else if (!pais.equals(other.pais))
			return false;
		return true;
	}

	public String toString(){
        String pattern="{0} {1} {2} \n{3}\n{4} CP:{5} {6}";
        return MessageFormat.format(pattern
        		, getCalle()
        		,getNumero()
        		,getNumeroInterior()
        		,getColonia()
        		,getMunicipio()
        		,getCp()
        		,getEstado()
        		);
    }
	
	public String formattedAddress(){
		String pattern="{0} #{1} {2}\n{3}\n{4} C.P.:{5} Ciudad:{6} Estado:{7}";
		return MessageFormat.format(pattern
        		,getCalle() //0
        		,getNumero() //1
        		,getNumeroInterior() //2
        		,getColonia() //3
        		,getMunicipio() //4
        		,getCp() //5
        		,getCiudad() //6
        		,getEstado() //7
        		);
	}
	
	public final synchronized void addPropertyChangeListener(
            PropertyChangeListener listener) {
		suport.addPropertyChangeListener(listener);
		 
	}
	 
	public final synchronized void removePropertyChangeListener(
            PropertyChangeListener listener) {
		suport.removePropertyChangeListener(listener);		
	}
	
	
	
	
}	