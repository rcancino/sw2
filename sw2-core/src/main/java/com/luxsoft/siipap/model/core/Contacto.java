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

import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;

/**
 * Abstraccion de lo que es un contacto. Es decir una persona
 * que puede representar una organización. 
 * 
 * @author Ruben Cancino
 */
@Embeddable
public class Contacto extends BaseBean{
    
    @Column(length=50,nullable=false)
    @NotNull
    private String nombre;
    
    @Column(length=50,nullable=false)
    private String telefono="";
    
    @Column(length=50,nullable=true)
    private String email1="";
    
    @Column(length=50,nullable=false)
    private String puesto="";

    public Contacto() {
    }

    public Contacto(String nombre, String puesto) {
        this.nombre = nombre;
        this.puesto = puesto;
        setTelefono("");
        setEmail1("");
    }
    

    public String getEmail1() {
        return email1;
    }

    public void setEmail1(String email1) {
    	Object old=this.email1;
        this.email1 = email1;
        firePropertyChange("email1", old, email1);
    }    

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
    	Object old=this.nombre;
        this.nombre = nombre;
        firePropertyChange("nombre", old, nombre);
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
    	Object old=this.puesto;
        this.puesto = puesto;
        firePropertyChange("puesto", old, puesto);
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
    	Object old=this.telefono;
        this.telefono = telefono;
        firePropertyChange("telefono", old, telefono);
    }

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((nombre == null) ? 0 : nombre.hashCode());
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
		final Contacto other = (Contacto) obj;
		if (nombre == null) {
			if (other.nombre != null)
				return false;
		} else if (!nombre.equals(other.nombre))
			return false;
		return true;
	}

    public String toString(){
    	return getNombre();
    }
    
    

}
