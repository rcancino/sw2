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

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.core.TipoDeCliente;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 *
 * @author Ruben Cancino
 */
public class TipoDeClienteDaoTest extends BaseDaoTestCase{
    
    public void testAdddDefaultTipo(){
        TipoDeCliente tipo=new TipoDeCliente("GENERICO","CLIENTE GENERICO SIN CLASIFICACION");
        tipo=(TipoDeCliente)universalDao.save(tipo);
        flush();
        assertNotNull(tipo.getId());
        setComplete();
    }
    
    public void testAddRemove(){
        TipoDeCliente tipo=new TipoDeCliente("TIPO1","TEST");
        tipo=(TipoDeCliente)universalDao.save(tipo);
        flush();
        assertNotNull(tipo.getId());
        
        tipo=(TipoDeCliente)universalDao.get(TipoDeCliente.class, tipo.getId());
        assertNotNull(tipo);
        
        universalDao.remove(TipoDeCliente.class, tipo.getId());
        flush();
        try {
            tipo=(TipoDeCliente)universalDao.get(TipoDeCliente.class, tipo.getId());
            fail("No debe existir el tipo");
        } catch (ObjectRetrievalFailureException ex) {
            assertNotNull(ex);
        }
    }

}
