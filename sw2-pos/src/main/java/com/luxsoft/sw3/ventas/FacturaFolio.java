package com.luxsoft.sw3.ventas;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Folio automatico de las facturas
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_FOLIO_FACTURA")
public class FacturaFolio {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="FOLIO_FISCAL")
	private Long id;

	public Long getId() {
		return id;
	}
	
	

}
