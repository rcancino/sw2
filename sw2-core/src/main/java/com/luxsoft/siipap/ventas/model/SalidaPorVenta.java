package com.luxsoft.siipap.ventas.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.luxsoft.siipap.inventarios.model.Inventario;

/**
 * Salida de inventario originado por una venta
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_INVENTARIO_FAC",
		uniqueConstraints=@UniqueConstraint(
				columnNames={"SUCURSAL_ID","DOCUMENTO","SER","RENGLON"}))
public class SalidaPorVenta extends Inventario{
	
	@Column(name="VENTADET_ID")
	private Long ventaDet;
	
	@Column(name="SER",length=1)
	private String serie;
	
	@Column(name="TIP",length=1)
	private String tipo;
	
	

	public Long getVentaDet() {
		return ventaDet;
	}

	public void setVentaDet(Long ventaDet) {
		this.ventaDet = ventaDet;
	}

	@Override
	public String getTipoDocto() {
		return "FAC";
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	

}
