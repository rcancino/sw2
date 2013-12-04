package com.luxsoft.siipap.embarques;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.model.BaseBean;

/**
 * Servicio de transporte foraneo
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_TRANSPORTES_FORANEOS")
public class ServicioDeTransporte extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="TRANSPORTE_ID")
	private Long id;
	
	@Column(name="NOMBRE", nullable=false)
	@Length(max=255)
	private String nombre;
	
	@Column(name="RFC",length=20)
	@Length(max=20)
	private String rfc;
	
	@Column(name="TEL1", length=30)
	private String telefono1;
	
	@Column(name="TEL2", length=30)
	private String telefono2;
	
	@Column(name="FAX", length=30)
	private String fax;
	
	@Column(name="CALLE", length=50,nullable=true)
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		Object old=this.nombre;
		this.nombre = nombre;
		firePropertyChange("nombre", old, nombre);
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		Object old=this.rfc;
		this.rfc = rfc;
		firePropertyChange("rfc", old, rfc);
	}

	public String getTelefono1() {
		return telefono1;
	}

	public void setTelefono1(String telefono1) {
		Object old=this.telefono1;
		this.telefono1 = telefono1;
		firePropertyChange("telefono1", old, telefono1);
	}

	public String getTelefono2() {
		return telefono2;
	}

	public void setTelefono2(String telefono2) {
		Object old=this.telefono2;
		this.telefono2 = telefono2;
		firePropertyChange("telefono2", old, telefono2);
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		Object old=this.fax;
		this.fax = fax;
		firePropertyChange("fax", old, fax);
	}

	public String getCalle() {
		return calle;
	}

	public void setCalle(String calle) {
		Object old=this.calle;
		this.calle = calle;
		firePropertyChange("calle", old, calle);
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

	public void setNumeroInterior(String numeroInterior) {
		Object old=this.numeroInterior;
		this.numeroInterior = numeroInterior;
		firePropertyChange("numeroInterior", old, numeroInterior);
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

	public void setCp(String cp) {
		Object old=this.cp;
		this.cp = cp;
		firePropertyChange("cp", old, cp);
	}

	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		Object old=this.municipio;
		this.municipio = municipio;
		firePropertyChange("municipio", old, municipio);
	}

	public String getCiudad() {
		return ciudad;
	}

	public void setCiudad(String ciudad) {
		Object old=this.ciudad;
		this.ciudad = ciudad;
		firePropertyChange("ciudad", old, ciudad);
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		Object old=this.estado;
		this.estado = estado;
		firePropertyChange("estado", old, estado);
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
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
		ServicioDeTransporte other = (ServicioDeTransporte) obj;
		if (nombre == null) {
			if (other.nombre != null)
				return false;
		} else if (!nombre.equals(other.nombre))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return nombre;
	}
	
	

}
