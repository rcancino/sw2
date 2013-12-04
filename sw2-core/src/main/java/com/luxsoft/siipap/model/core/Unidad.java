/*
 *  Copyright 2008 RUBEN.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package com.luxsoft.siipap.model.core;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.Length;

/**
 * Catalog de unidades para los productos
 * 
 * @author Ruben Cancino
 */
@Entity
@Table(name="SX_UNIDADES")
public class Unidad  implements Serializable{
		
	
	@Id
	@Column(name="UNIDAD",length=3,nullable=false)
	@Length(max=3,min=2)
	private String unidad;
	
	@Column(name="DESCRIPCION",length=50,nullable=false)
	private String nombre;
	
	@Column(name="FACTOR",nullable=false)
	private double factor;
	
	public Unidad(){
	}

	public Unidad(String unidad,double factor ) {		
		this.unidad = unidad;
		this.nombre=unidad;
		this.factor = factor;
	}

	public Unidad(String unidad,String descripcion, double factor ) {
		this.unidad = unidad;
		this.nombre = descripcion;
		this.factor = factor;
		
	}

	public String getUnidad() {
		return unidad;
	}

	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * El factor para obtener las piezas por unidad. Normalmente
	 * la unidad base es la pieza que implica un elemento, pero an algunos
	 * procesos, por simplificacion, se permite multiplos, com es el caso 
	 * de los productos que normalmente tiene precios por millar pero se venden
	 * por pieza es decir por hoja. 
	 * 
	 * @return
	 */
	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}
	
	/** Factory methos para la creacion de unidades soportadas **/
	
	public static Unidad getPieza(){
		return new Unidad("PZA","PIEZA",1);
	}
	
	public static Unidad getMillar(){
		return new Unidad("MIL","MILLAR",1000);
	}
	
	public static Unidad getKilo(){
		return new Unidad("KGR","KILOS",1);
	}
	
	public static Unidad getMetros2(){
		return new Unidad("MT2","METROS CUADRADOS",1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((unidad == null) ? 0 : unidad.hashCode());
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
		Unidad other = (Unidad) obj;
		if (unidad == null) {
			if (other.unidad != null)
				return false;
		} else if (!unidad.equals(other.unidad))
			return false;
		return true;
	}
	
	public String toString(){
		return getNombre();
	}
    
	/*
    MIL,    
    PZA,
    BOL,
    CJA,
    MTS,
    KGS,
    TON
      */
	
	
}
