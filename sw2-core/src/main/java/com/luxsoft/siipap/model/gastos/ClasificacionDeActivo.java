package com.luxsoft.siipap.model.gastos;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;

@Entity
@Table (name="SW_ACTIVO_TIPO")
public class ClasificacionDeActivo extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="IPC_ID")
	private Long id;
	
	@Column(name="NOMBRE", length=100,nullable=false,unique=true)
	@NotNull @Length(max=100)
	private String nombre;
	
	@Column(name="DESCRIPCION", length=150)
	@Length(max=150)
	private String descripcion;
	
	@Column (name="TASA",precision=6,scale=4)
	private double tasa;
	
	@Column(name="CUENTACONTABLE",nullable=true,unique=true,length=30)	
	@Length (max=30,message="El rango maximo es de 30 caracteres")
	private String cuentaContable;
	
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

	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		Object old=this.descripcion;
		this.descripcion = descripcion;
		firePropertyChange("descripcion", old, descripcion);
	}	

	public double getTasa() {
		return tasa;
	}
	public void setTasa(double tasa) {
		double old=this.tasa;
		this.tasa = tasa;
		firePropertyChange("tasa", old, tasa);
	}
	
	public String getCuentaContable() {
		return cuentaContable;
	}
	public void setCuentaContable(String cuentaContable) {
		Object old=this.cuentaContable;
		this.cuentaContable = cuentaContable;
		firePropertyChange("cuentaContable", old, cuentaContable);
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((nombre == null) ? 0 : nombre.hashCode());
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
		final ClasificacionDeActivo other = (ClasificacionDeActivo) obj;
		if (nombre == null) {
			if (other.nombre != null)
				return false;
		} else if (!nombre.equals(other.nombre))
			return false;
		return true;
	}
	
	public String toString(){
		return nombre;
	}
	
	@AssertTrue (message="La tasa debe ser >0 y <=100")
	public boolean validarTasa(){
		return (getTasa()>0 && getTasa()<=100);
	}

}
