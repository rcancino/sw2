package com.luxsoft.siipap.model;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Currency;

import junit.framework.TestCase;

public class CantidadMonetariaEditorTest extends TestCase{
	
	public void testAutomaticRegistration(){
		PropertyEditor editor=PropertyEditorManager.findEditor(CantidadMonetaria.class);
		assertNotNull("Debe existir un editor para cantidad monetaria",editor);
	}
	
	public void testSetValue(){
		PropertyEditor editor=PropertyEditorManager.findEditor(CantidadMonetaria.class);
		editor.setAsText("250.35:MXN");
		assertTrue(editor.getValue().getClass()==CantidadMonetaria.class);
		System.out.println("Value: "+editor.getAsText());
		CantidadMonetaria res=(CantidadMonetaria)editor.getValue();
		assertEquals(Currency.getInstance("MXN"), res.currency);
		
		editor.setAsText("250.35");
		assertTrue(editor.getValue().getClass()==CantidadMonetaria.class);
		System.out.println("Value: "+editor.getAsText());
		res=(CantidadMonetaria)editor.getValue();
		assertEquals(
				Currency.getInstance(CantidadMonetariaEditor.DEFAULT_MONETARY_CURRENCY)
				, res.currency);
		
		editor.setAsText("250.35:USD");
		assertTrue(editor.getValue().getClass()==CantidadMonetaria.class);
		System.out.println("Value: "+editor.getAsText());
		res=(CantidadMonetaria)editor.getValue();
		assertEquals(Currency.getInstance("USD"), res.currency);
		
		editor.setAsText("250.35:EUR");
		assertTrue(editor.getValue().getClass()==CantidadMonetaria.class);
		System.out.println("Value: "+editor.getAsText());
		res=(CantidadMonetaria)editor.getValue();
		assertEquals(Currency.getInstance("EUR"), res.currency);
		
		try {
			editor.setAsText(" ");
			fail("Debe mandar error por que El formato es nulo o la moneda no es localizable");
		} catch (IllegalArgumentException ie) {
			assertNotNull(ie);
		}
		try {
			editor.setAsText("45df ");
			fail("Debe mandar error por que el monto no es numerico");
		} catch (NumberFormatException ie) {
			assertNotNull(ie);
		}
		
	}

}
