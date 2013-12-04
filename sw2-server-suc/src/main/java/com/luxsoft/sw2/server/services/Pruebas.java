package com.luxsoft.sw2.server.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ReplicationMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.sw3.replica.EntityLog;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.transformer.HibernateTransformer;

public class Pruebas {
	
	public static void salvarEnArchivoDeTexto(String data,String filePath) {
		try {
			File target=new File(filePath);
			System.out.println("Salvando a: "+target.getAbsolutePath());
			FileOutputStream out=new FileOutputStream(target);
			OutputStreamWriter writer=new OutputStreamWriter(out, "UTF-8");
			BufferedWriter buf=new BufferedWriter(writer);
			buf.write(data);
			buf.flush();
			buf.close();
			writer.close();
			out.flush();
			out.close();
		} catch (Exception e) {
			throw new RuntimeException("Imposible salvar lista el archivo: "+filePath+"  Causa: "+ExceptionUtils.getRootCauseMessage(e),e);
		}
		
	}
	
	public static void salvarEnArchivoDeTexto(List<String> lineas,String filePath) {
		try {
			File target=new File(filePath);
			System.out.println("Salvando a: "+target.getAbsolutePath());
			FileOutputStream out=new FileOutputStream(target);
			OutputStreamWriter writer=new OutputStreamWriter(out, "UTF-8");
			BufferedWriter buf=new BufferedWriter(writer);
			for(String linea:lineas){
				buf.write(linea);
				buf.newLine();
			}
			buf.flush();
			buf.close();
			writer.close();
			out.flush();
			out.close();
		} catch (Exception e) {
			throw new RuntimeException("Imposible salvar lista el archivo: "+filePath+"  Causa: "+ExceptionUtils.getRootCauseMessage(e),e);
		}
		
	}
	
	public static List<String> leerDeTexto(String filePath){
		try {
			File target=new File(filePath);
			List<String> lines=new ArrayList<String>();
			System.out.println("Cargando archivo: "+target.getAbsolutePath());
			FileInputStream in=new FileInputStream(target);
			InputStreamReader reader=new InputStreamReader(in, "UTF-8");
			BufferedReader buf=new BufferedReader(reader);
			String line=buf.readLine();
			while(line!=null){
				lines.add(line);
				line=buf.readLine();
			}
			buf.close();
			reader.close();
			in.close();
			return lines;
		} catch (Exception e) {
			throw new RuntimeException("Imposible procesar el archivo batch: "+filePath+"  Causa: "+ExceptionUtils.getRootCauseMessage(e),e);
		}
		
	}
	
	public static String leerArchivo(String filePath){
		try {
			File target=new File(filePath);
			
			System.out.println("Cargando archivo: "+target.getAbsolutePath());
			FileInputStream in=new FileInputStream(target);
			InputStreamReader reader=new InputStreamReader(in, "UTF-8");
			BufferedReader buf=new BufferedReader(reader);
			String line=buf.readLine();
			StringBuffer sbuff=new StringBuffer();
			sbuff.append(line);
			while(line!=null){				
				line=buf.readLine();
			}
			buf.close();
			reader.close();
			in.close();
			return sbuff.toString();
		} catch (Exception e) {
			throw new RuntimeException("Imposible procesar el archivo batch: "+filePath+"  Causa: "+ExceptionUtils.getRootCauseMessage(e),e);
		}
		
	}
	
	
	public static void replicaBatch(final String filePath){
		//final List<String> data=new ArrayList<String>();
		final String data=(String)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				String hql="from Existencia x where x.year=? and x.mes=? and x.sucursal.id=? ";
				List rs=session.createQuery(hql)
						.setInteger(0, 2012)
						.setInteger(1, 5)
						.setInteger(2, 9).list();
				
				String ser=new JSONSerializer()	
				.exclude("*.class")
				.include("sucursal.id")
				.exclude("producto.proveedor.*","sucursal.*")
				
					//.exclude("producto.linea","producto.marca","producto.clase","producto.proveedor","producto.userLog","sucursal.direccion","sucursal.empresa")			
					//.transform(new HibernateTransformer(), "producto","sucursal")
					//.include("producto.id","sucursal.id")
					//.exclude("producto.*","sucursal.*")
					.serialize(rs);
				return ser;
			}
		});
		ServiceLocator2.getJmsTemplate().send("EXISTENCIAS.BATCH", new MessageCreator() {
			public Message createMessage(javax.jms.Session session) throws JMSException {
				TextMessage message=session.createTextMessage();
				message.setText(data);
				return message;
			}
		});
		//salvarEnArchivoDeTexto(data, filePath);
		/*
		String hql="from Existencia x where x.year=? and x.mes=? and x.sucursal.id=? ";
		List beans=ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{2012,5,9L});
		List<String> data=new ArrayList<String>();
		for(Object bean:beans){
			String ser=new JSONSerializer()	
			.exclude("*.class")
			.exclude("producto.*","sucursal.*")
			.include("producto.id","sucursal.id")
				//.exclude("producto.linea","producto.marca","producto.clase","producto.proveedor","producto.userLog","sucursal.direccion","sucursal.empresa")			
				//.transform(new HibernateTransformer(), "producto","sucursal")
				//.include("producto.id","sucursal.id")
				//.exclude("producto.*","sucursal.*")
				.serialize(bean);
			//Base64.encodeBase64(ser.getBytes("UTF-8"));
			data.add(ser);
		}
		System.out.println("Entidades serializadas: "+data.size());
		salvarEnArchivoDeTexto(data, filePath);*/
	}
	
	public static void batchImport(final String path){
		//String data=leerArchivo(path);
		TextMessage message=(TextMessage)ServiceLocator2.getJmsTemplate().receive("EXISTENCIAS.BATCH");
		String data;
		try {
			data = message.getText();
			List<Existencia> exis=new JSONDeserializer<List<Existencia>>().use(null, ArrayList.class)
					.use("values",Existencia.class)
					.deserialize(data);
			System.out.println("Existencias a registrar: "+exis.size());
			for(Existencia e:exis){
				System.out.println("Importando: "+e);
				e.setImportado(new Date());
				ServiceLocator2.getHibernateTemplate().replicate(e, ReplicationMode.OVERWRITE);
			}
		} catch (JMSException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//System.out.println(data);
		/*
		List<Existencia> exis=new JSONDeserializer<List<Existencia>>().use(null, ArrayList.class)
				.use("values",Existencia.class)
				.deserialize(data);
		System.out.println("Existencias a registrar: "+exis.size());
		for(Existencia e:exis){
			System.out.println("Importando: "+e);
		}
		
		List<String> data=leerDeTexto(path);
		JSONDeserializer des=new JSONDeserializer();
		for(String row:data){
			Object bean=des.deserialize(row);
			try {
				Existencia e=(Existencia)bean;
				e.setImportado(new Date());
				//ServiceLocator2.getHibernateTemplate().replicate(bean, ReplicationMode.OVERWRITE);
			} catch (Exception e) {
				
				System.out.println("Entidad sin importar: "+bean+ "Causa: "+ExceptionUtils.getRootCauseMessage(e));
			}
			
		}*/
	}
	
	
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		//replicaBatch("/basura/exisJson.txt");
		batchImport("/basura/exisJson.txt");
		//replicarAbono();
	}

}
