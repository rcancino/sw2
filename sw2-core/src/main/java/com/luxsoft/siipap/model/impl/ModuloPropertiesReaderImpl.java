package com.luxsoft.siipap.model.impl;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.luxsoft.siipap.model.Modulo;
import com.luxsoft.siipap.model.ModuloPropertiesReader;

/**
 * Implementacion por defecto de {@link ModuloPropertiesReader}
 * utiliza el bean de {@link Configuracion} como base para definir el path al 
 * archivo de propiedades bajo el nombre de modulo.properties
 * 
 * En otras palabras el archivo de propiedades debe estar en el mismo nivel que la
 * clase de configuracion del modulo 
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class ModuloPropertiesReaderImpl implements ModuloPropertiesReader{
	
	private Modulo modulo;
	private ResourceLoader resourceLoader=new DefaultResourceLoader();
	public static final String PROPERTIES_FILE_NAME="modulo.properties";
	
	public ModuloPropertiesReaderImpl(){
	}

	public Properties readProperties() {
		Assert.isTrue(modulo!=null);
		Resource r= resourceLoader.getResource(resolvePath());
		Properties props=new Properties();
		try {
			props.load(r.getInputStream());
			return props;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setModulo(Modulo modulo) {		
		this.modulo=modulo;
	}
	
	/**
	 * Resuelve el path
	 *  
	 *  com.luxsoft.siipap.model.ConfiurationClass 
	 *  a com/luxsoft/siipap/model/configurationClas.properties
	 *  
	 * @return
	 */
	private String resolvePath(){		
		Assert.isTrue( (modulo.getPackageName()!=null)
				,"Debe existir la configuracion el nombre del paquete para el modulo");
		return ClassUtils.convertClassNameToResourcePath(modulo.getPackageName())+"/"+PROPERTIES_FILE_NAME;
	}

}
