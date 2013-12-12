package com.luxsoft.cfdi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;

import com.luxsoft.sw3.cfdi.CFDIUtils;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.services.Services;

public class PruebasCFDI {
	
	
	public static void test1() throws IOException{
		CFDI cfdi=Services.getCFDIManager().getCFDI("8a8a8161-42c4f4e5-0142-c4f6a272-0005");
		File xml=new File("c:\\basura\\facturaCfdi3.xml");
		
		XmlOptions options = new XmlOptions();
		options.setCharacterEncoding("UTF-8");
        options.put( XmlOptions.SAVE_INNER );
        options.put( XmlOptions.SAVE_PRETTY_PRINT );
        options.put( XmlOptions.SAVE_AGGRESSIVE_NAMESPACES );
        options.put( XmlOptions.SAVE_USE_DEFAULT_NAMESPACE );
        options.put(XmlOptions.SAVE_NAMESPACES_FIRST);
		Map suggestedPrefix=new HashMap();
		suggestedPrefix.put("", "");
		
		cfdi.getComprobanteDocument().save(xml,options);
		CFDIUtils.validarDocumento(cfdi.getComprobanteDocument());
		System.out.println("XML: "+xml.getAbsolutePath());
	}
	
	
	public static void main(String[] args) throws IOException {
		CFDI cfdi=Services.getCFDIManager().getCFDI("402881c6-42e05111-0142-e0580e1c-0009");
		cfdi.salvarArchivoTimbradoXml();
	}

}
