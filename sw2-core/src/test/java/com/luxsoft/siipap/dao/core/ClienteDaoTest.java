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

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Contacto;
import com.luxsoft.siipap.model.core.TipoDeCliente;

/**
 *
 * @author Ruben Cancino
 */
@SuppressWarnings("unchecked")
public class ClienteDaoTest extends BaseDaoTestCase{
    
    private ClienteDao clienteDao;

    public void setClienteDao(ClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }
    
    
	public void testAddRemove(){
        //List<TipoDeCliente> tipos=universalDao.getAll(TipoDeCliente.class);
        //assertFalse(tipos.isEmpty());
        
        Cliente c=new Cliente("CLIE_TEST1");
        //c.setTipo(tipos.get(0));
        c=clienteDao.save(c);
        assertNotNull(c.getId());
        flush();
                
        //Buscar Por clave
        c=clienteDao.buscarPorClave("CLIE_TEST1");
        assertNotNull(c);
        
        clienteDao.remove(c.getId());
        flush();
        
        try {
            c=clienteDao.get(c.getId());
            fail("No debe encontrar el cliente: "+c);
        } catch (ObjectRetrievalFailureException ex) {
            assertNotNull(ex);
        }

    }
    
    public void testAddRemoveDirecciones(){
       List<TipoDeCliente> tipos=universalDao.getAll(TipoDeCliente.class);
        assertFalse(tipos.isEmpty());
        String clave="CLIE_TEST1";
        Cliente c=new Cliente(clave);
        c.setTipo(tipos.get(0));
        
        Direccion d=new Direccion();
        d.setCalle("CALLE");
        d.setColonia("COLONIA");        
        c.agregarDireccion(d);
        
        Direccion d2=new Direccion();
        d2.setCalle("CALLE");
        d2.setColonia("COLONIA");        
        c.setDireccionFiscal(d2);
        
        c=clienteDao.save(c);
        
        assertNotNull(c.getId());
        flush();
        
        //Buscar Por clave
        c=clienteDao.buscarPorClave(clave);
        assertNotNull(c);
        assertEquals(1, c.getDirecciones().size());
        assertEquals("CALLE",c.getDireccionFiscal().getCalle());
        
    }
    
    public void testAddBunch(){
         List<TipoDeCliente> tipos=universalDao.getAll(TipoDeCliente.class);
        assertFalse(tipos.isEmpty());
        for(int i=1;i<=10;i++){
            String clave="CLIE_TEST_"+i;
            Cliente c=new Cliente(clave);
            c.setTipo(tipos.get(0));        
            Direccion d=new Direccion();
            d.setCalle("CALLE");
            d.setColonia("COLONIA");
            c.agregarDireccion(d);
            c.agregarDireccion(d, "ENTREGHA1");
            c.agregarComentario("VENTAS", "COMENTARIO DE VENTAS");
            
            c.agregarTelefono("PRINCIPAL", "53793518");
            Contacto ct=new Contacto("TEST USER","DIRECTOR GENERAL");
            c.agregarContacto(ct);
            c.habilitarCredito();
            c=clienteDao.save(c);
            assertNotNull(c.getId());
        }
        setComplete();
    }

}
