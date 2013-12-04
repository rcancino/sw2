package com.luxsoft.sw3.cfd.services;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import mx.gob.sat.cfd.x2.ComprobanteDocument;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.luxsoft.utils.LoggerHelper;

/**
 * Genera cadea original para la version 2 del CFD
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CadenaOriginalBuilderV2Impl implements CadenaOriginalBuilder{
	
	private Logger logger=LoggerHelper.getLogger();
	
	
	
	public String obtenerCadena(ComprobanteDocument comprobante) throws RuntimeException{		
		
		String cadenaOriginal = "";
        try
        {
            String fileTemp= "~" + new Date().getTime();
            File file = new File(getTempDir(),fileTemp);            
            OutputStream out = new FileOutputStream(file);
            
          
            DOMSource source=new DOMSource(comprobante.getDomNode());
            getTransofrmer().transform(source, new StreamResult(out));
            
            InputStream in = new FileInputStream(new File(getTempDir(),fileTemp));
            InputStreamReader streamReader = new InputStreamReader(in,"UTF-8");
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            
            cadenaOriginal = bufferedReader.readLine();

            streamReader.close();
            in.close();
            out.close();
            
            File eliminarFileTemp = new File(getTempDir(),fileTemp);
            eliminarFileTemp.delete();
            
            if(cadenaOriginal.equals("|||")) cadenaOriginal = null;
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
            throw new RuntimeException(
            		"Se presentó un error al intentar generar la cadena original\n " +
            		"" + ExceptionUtils.getRootCauseMessage(ex),ex);
        }
        return cadenaOriginal;
	}

	public String obtenerCadena(Comprobante comprobante) {		
		
		try {
			//StringWriter out=new StringWriter();
			final ByteArrayOutputStream out=new ByteArrayOutputStream();
			final OutputStreamWriter writer=new OutputStreamWriter(out, "UTF-8");
			System.out.println(comprobante);
			Result res=new StreamResult(writer);
			DOMSource source=new DOMSource(comprobante.getDomNode());
			getTransofrmer().transform(source, res);
			String cadena=out.toString();
			System.out.println("Transformacion: "+cadena);
			return cadena;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new RuntimeException("Error leyendo XSLT para cadena original "+ExceptionUtils.getRootCauseMessage(e)
					,ExceptionUtils.getRootCause(e));
			
		}
	}
	
	private Transformer transformer;
	
	public Transformer getTransofrmer(){
		if(transformer==null){
			TransformerFactory transformerFactory=TransformerFactory.newInstance();
			DefaultResourceLoader loader=new DefaultResourceLoader();
			//Resource xsltResource=loader.getResource("classpath:META-INF/cfd_xlst_2.2/cadenaoriginal_2_2.xslt");
			//ClassPathResource xsltResource=new ClassPathResource("META-INF/cfd_xlst_2.2/cadenaoriginal_2_2.xslt");
			String xslPath=System.getProperty("cfd.xslt.path");
			FileSystemResource xsltResource=new FileSystemResource(xslPath);
			StreamSource xslt;
			try {
				xslt = new StreamSource(xsltResource.getInputStream());
				transformer=transformerFactory.newTransformer(xslt);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return transformer;
	}
	
	private DOMSource source;
	
	protected Source getSource(){
		if(source==null){
			ClassPathResource xslResource=new ClassPathResource("META-INF/sat/cadenaoriginal_2_0.xslt");
			final DocumentBuilderFactory documentFactory=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;			
			try {
				builder = documentFactory.newDocumentBuilder();
				Document docto= builder.parse(xslResource.getInputStream());
				source=new DOMSource(docto);
				return source;
			} catch (Exception e) {
				throw new RuntimeException("Error leyendo XSLT para cadena original "+ExceptionUtils.getRootCauseMessage(e)
						,ExceptionUtils.getRootCause(e));
				
			}
			
		}
		return source;
	}
	
	public String transformation(String _factura, String _xslt, String _id)throws Exception
    {
        String cadenaOriginal = "";
        try
        {
            String fileTemp;
            
            if(_id == null){
                fileTemp = "~" + new Date().getTime();
            }else{
                fileTemp = "~" + _id;
            }
            
            File file = new File("C:\\",fileTemp);
            
            OutputStream out = new FileOutputStream(file);
            
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(new StreamSource(_xslt));
            
            transformer.transform(new StreamSource(new FileInputStream(_factura)), new StreamResult(out));
            
            InputStream in = new FileInputStream(new File("C:\\",fileTemp));
            InputStreamReader streamReader = new InputStreamReader(in,"UTF-8");
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            
            cadenaOriginal = bufferedReader.readLine();

            streamReader.close();
            in.close();
            out.close();
            
            //File eliminarFileTemp = new File(fileTemp);
            //eliminarFileTemp.delete();
            
            if(cadenaOriginal.equals("|||")) cadenaOriginal = null;
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
            throw new Exception("Se presentó un error al intentar leer el archivo temporal para extraer la cadena original: " + ex.getMessage());
        }
        return cadenaOriginal;
    }

	
	private File tempDir;
	
	public File getTempDir(){
		if(tempDir==null){
			String path=System.getProperty("user.home");
			tempDir=new File(path+"/cfd");
			tempDir.mkdirs();
		}
		if(!tempDir.exists()){
			tempDir.mkdirs();
		}
		return tempDir;
		
	}
	
	
	
	public static void main(String[] args) throws Exception{
		//Leemos un comprobante fiscal digital
		
		FileSystemResource resource=new FileSystemResource("c:\\CFD\\xml\\9CRE0001154.xml");
		//System.out.println(resource.getURI());
		ComprobanteDocument document=ComprobanteDocument.Factory.parse(resource.getInputStream());
		CadenaOriginalBuilderV2Impl cd=new CadenaOriginalBuilderV2Impl();
		String res=cd.obtenerCadena(document);
		System.out.println(res);
				
		/*
		CadenaOriginalBuilderImpl cd=new CadenaOriginalBuilderImpl();
		FileSystemResource resource=new FileSystemResource("C:\\pruebas\\cfd\\prueba2\\Muestra.xml");
		FileSystemResource xslRes=new FileSystemResource("C:\\pruebas\\cfd\\prueba2\\cadenaoriginal_2_0.xslt");		
		String res=cd.transformation(resource.getPath(),xslRes.getPath() , null);
		System.out.println(res);
		*/
		
		
	}

}
