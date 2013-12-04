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

package com.luxsoft.siipap.dao.core;

import java.util.List;
import java.util.Set;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.ClienteRow;

/**
 *
 * @author Ruben Cancino
 */
public interface ClienteDao extends GenericDao<Cliente,Long>{
    
    public Cliente buscarPorClave(final String clave);
    
    public void eliminarCredito(final String clave);
    
    /**
     * Regresa los clientes en una lista adecuada para las GUI 
     * 
     * @return
     */
    public void buscarClientes(final List<ClienteRow> clientes);
    
    public List<Cliente> buscarClientePorClave(final String clave);
    
    public List<Cliente> buscarClientePorNombre(final String clave);
    
    public List<Cliente> buscarClientesCredito();

    /**
     * Regresa una lista de las cuentas registradas para el cliente
     * 
     * @param clave
     * @return
     */
    public Set<String> buscarCuentasRegistradas(final String clave);
    
   
    
}
