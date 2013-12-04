package com.luxsoft.sw2.replica.valida2;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * Entidad para registrar informaci�n de las entidades en replica con objeto
 *  de validar la integridad de informacion
 *  
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_VALIDACIONES_REPLICA")
public class Validacion {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="VALIDACION_ID")
	public Long id;
	
	@Column(name="TABLA",nullable=false,length=50)
	private String tabla;
	
	@Column(name="ENTIDAD",nullable=false,length=50)
	private String entidad;
	
	@Column(name="CONCEPTO",nullable=false,length=12)
	private String concepto;
	
	@Column(name="SUCURSAL_ID",nullable=false)
	private Long sucursalId;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	private Date fecha=new Date();
	
	@Column(name="REG_SUC",nullable=false)
	private int registrosEnSucursal;
	
	@Column(name="REG_CEN",nullable=false)
	private int registrosEnCentral;
	
	@Column(name="CONTROL1_SUC",nullable=false)
	private double control_1_sucursal;
	
	@Column(name="CONTROL1_CEN",nullable=false)
	private double control_1_central;
	
	@Column(name="CONTROL1_DESC")
	private String control_1_desc;
	
	@Column(name="CONTROL2_SUC",nullable=false)
	private double control_2_sucursal;
	
	@Column(name="CONTROL2_CEN",nullable=false)
	private double control_2_central;
	
	@Column(name="CONTROL2_DESC")
	private String control_2_desc;
	
	
	@Column(name="CONTROL3_SUC",nullable=false)
	private double control_3_sucursal;
	
	@Column(name="CONTROL3_CEN",nullable=false)
	private double control_3_central;
	
	@Column(name="CONTROL3_DESC")
	private String control_3_desc;
	
	@Column(name="ACTUALIZACION",nullable=false)
	private Date actualizacion;

	public String getTabla() {
		return tabla;
	}

	public void setTabla(String tabla) {
		this.tabla = tabla;
	}

	public String getEntidad() {
		return entidad;
	}

	public void setEntidad(String entidad) {
		this.entidad = entidad;
	}

	public String getConcepto() {
		return concepto;
	}

	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}

	public Long getSucursalId() {
		return sucursalId;
	}

	public void setSucursalId(Long sucursalId) {
		this.sucursalId = sucursalId;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public int getRegistrosEnSucursal() {
		return registrosEnSucursal;
	}

	public void setRegistrosEnSucursal(int registrosEnSucursal) {
		this.registrosEnSucursal = registrosEnSucursal;
	}

	public int getRegistrosEnCentral() {
		return registrosEnCentral;
	}

	public void setRegistrosEnCentral(int registrosEnCentral) {
		this.registrosEnCentral = registrosEnCentral;
	}

	public double getControl_1_sucursal() {
		return control_1_sucursal;
	}

	public void setControl_1_sucursal(double conrol_1_sucursal) {
		this.control_1_sucursal = conrol_1_sucursal;
	}

	public double getControl_1_central() {
		return control_1_central;
	}

	public void setControl_1_central(double control_1_central) {
		this.control_1_central = control_1_central;
	}

	public String getControl_1_desc() {
		return control_1_desc;
	}

	public void setControl_1_desc(String control_1_desc) {
		this.control_1_desc = control_1_desc;
	}

	public double getControl_2_sucursal() {
		return control_2_sucursal;
	}

	public void setControl_2_sucursal(double control_2_sucursal) {
		this.control_2_sucursal = control_2_sucursal;
	}

	public double getControl_2_central() {
		return control_2_central;
	}

	public void setControl_2_central(double control_2_central) {
		this.control_2_central = control_2_central;
	}

	public String getControl_2_desc() {
		return control_2_desc;
	}

	public void setControl_2_desc(String control_2_desc) {
		this.control_2_desc = control_2_desc;
	}

	public double getControl_3_sucursal() {
		return control_3_sucursal;
	}

	public void setControl_3_sucursal(double control_3_sucursal) {
		this.control_3_sucursal = control_3_sucursal;
	}

	public double getControl_3_central() {
		return control_3_central;
	}

	public void setControl_3_central(double control_3_central) {
		this.control_3_central = control_3_central;
	}

	public String getControl_3_desc() {
		return control_3_desc;
	}

	public void setControl_3_desc(String control_3_desc) {
		this.control_3_desc = control_3_desc;
	}

	public Date getActualizacion() {
		return actualizacion;
	}

	public void setActualizacion(Date actualizacion) {
		this.actualizacion = actualizacion;
	}

	public Long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((concepto == null) ? 0 : concepto.hashCode());
		result = prime * result + ((entidad == null) ? 0 : entidad.hashCode());
		result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
		result = prime * result + ((tabla == null) ? 0 : tabla.hashCode());
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
		Validacion other = (Validacion) obj;
		if (concepto == null) {
			if (other.concepto != null)
				return false;
		} else if (!concepto.equals(other.concepto))
			return false;
		if (entidad == null) {
			if (other.entidad != null)
				return false;
		} else if (!entidad.equals(other.entidad))
			return false;
		if (fecha == null) {
			if (other.fecha != null)
				return false;
		} else if (!fecha.equals(other.fecha))
			return false;
		if (tabla == null) {
			if (other.tabla != null)
				return false;
		} else if (!tabla.equals(other.tabla))
			return false;
		return true;
	}

	
	

}
