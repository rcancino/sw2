/*
 *  Copyright 2008 Ruben Cancino Ramos.
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

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.validator.Length;

import com.luxsoft.siipap.model.BaseBean;

/**
 *  Abstraccio de lo que es un descuento en el sistema
 * 
 * @author Ruben Cancino
 */
@Embeddable
public class Descuento extends BaseBean implements Comparable<Descuento>{
    
    private int orden=0;
    
    /**
     * Valor del descuento. Por estandar es de 4 pociciones
     * decimales
     */
    @Column(name="DESCUENTO",nullable=false)
    private double descuento=0;
    
    @Column(name="DESCRIPCION",length=100,nullable=false)
    @Length(max=255)
    private String descripcion="";
    
    @Column(name="ACTIVO",nullable=false)
    private boolean activo=true;
    
    

    public Descuento() {
    }


    /**
     * Crea un descuento de la magnitud indicada
     * y la descripción inidcada
     * 
     * @param valor El importe del descuento decimal de maximo 6 posiciones
     */
    public Descuento(final double valor,final String desc) {
        setDescuento(valor);
        this.descripcion=desc;
    }

    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
    	Object old=this.descripcion;
        this.descripcion = descripcion;
        firePropertyChange("descripcion", old, descripcion);
    }
    
    
   


	public double getDescuento() {
		return descuento;
	}


	public void setDescuento(double descuento) {
		this.descuento = descuento;
	}


	public boolean isActivo() {
        return activo;
    }
    public void setActivo(boolean activo) {
    	boolean old=this.activo;
        this.activo = activo;
        firePropertyChange("activo", old, activo);
    }

    public int getOrden() {
        return orden;
    }
    public void setOrden(int orden) {
    	int old=this.orden;
        this.orden = orden;
        firePropertyChange("orden", old, orden);
    }

    
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = prime;
		result = prime * result
				+ ((descripcion == null) ? 0 : descripcion.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj==null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Descuento other = (Descuento) obj;
		if (descripcion == null) {
			if (other.descripcion != null)
				return false;
		} else if (!descripcion.equals(other.descripcion))
			return false;
		return true;
	}

    
    
    @Override
    public String toString(){
        return descripcion+": "+descuento;
    }

    public int compareTo(Descuento o) {
        Integer d1=getOrden();
        Integer d2=o.getOrden();
        return d1.compareTo(d2);
    }

}
