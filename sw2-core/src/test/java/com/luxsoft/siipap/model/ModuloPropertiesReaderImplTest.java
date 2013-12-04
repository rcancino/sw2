package com.luxsoft.siipap.model;

import java.util.Properties;

import com.luxsoft.siipap.model.impl.ModuloPropertiesReaderImpl;

import junit.framework.TestCase;

/**
 * Prueba el funcionamiento adecuado de la implementacion
 * de {@link ModuloPropertiesReader}
 * 
 * @author Ruben Cancino
 *
 */
public class ModuloPropertiesReaderImplTest extends TestCase{
	
	private ModuloPropertiesReader reader;
	private Modulo m;
	
	public void setUp(){
		reader=new ModuloPropertiesReaderImpl();
		m=new Modulo("ModuloTest","Test Modulo");
		m.setPackageName("com.luxsoft.siipap.model");
		reader.setModulo(m);
	}
	
	public void testReadPropertiesFile(){
		Properties props=reader.readProperties();
		assertNotNull(props);
		String name=props.getProperty(ModuleBasicProerties.NOMBRE.name());
		assertEquals("Modulo de prueba", name);
	}

}
