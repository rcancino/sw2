package com.luxsoft.sw3.cfdi.parches;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;


import com.edicom.ediwinws.cfdi.client.CfdiClient;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.CFDITimbrador;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.cfdi.model.CFDIClienteMails;

public class CorreccionDeTimbradoEnModoPruebas {
	
	public CorreccionDeTimbradoEnModoPruebas() {
		
	}
	
	
	public static void run() throws Exception{
		
		final File sourceDir=new File("Y:\\cfd\\xml\\cfdi");
		Assert.isTrue(sourceDir.exists(),"No existe la carpeta de CFDI sin timbrar");
		Assert.isTrue(sourceDir.isDirectory());
		System.out.println("Corrigiendo timbrado en URL: "+System.getProperty("jdbc.url"));
		System.out.println("Procesando carpeta: "+sourceDir.getAbsolutePath());
		
		List<String> ids=ServiceLocator2.getJdbcTemplate()
				.queryForList("SELECT CFD_ID FROM SX_CFDI C WHERE C.UUID LIKE ?"
						,new Object[]{"%-7E57-%"},String.class);
		
		System.out.println("Comprobantes a procesar: "+ids.size());
		int row=0;
		for(String id:ids){
			CFDI cfdi=ServiceLocator2.getCFDIManager().getCFDI(id);
			try{
				//Buscando archivo sin timbrar
				String xmlName=StringUtils.removeStart(cfdi.getXmlFilePath(), "SIGN_");
				File xml=new File(sourceDir,xmlName);
				Assert.isTrue(xml.exists(),"No encontro el archivo XML sin timbrar: "+xmlName);
				System.out.println("Utilizando xml sin timbrar: "+xml.getName());
				byte[] data=new byte[(int)xml.length()];
				FileInputStream is=new FileInputStream(xml);
				is.read(data);
				is.close();
				
				cfdi.setXml(data);
				cfdi.setTimbre(null);
				cfdi.cargarTimbrado();
				cfdi.setXmlFilePath(xmlName);
				cfdi.setComentario("RE TIMBRADO POR ERROR DE PRUEBAS ");
				// Timbrando nuevamente
				cfdi.setDocument(null);
				ServiceLocator2.getCFDIManager().timbrar(cfdi);
				//session.flush();
				//session.clear();
				row++;
			}catch(Exception e){
				System.out.println("Error procesando cfdi "+ExceptionUtils.getRootCauseMessage(e));
			}
		}
		
		System.out.println("Total de cfdi procesados: "+row);
	}
	
	
	
	
	public static void reTimbrar(String uuid) throws Exception{
		final CFDITimbrador timbrador=new CFDITimbrador();
		timbrador.setHibernateTemplate(ServiceLocator2.getHibernateTemplate());
		timbrador.afterPropertiesSet();
		List<CFDI> list=ServiceLocator2.getHibernateTemplate().find("from CFDI c where c.UUID=?",uuid);
		int count=0;
		
		for(CFDI cfdi:list){
			try {
				//System.out.println("Re timbrando: "+cfdi.getId()+" XML: "+cfdi.getComprobante().toString());
				//timbrador.timbrar(cfdi);
				System.out.println("Re Timbrado OK: "+cfdi.getId()+" UUID: "+cfdi.getUUID());
				//cfdi.setComentario("RE PROGRAMAR ENVIO");
				//hibernateTemplate.merge(cfdi);
				count++;
			} catch (Exception e) {
				System.out.println("Error timbrando cfdi: "+cfdi.getId()+" Error: "+ExceptionUtils.getMessage(e));
				e.printStackTrace();
			}
			
		}
		System.out.println("Total: "+list.size()+ " Re timbrados: "+count);
		
	}
	
	public static void main(String[] args) throws Exception{
		//-Djdbc.url=jdbc:mysql://10.10.1.101/produccion -DsucursalOrigen=TACUBA
		
		System.setProperty("jdbc.url", "jdbc:mysql://10.10.7.1/produccion");
		System.setProperty("sucursalOrigen", "CF5FEBRERO");
		System.setProperty("cfdi.timbrado", "produccion");
		System.setProperty("cfd.dir.path", "C:\\cfdi\\correcciones");
		CorreccionDeTimbradoEnModoPruebas.run();
		CorreccionDeTimbradoEnModoPruebasReGenerandoXML.run();
	}

}
