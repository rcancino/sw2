package com.luxsoft.siipap.model.tesoreria;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Currency;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.util.MonedasUtils;



@Entity
@Table (name="SW_CUENTAS")
public class Cuenta extends BaseBean{
	
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;	
		
	@ManyToOne(optional=false,fetch=FetchType.EAGER)
    @JoinColumn(name="BANCO_ID", nullable=false,updatable=false)
    @NotNull (message="El Banco es obligatorio")
	private Banco banco;
	
	@Column(name="CLAVE",nullable=false, unique=true, length=30)	 
	@Length(min=1, max=30,message="La clave debe ser de  5 a 30 caracteres")
	private String clave="";
	
	@Column(name="numero",nullable=false)
	@NotNull (message="El número de cuenta es obligatorio")
	private Long numero;
	
	@Column(name="DESCRIPCION",length=55)
	@Length (max=55)
	private String descripcion;
	
	@Column(name="CUENTACONTABLE",nullable=false,unique=true,length=30)
	@NotEmpty(message="La cuenta contable es obligatoria")
	@Length (max=30,message="El rango maximo es de 30 caracteres")
	private String cuentaContable;
	
	@Column(name="MONEDA",nullable=false,length=3)
	private Currency moneda=MonedasUtils.PESOS;
	
	@Enumerated (EnumType.STRING)
	@Column(name="TIPO",nullable=false,updatable=false)
	private Clasificacion tipo=Clasificacion.CHEQUES;
	
	@Column(name="ENCRIPTAR",nullable=false,updatable=false,insertable=true)
	private boolean encriptar=false;
	
	@Column(name="ACTIVO_VTA",nullable=false,columnDefinition=" bit default false")
	private boolean activoEnVentas=false;
	
	
	public Cuenta() {}
	
	public Cuenta(Banco banco) {		
		this.banco = banco;
	}

	public Cuenta(Banco banco, String clave, Long numero) {		
		this.banco = banco;
		this.clave = clave;
		this.numero = numero;
	}
	
	public Banco getBanco() {
		return banco;
	}
	public void setBanco(Banco banco) {
		Object old=this.banco;
		this.banco = banco;
		firePropertyChange("banco", old, banco);
	}

	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		String old=this.clave;
		this.clave = clave;
		firePropertyChange("clave", old, clave);
	}

	public String getCuentaContable() {
		return cuentaContable;
	}
	public void setCuentaContable(String cuentaContable) {
		String old=this.cuentaContable;
		this.cuentaContable = cuentaContable;
		firePropertyChange("cuentaContable", old, cuentaContable);
	}

	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Currency getMoneda() {
		return moneda;
	}
	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}

	public Long getNumero() {
		return numero;
	}
	public void setNumero(Long numero) {
		Object old=this.numero;
		this.numero = numero;
		firePropertyChange("numero", old, numero);
	}
	
	@Enumerated(EnumType.STRING)
	@Column(name="TIPO",nullable=false,updatable=false)
	public Clasificacion getTipo() {
		return tipo;
	}
	public void setTipo(Clasificacion tipo) {
		Object old=this.tipo;
		this.tipo = tipo;
		firePropertyChange("tipo", old, tipo);
	}	
	
	public boolean isEncriptar() {
		return encriptar;
	}
	public void setEncriptar(boolean encriptar) {
		this.encriptar = encriptar;
	}
	
	
	
	public boolean isActivoEnVentas() {
		return activoEnVentas;
	}

	public void setActivoEnVentas(boolean activoEnVentas) {
		this.activoEnVentas = activoEnVentas;
	}



	public static enum Clasificacion {		
		INVERSION,		
		CHEQUES;		
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
		final Cuenta other = (Cuenta) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public static NumberFormat nf;
	
	static{
		nf=NumberFormat.getInstance();
		nf.setGroupingUsed(false);
	}
	
	public String toString(){
		String pattern="{0} {1} ({2})";
		//return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE,false);
		return MessageFormat.format(pattern				
				,StringUtils.rightPad(getBanco().getClave(),30)
				,nf.format(getNumero())
				,getMoneda()
				);
	}
	
	
	public String getCuentaDesc(){
		String pattern="{0} ({1})";
		return MessageFormat.format(pattern				
				,getBanco().getClave().trim()
				,nf.format(getNumero())
				);
	}
	
	
}
