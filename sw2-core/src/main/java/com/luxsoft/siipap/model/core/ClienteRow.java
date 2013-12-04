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


/**
 *
 * @author RUBEN
 */
public class ClienteRow {
    
    //public static final String PROP_CLAVE = "clave";
    //public static final String PROP_NOMBRE = "nombre";
    //public static final String PROP_CONTACTO = "contacto";
    
    private Long id;    

    private String clave;
    
    protected String nombre;
    
    private String contacto;
    
    private boolean credito=false;
    
    private boolean activo;

    public ClienteRow() {    }
    
    public ClienteRow(final Cliente c){
    	this.id=c.getId();
        this.clave = c.getClave();
        this.nombre = c.getNombreRazon();
        this.contacto = c.getContactoPrincipal();
        this.activo=!c.isSuspendido();
        this.credito=c.isDeCredito();
    }

    public ClienteRow(Long id,String clave, String nombre, String contacto) {
    	this.id=id;
        this.clave = clave;
        this.nombre = nombre;
        this.contacto = contacto;
    }
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        //String oldClave = clave;
        this.clave = clave;
        //propertyChangeSupport.firePropertyChange(PROP_CLAVE, oldClave, clave);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        //String oldNombre = nombre;
        this.nombre = nombre;
        //propertyChangeSupport.firePropertyChange(PROP_NOMBRE, oldNombre, nombre);
    }
    

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        //String oldContacto = contacto;
        this.contacto = contacto;
        //propertyChangeSupport.firePropertyChange(PROP_CONTACTO, oldContacto, contacto);
    }
    
    /*private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }*/
    
    

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		final ClienteRow other = (ClienteRow) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
    
    public String toString(){
    	return nombre;
    }

	public boolean isCredito() {
		return credito;
	}

	public void setCredito(boolean credito) {
		this.credito = credito;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

}
