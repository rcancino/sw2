package com.luxsoft.siipap.cxc.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.tesoreria.Banco;

/**
 * Catalogo de tarjetas de credito
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_TARJETAS")
public class Tarjeta extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	 @Column(name="TARJETA_ID")
	private Long id;
	
	@Column(name="NOMBRE",nullable=false,unique=true)
	@NotNull @Length(max=255)
	private String nombre;
	
	@ManyToOne(optional = true)			
	@JoinColumn(name = "BANCO_ID")
	@NotNull
	private Banco banco;
	
	@Column(name="COMISION_BANCARIA",nullable=false)
	private double comisionBancaria=0;
	
	@Column(name="COMISION_VENTA",nullable=false)
	private double comisionVenta=0;
	
	@CollectionOfElements(fetch=FetchType.EAGER)
	@JoinTable(name="SX_TARJETAS_ESQUEMAS"
		,joinColumns=@JoinColumn(name="TARJETA_ID"))
	@Fetch(value=FetchMode.SUBSELECT)
	private Set<EsquemaPorTarjeta> esquemas=new HashSet<EsquemaPorTarjeta>();
	
	@Column(name="DEBITO",nullable=false)
	private boolean debito=false;
	
	public Long getId() {
		return id;
	}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String clave) {
		Object old=this.nombre;
		this.nombre = clave;
		firePropertyChange("nombre", old, nombre);
	}
	
	public double getComisionBancaria() {
		return comisionBancaria;
	}
	public void setComisionBancaria(double comision) {
		double old=this.comisionBancaria;
		this.comisionBancaria = comision;
		firePropertyChange("comisionBancaria", old, comisionBancaria);
	}	
	
	public double getComisionVenta() {
		return comisionVenta;
	}
	public void setComisionVenta(double comisionVenta) {
		double old=this.comisionVenta;
		this.comisionVenta = comisionVenta;
		firePropertyChange("comisionVenta", old, comisionVenta);
	}
	public Banco getBanco() {
		return banco;
	}
	public void setBanco(Banco banco) {
		Object old=this.banco;
		this.banco = banco;
		firePropertyChange("banco", old, banco);
	}
	

	public boolean isDebito() {
		return debito;
	}
	public void setDebito(boolean debito) {
		boolean old=this.debito;
		this.debito = debito;
		firePropertyChange("debito", old, debito);
	}
	
	public boolean agregarEsquema(Esquema esquema,double comision){
		return esquemas.add(new EsquemaPorTarjeta(esquema,comision));
	}
	
	public boolean eliminarEsquema(EsquemaPorTarjeta esquema){
		return esquemas.remove(esquema);
	}
	
	public Set<EsquemaPorTarjeta> getEsquemas(){
		return esquemas;
	}	
	
	
	@Override
	public boolean equals(Object o) {
		if(o==null) return false;
		if(o==this) return true;
		if(getClass()!=o.getClass())return false;
		Tarjeta other=(Tarjeta)o;
		return new EqualsBuilder()
		.append(nombre, other.getNombre())
		.append(banco, other.getBanco())
		.isEquals();
	}
	
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(nombre)
		.append(banco)
		.toHashCode();
		
	}
	
	@Override
	public String toString() {
		String s1=StringUtils.abbreviate(nombre,50);
		String s2=StringUtils.abbreviate(banco.getNombre(),50);
		
		return s1+ " ("+s2+")";
	}
	
	@AssertTrue(message="El importe de la comisión  debe ser >=0 y <99.99")
	public boolean validarComisionesBancaria(){
		return this.comisionBancaria>=0 && this.comisionBancaria<=99;
	}

	@AssertTrue(message="El importe de la comisión de ventas debe ser >=0 y <99.99")
	public boolean validarComisionesVentas(){
		return this.comisionVenta>=0 && this.comisionVenta<99.99;
	}
}
