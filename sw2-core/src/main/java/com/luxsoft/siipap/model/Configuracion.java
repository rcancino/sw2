package com.luxsoft.siipap.model;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.luxsoft.siipap.model.tesoreria.Cuenta;

/**
 * Bean para persistir la configuracion general del sistema
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_CONFIGURACION")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="MODULO",discriminatorType=DiscriminatorType.STRING,length=20)
@DiscriminatorValue("KERNELL")
public class Configuracion extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(optional=false,fetch=FetchType.EAGER)
	@JoinColumn(name="SUCURSAL",nullable=false,updatable=false)
	private Sucursal sucursal;
	
	@ManyToOne(optional=true)
	@JoinColumn(name="CUENTA_PREFERENTE_ID",nullable=true)
	private Cuenta cuentaPreferencial;
	

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
	
	

	public Cuenta getCuentaPreferencial() {
		return cuentaPreferencial;
	}

	public void setCuentaPreferencial(Cuenta cuentaPreferencial) {
		this.cuentaPreferencial = cuentaPreferencial;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if(obj==null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Configuracion other = (Configuracion) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append(id)
		.toString();
	}
	
	/**
	 * Regresa la sucursal local de operacion
	 * 
	 * @return
	 */
	public static Long getSucursalLocalId(){
		String sucursal=System.getProperty("sw3.sucursal.local","1");
		System.err.println("-------------------"+sucursal);
		Long id=Long.valueOf(sucursal);
		return id;
	}
	

}
