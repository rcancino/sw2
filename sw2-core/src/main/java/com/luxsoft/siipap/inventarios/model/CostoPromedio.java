package com.luxsoft.siipap.inventarios.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Formula;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;

@Entity
@Table(name="SX_COSTOS_P"
	,uniqueConstraints=@UniqueConstraint(columnNames={"PRODUCTO_ID","YEAR","MES"})
	)
public class CostoPromedio {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ID")
	private Long id;
	
	@Version
	private int version;
	
	@Column(name="YEAR",nullable=false)
	private int year;
	
	@Column(name="MES",nullable=false)
	private int mes;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="PRODUCTO_ID", nullable=false,updatable=false) 
    @NotNull
    private Producto producto;   
    
    @Column(name="CLAVE",nullable=false)
    @NotNull  @Length(max=10)
    private String clave;
    
    @Transient
    private String descripcion;
    
    @Column(name="COSTOP",nullable=false)
    private BigDecimal costop=BigDecimal.ZERO;
    
    @Column(name="COSTOU",nullable=false)
    private BigDecimal costoUltimo=BigDecimal.ZERO;
    
    @Column(name="COSTOR",nullable=false)
    private BigDecimal costoRepo=BigDecimal.ZERO;
    

    public CostoPromedio(){}
    
    public CostoPromedio(int year,int mes,Producto p){
    	this.year=year;
    	this.mes=mes;
    	setProducto(p);
    }

	public Long getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMes() {
		return mes;
	}

	public void setMes(int mes) {
		this.mes = mes;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
		this.clave=producto.getClave();
		this.descripcion=producto.getDescripcion();
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	

	public BigDecimal getCostop() {
		return costop;
	}

	public void setCostop(BigDecimal costop) {
		this.costop = costop;
	}

	public BigDecimal getCostoUltimo() {
		return costoUltimo;
	}

	public void setCostoUltimo(BigDecimal costoUltimo) {
		this.costoUltimo = costoUltimo;
	}

	public BigDecimal getCostoRepo() {
		return costoRepo;
	}

	public void setCostoRepo(BigDecimal costoRepo) {
		this.costoRepo = costoRepo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mes;
		result = prime * result
				+ ((producto == null) ? 0 : producto.hashCode());
		result = prime * result + year;
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
		CostoPromedio other = (CostoPromedio) obj;
		if (mes != other.mes)
			return false;
		if (producto == null) {
			if (other.producto != null)
				return false;
		} else if (!producto.equals(other.producto))
			return false;
		if (year != other.year)
			return false;
		return true;
	}
    
    public String toString(){
    	return  ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE);
    	
    }
    
    @Formula("(select ifnull(sum(X.CANTIDAD),0) FROM SX_EXISTENCIAS X where X.PRODUCTO_ID=PRODUCTO_ID and X.YEAR=YEAR and X.MES=MES)")
    private double existencia;


	public double getExistencia() {
		return existencia;
	}    
    
	/**
	 * Costo del inventario a valor promedio
	 * 
	 * @return
	 */
    public BigDecimal getCostoAPromedio(){
    	double cant=getExistencia()/getProducto().getUnidad().getFactor();
    	CantidadMonetaria costo=CantidadMonetaria.pesos(getCostop());
    	return costo.multiply(cant).amount();
    }
    
    /**
	 * Costo del inventario a ultimo costo
	 * 
	 * @return
	 */
    public BigDecimal getCostoAUltimo(){
    	double cant=getExistencia()/getProducto().getUnidad().getFactor();
    	CantidadMonetaria costo=CantidadMonetaria.pesos(getCostoUltimo());
    	return costo.multiply(cant).amount();
    }
    
    public String asPeriodo(){
    	return getMes()+ " - "+getYear();
    	//return MessageFormat.format("{}", arguments)
    }

}
