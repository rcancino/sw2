package com.luxsoft.sw3.cfd.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import mx.gob.sat.cfd.x2.ComprobanteDocument;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import com.luxsoft.sw3.cfd.CFDUtils;

import junit.framework.TestCase;

public class CadenaOriginalBuilderImplTest extends TestCase {
	
	
	String expected;
	/*
	@Override
	protected void setUp() throws Exception {		
		super.setUp();
		ClassPathResource resource=new ClassPathResource("cfd/CadenaOriginalDePrueba_4.txt");
		assertTrue(resource.exists());
		BufferedReader r=new BufferedReader(new InputStreamReader(resource.getInputStream()));
		expected="";	
		String line;
		while ((line = r.readLine())!=null){
			if(line!=null){
				expected+=line;
			}		
		}	
		r.close();
		System.out.println(expected);
	}*/

	public void testValidarGeneracion() throws Exception{
		//String cadenaOriginal="||A|1|2005-09-02T16:30:00|1|ISP900909Q88|Industrias del Sur Poniente, S.A. de C.V.|Alvaro Obregón|37|3|Col. Roma Norte|México|Cuauhtémoc|Distrito Federal|México|06700|Pino Suarez|23|Centro|Monterrey|Monterrey|Nuevo Léon|México|95460|CAUR390312S87|Rosa María Calderón Uriegas|Topochico|52|Jardines del Valle|Monterrey|Monterrey|Nuevo León|México|95465|10|Caja|Vasos decorados|20|200|1|pieza|Charola metálica|150|150|IVA|52.5||";
		String cadenaOriginal="||2.0|ABCD|2|03-05-2010T14:11:36|49|2008|INGRESO|UNA SOLA EXHIBICIÓN|2000.00|00.00|2320.00|PAMC660606ER9|CONTRIBUYENTE PRUEBASEIS PATERNOSEIS MATERNOSEIS|PRUEBA SEIS|6|6|PUEBLA CENTRO|PUEBLA|PUEBLA|PUEBLA||MÉXICO|72000|CAUR390312S87|ROSA MARÍA CÁLDERON URIEGAS|TOPOCHICO|52|JARDINES DEL VALLE|NUEVO LEÓN|MEXICO|95465|1.00|SERVICIO|01|ASESORIA FISCAL Y ADMINISTRATIVA|2000.00|IVA|16.00|320.00||";
		                        // ||2.0|ABCD|2|03-05-2010T14:11:36|49|2008|INGRESO|UNA SOLA EXHIBICIÓN|2000.00|00.00|2320.00|PAMC660606ER9|CONTRIBUYENTE PRUEBASEIS PATERNOSEIS MATERNOSEIS|PRUEBA SEIS|6|6|PUEBLA CENTRO|PUEBLA|PUEBLA|PUEBLA||MÉXICO|72000|CAUR390312S87|ROSA MARÍA CÁLDERON URIEGAS|TOPOCHICO|52|JARDINES DEL VALLE|NUEVO LEÓN|MEXICO|95465|1.00|SERVICIO|01|ASESORIA FISCAL Y ADMINISTRATIVA|2000.00|IVA|16.00|320.00|| 
		/*FileOutputStream os=new FileOutputStream(new File("c:\\cadena.txt"));
		os.write(cadenaOriginal.getBytes("UTF-8"));
		os.flush();
		os.close();*/
		//Digest digest=new MD5Digest();
		MessageDigest digest=MessageDigest.getInstance("MD5");
		byte[] cadenaCifrada=digest.digest(cadenaOriginal.getBytes("UTF-8"));
		//byte[] cadenaEncoded=cadenaOriginal.getBytes();//this.expected.getBytes();
		//digest.update(cadenaEncoded,0,cadenaEncoded.length);
		//digest.doFinal(cadenaCifrada, 0);
		System.out.println("Cadena cifrada: "+new String(cadenaCifrada));
		System.out.println("Cadena cifrada HEX: "+new String(Hex.encode(cadenaCifrada)));
		
		
		//Cargamos el comprobante de prueba
		
		//ClassPathResource resource=new ClassPathResource("cfd/Muestra.xml");
		//FileSystemResource resource=new FileSystemResource("c:\\pruebas\\cfd\\pruebas2\\Muestra.xml");
		FileInputStream io=new FileInputStream("C:\\pruebas\\cfd\\prueba2\\Muestra.xml");
		
		//InputStreamReader reader=new InputStreamReader(resource.getInputStream());
		ComprobanteDocument document=ComprobanteDocument.Factory.parse(io);
		List errs=CFDUtils.validar(document);
		assertTrue("El documento tiene errores",errs.isEmpty());
		//System.out.println(document);
		
		CadenaOriginalBuilderImpl builder=new CadenaOriginalBuilderImpl();
		String res=builder.obtenerCadena(document);
		System.out.println("In : "+cadenaOriginal);
		System.out.println("Out: "+res);
		
		cadenaCifrada=digest.digest(res.getBytes("UTF-8"));
		System.out.println("Cadena cifrada HEX: "+new String(Hex.encode(cadenaCifrada)));
		/*
		MessageDigest digest=MessageDigest.getInstance("MD5");
		byte[] resDigest=digest.digest(res.getBytes());
		byte[] expectedDigest=digest.digest(expected.getBytes("UTF-8"));
		System.out.println("Out MD5:"+new String(Base64.encode(resDigest)));
		System.out.println("Out MD5:"+new String(Base64.encode(expectedDigest)));
		*/
	}
	
	

}
