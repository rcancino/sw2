package com.luxsoft.siipap.sanbox;

import org.springframework.beans.BeanWrapperImpl;

import com.luxsoft.siipap.compras.model.Compra;

import junit.framework.TestCase;

/**
 * Pruebas para analizar ideas sobre un nuevo Wrapper que pueda soportar
 * PropertyChangeEvents
 * 
 * @author Ruben Cancino
 *
 */
public class BeanWrapperTest extends TestCase{
	
	
	public void testSimpleCambio(){
		Compra compra=new Compra();
		BeanWrapperImpl wrapper=new BeanWrapperImpl(compra);
		// Asignamos valores mediante el wrapper
		Object val="COMENTARIO";
		wrapper.setPropertyValue("comentario", val);
		
		assertEquals(val, compra.getComentario());
		boolean bound=wrapper.getPropertyDescriptor("comentario").isBound();
		
		assertFalse(bound);
	}

}
