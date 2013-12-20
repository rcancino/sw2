package com.luxsoft.sw3.cfdi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.service.ServiceLocator2;

public class CFDITasks {
	
	private final HibernateTemplate hibernateTemplate;
	
	public CFDITasks(HibernateTemplate template){
		this.hibernateTemplate=template;
	}
	
	public void subirLlavePrivada() throws IOException{
		String path="sat/PAPEL_CFD.key";
		ClassPathResource resource=new ClassPathResource(path);
		Assert.isTrue(resource.exists(),"No existe el recurso: "+path);
		
		File file=resource.getFile();
		byte[] data=new byte[(int)file.length()];
		FileInputStream is=new FileInputStream(file);
		is.read(data);
		is.close();
		Empresa empresa=(Empresa)hibernateTemplate.get(Empresa.class, 1L);
		empresa.setCfdiPrivateKey(data);
		empresa.setNumeroDeCertificado("00001000000202171318");
		hibernateTemplate.merge(empresa);
		
	}
	
	public void subirCertificado() throws IOException{
		String path="sat/00001000000202171318.cer";
		ClassPathResource resource=new ClassPathResource(path);
		Assert.isTrue(resource.exists(),"No existe el recurso: "+path);
		
		File file=resource.getFile();
		byte[] data=new byte[(int)file.length()];
		FileInputStream is=new FileInputStream(file);
		is.read(data);
		is.close();
		
		Empresa empresa=(Empresa)hibernateTemplate.get(Empresa.class, 1L);
		empresa.setCertificadoDigital(data);
		empresa.setNumeroDeCertificado("00001000000202171318");
		hibernateTemplate.merge(empresa);
		
	}
	
	public void inicializar() throws Exception{
		subirLlavePrivada();
		subirCertificado();
		Empresa empresa=(Empresa)hibernateTemplate.get(Empresa.class, 1L);
		empresa.setTipoDeComprobante(Empresa.TipoComprobante.CFDI);
		hibernateTemplate.merge(empresa);
	}
	
	public static void main(String[] args) throws Exception{
		CFDITasks tasks=new CFDITasks(ServiceLocator2.getHibernateTemplate());
		///tasks.subirLlavePrivada();
		//tasks.subirCertificado();
		tasks.inicializar();
	}
	

}
