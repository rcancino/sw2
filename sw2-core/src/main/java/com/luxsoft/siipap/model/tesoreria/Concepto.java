package com.luxsoft.siipap.model.tesoreria;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.luxsoft.siipap.model.BaseBean;

/**
 * Concepto de CargoAbono. Es una forma de clasificar los Ingresos/Abonos
 * 
 * {@link CargoAbono} 
 * 
 * 
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table (name="SW_CONCEPTOS")
public class Concepto extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Column(name="CLAVE",nullable=false, unique=true, length=100)
	private String clave;
	
	@Column(name="DESCRIPCION",nullable=false ,length=100)
	private String descripcion;
	
	@Enumerated(EnumType.STRING)
	@Column(name="TIPO",nullable=false,updatable=false)
	private Tipo tipo=Tipo.CARGO;
	
	@Enumerated(EnumType.STRING)
	@Column(name="CLASE",length=30)
	private Clase clase=Clase.COMPRAS;
	
	public Concepto() {}
	
	public Concepto(String clave, String descripcion) {
		this.clave = clave;
		this.descripcion = descripcion;
	}
	
	public Concepto(String clave, String descripcion,Tipo tipo) {		
		this.clave = clave;
		this.descripcion = descripcion;
		this.tipo=tipo;
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
		this.descripcion = descripcion;
	}

	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}
	
	

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((id == null) ? 0 : id.hashCode());
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
		final Concepto other = (Concepto) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public Clase getClase() {
		return clase;
	}

	public void setClase(Clase clase) {
		this.clase = clase;
	}



	/**
	 * Enumeracion para definir si el concepto es un ingreso o un egreso
	 * 
	 * @author Ruben Cancino
	 *
	 */
	public static enum Tipo{
		
		/**
		 * El concepto es un egreso
		 *  
		 */
		CARGO,
		
		/**
		 * El concepto es un ingreso
		 * 
		 */
		ABONO		
	}

	@Override
	public String toString() {
		return getClave();
	}
	
	public static enum Clase{
		COMPRAS,GASTOS,TESORERIA1,TESORERIA2
	}
	

}
