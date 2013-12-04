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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.compras.dao.TestUtils;
import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.core.Clase;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.model.core.Marca;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Unidad;

/**
 * 
 * @author Ruben Cancino
 */
@SuppressWarnings("unchecked")
public class ProductoDaoTest extends BaseDaoTestCase {

	private ProductoDao productoDao;
	private Unidad unidad;
	
		

	@Override
	protected void onSetUp() throws Exception {		
		super.onSetUp();
		unidad=(Unidad)universalDao.get(Unidad.class, "MIL");
		assertNotNull(unidad);
	}

	public void setProductoDao(ProductoDao productoDao) {
		this.productoDao = productoDao;
	}

	public void testAddRemove() throws Exception {
		Producto prod = new Producto();
		
		prod.setClave("TESTPROD1");
		prod.setDescripcion("Producto de prueba");
		prod.setUnidad(unidad);
		
		final int x=RandomUtils.nextInt(528666665);

		final Linea l = new Linea("LIN_"+x,"Linea de prueba autogenerada");
		final Marca m = new Marca("MARC_"+x);
		final Clase c = new Clase("CL_"+x);
		

		prod.setLinea(l);
		prod.setMarca(m);
		prod.setClase(c);

		prod = productoDao.save(prod);
		flush();
		assertNotNull(prod.getId());
		
		
		// Buscar por clave
		prod = productoDao.buscarPorClave("TESTPROD1");
		assertNotNull(prod);
		
		
		// Eliminar
		productoDao.remove(prod.getId());
		flush();
		try {
			prod = productoDao.get(prod.getId());
			fail("Encontro el producto TESTPROD1");
		} catch (ObjectRetrievalFailureException ex) {
			assertNotNull(ex);
		}
		
		
	}


	public void testAddSeveralProducts() {

		final List<Linea> lineas = universalDao.getAll(Linea.class);
		assertFalse("Deben existir lineas de prueba", lineas.isEmpty());
		final Linea l = lineas.get(0);

		final List<Marca> marcas = universalDao.getAll(Marca.class);
		assertFalse("Deben existir Marcas de prueba", marcas.isEmpty());
		final Marca m = marcas.get(0);

		final List<Clase> clases = universalDao.getAll(Clase.class);
		assertFalse("Deben existir clases de prueba", clases.isEmpty());
		final Clase c = clases.get(0);
		
		List<Producto> prods=new ArrayList<Producto>(TestUtils.generarProductosDePrueba(20, unidad));
		for(Producto prod:prods){
			prod.setUnidad(unidad);
			prod.setLinea(l);
			prod.setMarca(m);
			prod.setClase(c);
			prod=productoDao.save(prod);
			flush();
			assertNotNull(prod.getId());
		}
		setComplete();
	}
	
	
	public void testAddCodigoDeBarras() {
		
		List<Producto> productos=productoDao.getAll();
		assertFalse("Debe existir al menos un producto de prueba",productos.isEmpty());
		Producto p = productos.get(0);
		
		
		int original = p.getCodigos().size();
		p.agregarCodigo("EAN1", 1);
		p.agregarCodigo("EAN2", 2);

		// Agregar
		productoDao.save(p);
		flush();

		p = productoDao.get(p.getId());
		

		// Eliminar
		p.eliminarCodigo("EAN1");
		p.eliminarCodigo("EAN2");
		productoDao.save(p);
		flush();

		p = productoDao.get(p.getId());
		System.out.println("Codigos registrados: " + p.getCodigos());
		assertEquals(original, p.getCodigos().size());

	}

	public void testAddDeleteDescuentos() {
		
		List<Producto> productos=productoDao.getAll();
		assertFalse("Debe existir al menos un producto de prueba",productos.isEmpty());
		Producto p = productos.get(0);
		
		int original = p.getDescuentos().size();

		System.out.println("Descuentos originales: " + original);

		// Agregamos al final de los descuentos ya existentes dos nuevos
		// descuentos
		int d1 = p.agregarDescuento(.2, "TEST").getOrden();
		int d2 = p.agregarDescuento(.1, "TEST").getOrden();

		// Agregar
		p = productoDao.save(p);
		flush();

		p = productoDao.get(p.getId());
		System.out.println("Descuentos: " + p.getDescuentos().size());
		assertEquals(original + 2, p.getDescuentos().size());

		// Eliminar
		p.eliminarDescuento(d1);
		p.eliminarDescuento(d2);
		productoDao.save(p);
		flush();

		p = productoDao.get(p.getId());
		System.out.println("Descuentos registrados: " + p.getDescuentos());
		assertEquals(original, p.getDescuentos().size());

	}
	
}
