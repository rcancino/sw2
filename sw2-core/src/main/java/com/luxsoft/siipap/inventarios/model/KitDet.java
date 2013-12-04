package com.luxsoft.siipap.inventarios.model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Detalle de transformacion
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_INVENTARIO_KIT")
public class KitDet extends Inventario{
	
	
	
	@Override
	public String getTipoDocto() {
		return "KIT";
	}
	

}
