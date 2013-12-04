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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author Ruben Cancino
 */
public class ProductoTest {

    public ProductoTest() {
    }
    
    /**
     * Test of agregarCodigo method, of class Producto.
     */
    @Test
    public void testAgregarCodigo() {
        System.out.println("agregarCodigo");        
        Producto instance = new Producto();
        instance.agregarCodigo("EAN2", 2);
        instance.agregarCodigo("EAN3", 2);
        instance.agregarCodigo("EAN4", 2);
        
        assertEquals(3, instance.getCodigos().size());
    }

    /**
     * Test of eliminarCodigo method, of class Producto.
     */
    @Test
    public void testEliminarCodigo() {
        System.out.println("eliminarCodigo");
        Producto instance = new Producto();
        instance.agregarCodigo("EAN2", 2);
        instance.agregarCodigo("EAN3", 2);
        instance.agregarCodigo("EAN4", 2);
        
        assertEquals(3, instance.getCodigos().size());
        
        instance.eliminarCodigo("EAN2");
        instance.eliminarCodigo("EAN3");
        instance.eliminarCodigo("EAN4");
        
        assertEquals(0, instance.getCodigos().size());
    }

}