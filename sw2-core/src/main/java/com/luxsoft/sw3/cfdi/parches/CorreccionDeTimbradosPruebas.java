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
import org.hibernate.Session;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;


import com.edicom.ediwinws.cfdi.client.CfdiClient;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.CFDITimbrador;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.cfdi.model.CFDIClienteMails;

public class CorreccionDeTimbradosPruebas {
	
	private final HibernateTemplate hibernateTemplate;
	
	public CorreccionDeTimbradosPruebas(HibernateTemplate template) {
		this.hibernateTemplate=template;
	}
	
	
	public void guardarErroneos() throws Exception{
		
		List<String> lineas=new ArrayList<String>();
		@SuppressWarnings("unchecked")
		List<CFDI> rows=hibernateTemplate.find("from CFDI c ");
		for(CFDI cfdi:rows){
			if(cfdi.getTimbreFiscal().getUUID()!=null){
				String uuid=cfdi.getTimbreFiscal().getUUID();
				if(StringUtils.contains(uuid,"-7E57-" )){
					String msg="{0};{1};{2};{3};{4};{5}";
					String correo="";
					Venta venta=(Venta)hibernateTemplate.get(Venta.class, cfdi.getOrigen());
					if(venta!=null){
						List data=hibernateTemplate.find("from CFDIClienteMails c where c.cliente.id=?",venta.getCliente().getId());
						if(!data.isEmpty()){
							CFDIClienteMails cc=(CFDIClienteMails)data.get(0);
							correo=cc.getEmail1();
							
						}
					}
					if(StringUtils.isBlank(correo)){
						correo="SIN CORREO";
					}
					String linea=MessageFormat.format(msg, cfdi.getTipo(),cfdi.getSerie(),cfdi.getFolio(),cfdi.getReceptor(),correo,cfdi.getId());
					lineas.add(linea);
				}
			}
		}
		Writer out=new FileWriter("C:\\basura\\cfdiErrors.csv", false);
		BufferedWriter writer=new BufferedWriter(out);
		
		for(String line:lineas){
			writer.write(line);
			writer.newLine();
		}
		writer.flush();
		writer.close();
		out.close();
		
		
	}
	
	public void corregirErrores(String file)throws Exception{
		List<String> rows=new ArrayList<String>();
		
		Reader in=new FileReader(file);
		BufferedReader reader=new BufferedReader(in);
		String line=reader.readLine();
		while(line!=null){
			rows.add(line);
			line=reader.readLine();
		}
		reader.close();
		
		for(String row:rows){
			try {
				limpiarTimbrado(row);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error limpiando timbrando : "+row+ " Error: "+ExceptionUtils.getRootCauseMessage(e));
			}
		}
		System.out.println("Registros a reparar: "+rows.size());
	}
	
	public void limpiarTimbrado(String row) throws Exception{
		String[] data=StringUtils.split(row, ';');
		
		//System.out.println("Parsed row: "+data.length+ "  "+row);
		
		String pattern="Limpiando timbrado para cfdi {0}-{1} cfdi:{2}";
		final String cfdiId=data[5];
		final String message=MessageFormat.format(pattern, data[1],data[2],cfdiId);
		
		
		hibernateTemplate.execute(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				CFDI cfdi=(CFDI)session.load(CFDI.class, cfdiId);
				Assert.isTrue(StringUtils.contains(cfdi.getTimbreFiscal().getUUID(), "-7E57-"),"El timbrado no es de prueba");
				
				String xmlName=StringUtils.removeStart(cfdi.getXmlFilePath(), "SIGN_");
				String xmlPath="c:\\basura\\cfditac\\"+xmlName;
				Resource resource=new FileSystemResource(xmlPath);
				Assert.isTrue(resource.exists(),"No existe el archivo xml: "+xmlPath);
				try {
					File xml=resource.getFile();
					byte[] data=new byte[(int)xml.length()];
					FileInputStream is=new FileInputStream(xml);
					is.read(data);
					is.close();
					
					cfdi.setXml(data);
					cfdi.setTimbre(null);
					cfdi.cargarTimbrado();
					cfdi.setXmlFilePath(xmlName);
					cfdi.setUUID("REPROGRAMADO PARA TIMBRADO");
					cfdi.setComentario("RE PROGRAMAR ENVIO");
					System.out.println(message+ " UUID: "+cfdi.getTimbreFiscal().getUUID());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				
				return null;
			}
		});
	}
	
	public void limpiarTimbradoIndividual(final String  cfdiId) throws Exception {
		
		hibernateTemplate.execute(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				CFDI cfdi=(CFDI)session.load(CFDI.class, cfdiId);
				Assert.isTrue(StringUtils.contains(cfdi.getTimbreFiscal().getUUID(), "-7E57-"),"El timbrado no es de prueba");
				
				String xmlName=StringUtils.removeStart(cfdi.getXmlFilePath(), "SIGN_");
				String xmlPath="c:\\basura\\cfditac\\"+xmlName;
				Resource resource=new FileSystemResource(xmlPath);
				Assert.isTrue(resource.exists(),"No existe el archivo xml: "+xmlPath);
				try {
					File xml=resource.getFile();
					byte[] data=new byte[(int)xml.length()];
					FileInputStream is=new FileInputStream(xml);
					is.read(data);
					is.close();
					
					cfdi.setXml(data);
					cfdi.setTimbre(null);
					cfdi.cargarTimbrado();
					cfdi.setXmlFilePath(xmlName);
					cfdi.setUUID("REPROGRAMADO PARA TIMBRADO");
					cfdi.setComentario("RE PROGRAMAR ENVIO");
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				
				return null;
			}
		});
	}
	
	public void reTimbrar() throws Exception{
		final CFDITimbrador timbrador=new CFDITimbrador();
		timbrador.setHibernateTemplate(hibernateTemplate);
		timbrador.afterPropertiesSet();
		List<CFDI> list=hibernateTemplate.find("from CFDI c where c.UUID=?","REPROGRAMADO PARA TIMBRADO");
		int count=0;
		
		for(CFDI cfdi:list){
			try {
				//System.out.println("Re timbrando: "+cfdi.getId()+" XML: "+cfdi.getComprobante().toString());
				timbrador.timbrar(cfdi);
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
	
	

}
