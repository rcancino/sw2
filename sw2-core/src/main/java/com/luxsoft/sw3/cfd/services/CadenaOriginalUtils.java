package com.luxsoft.sw3.cfd.services;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;

import mx.gob.sat.cfd.x2.ComprobanteDocument;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.xmlbeans.XmlException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.Assert;

public class CadenaOriginalUtils {
	
	private static Transformer transformer;
	
	
	public static String cadenaOriginal(ComprobanteDocument document){
		
		try {
			if(transformer==null){
				TransformerFactory transformerFactory=TransformerFactory.newInstance();
				String xslPath=System.getProperty("cfd.xslt.path","z:/CFD/xslt/v2.2/cadenaoriginal_2_2.xslt");
				FileSystemResource xsltResource=new FileSystemResource(xslPath);
				Assert.isTrue(xsltResource.exists(),"No se localizan los archivos XSLT para la cadena original Llave: cfd.xslt.path");
				StreamSource xslt= new StreamSource(xsltResource.getInputStream());
				transformer=transformerFactory.newTransformer(xslt);
				
			}
			
			StringReader reader = new StringReader(document.xmlText());
			StringWriter writer = new StringWriter();
			transformer.transform(
					new javax.xml.transform.stream.StreamSource(reader)
					,new javax.xml.transform.stream.StreamResult(writer)
					);
			
			String result = writer.toString();
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Error generando la cadena original: "+ExceptionUtils.getRootCauseMessage(e));
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		
		//CFD XML de prueba
		String path="c:/CFD/xml/3MOS0057035.xml";
		FileSystemResource resource=new FileSystemResource(path);
		if(resource.exists()){
			System.out.println("OK: "+resource.getPath());
		}else{
			System.out.println("No existe el recurso: "+path);
		}
		ComprobanteDocument document=ComprobanteDocument.Factory.parse(resource.getInputStream());
		System.out.println(document.xmlText());
		
		//XSLT
		
		System.out.println(cadenaOriginal(document));
		
	}

}
