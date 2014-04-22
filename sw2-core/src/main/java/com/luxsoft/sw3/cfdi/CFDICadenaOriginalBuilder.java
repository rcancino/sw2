package com.luxsoft.sw3.cfdi;

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;



import mx.gob.sat.cfd.x3.ComprobanteDocument;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("cfdiCadenaBuilder")
public class CFDICadenaOriginalBuilder {
	
	

	public String generarCadena(ComprobanteDocument document){
		try {
			
			String xslPath=System.getProperty("cfd.xslt.path");
			FileSystemResource xslt=new FileSystemResource(xslPath);
			System.out.println("Ruta de XSLT: "+xslt.getPath());
			
			
			/*Resource xslt=new ClassPathResource("sat/cadenaoriginal_3_2.xslt");
			Assert.isTrue(xslt.exists(),"No existe el xslt");*/
			
			TransformerFactory factory=TransformerFactory.newInstance();
			StreamSource source=new StreamSource(xslt.getInputStream());
			Transformer transformer=factory.newTransformer(source);
			
			Writer writer=new StringWriter();
			StreamResult out=new StreamResult(writer);
			Source in=new DOMSource(document.getDomNode());
			transformer.transform(in, out);
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	

}
