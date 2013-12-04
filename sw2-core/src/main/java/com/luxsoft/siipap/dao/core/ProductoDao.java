/*
 *  Copyright 2008 Ruben Cancino.
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

package com.luxsoft.siipap.dao.core;

import java.util.List;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.core.Producto;


/**
 *
 * @author Ruben Cancino
 */
public interface ProductoDao extends GenericDao<Producto,Long>{
    
    public Producto buscarPorClave(final String clave);
    
    public List<Producto> buscarInventariablesActivos();
    
    public List<Producto> buscarPorLinea(final String nombre);
    
    public List<Producto> buscarActivos();
    
    public List<Producto> buscarProductosActivosYDeLinea();

}
