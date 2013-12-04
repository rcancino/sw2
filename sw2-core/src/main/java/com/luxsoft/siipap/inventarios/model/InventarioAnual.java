package com.luxsoft.siipap.inventarios.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;



/**
 * 
 * Inventario anual 
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_INVENTARIO_INI"
	,uniqueConstraints=@UniqueConstraint(
			columnNames={"PRODUCTO_ID","SUCURSAL_ID","YEAR","MES"})
	)
public class InventarioAnual extends Inventario{
	
	@Column(name="YEAR",nullable=false)
	private int year;
	
	@Column(name="MES",nullable=false)
	private int mes;

	@Override
	public String getTipoDocto() {
		return "INI";
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
	
	

}
