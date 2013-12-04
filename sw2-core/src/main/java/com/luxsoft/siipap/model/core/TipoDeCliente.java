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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Clasificación estandar de clientes
 * 
 * @author Ruben Cancino
 */
@Entity
@Table(name="SX_TIPO_CLIENTE")
public class TipoDeCliente {
    
    @Id @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="TIPO_ID")
    private Long id;
    
    @Column(name="CLAVE",length=15,unique=true,nullable=false)
    private String clave;
    
    @Column(name="DESCRIPCION", length=100)
    private String descripcion;

    public TipoDeCliente() {
    }
    
    public TipoDeCliente(String clave, String descripcion) {
        this.clave = clave;
        this.descripcion = descripcion;
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

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if(obj==this) return true;
        final TipoDeCliente other = (TipoDeCliente) obj;
        return clave.equals(other.getClave());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
    
}
