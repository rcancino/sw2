package com.luxsoft.siipap.ventas.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.NotNull;



import com.luxsoft.siipap.cxc.model.Cargo;

/**
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_ANTICIPOS_APLICADOS")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class AplicacionDeAnticiposFacturados {
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="APLICACION_ID")
	protected String id;
	
	@Version
	private int version;
	
	@ManyToOne(optional = false,
			fetch=FetchType.EAGER)			
	@JoinColumn(name = "ORIGEN_ID", nullable = false, updatable = false)
	private Venta anticipo;
	
	@ManyToOne(optional = false,
			fetch=FetchType.EAGER)			
	@JoinColumn(name = "CARGO_ID", nullable = false, updatable = false)
	private Cargo cargo;
	
	@Column (name="APLICADO",nullable=false,scale=2,precision=16)
	@NotNull
	private BigDecimal aplicadoImporte;
	
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Venta getAnticipo() {
		return anticipo;
	}

	public void setAnticipo(Venta anticipo) {
		this.anticipo = anticipo;
	}

	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	public BigDecimal getAplicadoImporte() {
		return aplicadoImporte;
	}

	public void setAplicadoImporte(BigDecimal aplicadoImporte) {
		this.aplicadoImporte = aplicadoImporte;
	}

	

	public int getVersion() {
		return version;
	}
	
	

}
