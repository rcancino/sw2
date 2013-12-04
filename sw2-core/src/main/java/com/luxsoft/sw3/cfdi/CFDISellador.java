package com.luxsoft.sw3.cfdi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.utils.LoggerHelper;

@Service("cfdiSellador")
public class CFDISellador implements InitializingBean{
	
	private Logger logger=LoggerHelper.getLogger();
	
	//private Empresa empresa;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	public CFDISellador(){
		
	}
	
	
	public String generarSello(final String cadenaOrignal){		
		
		try {
			final byte[] cadena=cadenaOrignal.getBytes("UTF-8");
			String algoritmo="MD5withRSA";
			Signature signature=Signature.getInstance(algoritmo,"BC");
			signature.initSign(getPrivateKey());
			signature.update(cadena);
			
			final byte[] signedData=signature.sign();
			final byte[] encoedeData=Base64.encode(signedData);
			return new String(encoedeData,"UTF-8");
		} catch (Exception  e) {
			e.printStackTrace();
			String msg="Error generando sello digital: "+ExceptionUtils.getRootCauseMessage(e);
			logger.error(msg,e);
			throw new RuntimeException(msg,ExceptionUtils.getCause(e));
		} 
	}
	
	public boolean validarSello(final String cadenaOriginal,final String selloDigital){
		try {
			String algoritmo="MD5withRSA";
			Signature signature=Signature.getInstance(algoritmo,"BC");
			signature.initVerify(getCertificado());
			signature.update(cadenaOriginal.getBytes("UTF-8"));
			byte[] decodedData=Base64.decode(selloDigital);
			return signature.verify(decodedData);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error validando sello digital",ExceptionUtils.getRootCause(e));
		} 	  
	}
	
	
	private X509Certificate certificado;
	
	public X509Certificate getCertificado() {
		if(certificado==null){
			try{
				java.security.Security.addProvider(new BouncyCastleProvider());
				CertificateFactory fact= CertificateFactory.getInstance("X.509","BC");
				InputStream is=new ByteArrayInputStream(getEmpresa().getCertificadoDigital());
				certificado = (X509Certificate)fact.generateCertificate(is);
				certificado.checkValidity();
				return certificado;
			}catch (Exception e) {
				String msg=ExceptionUtils.getRootCauseMessage(e);
				throw new RuntimeException("Error tratando de leer Certificado: "+msg,e);
			}
		}
		return certificado;
		
	}
	
	private PrivateKey privateKey;
	
	private PrivateKey getPrivateKey(){
		if(privateKey==null){
			
			final byte[] encodedKey=leerArchivoDeLlavePrivada();
			PKCS8EncodedKeySpec keySpec=new PKCS8EncodedKeySpec(encodedKey);
			try {
				final KeyFactory keyFactory=KeyFactory.getInstance("RSA","BC");
				privateKey=keyFactory.generatePrivate(keySpec);
				logger.info("PrivateKey object successfully loaded...");
				
			} catch (Exception e) {
				throw new RuntimeException("Error generando la llave privada", ExceptionUtils.getRootCause(e));
			}
		}
		return privateKey;
	}
	
	private byte[] leerArchivoDeLlavePrivada(){
		try {
			byte[] encodedPrivateKey = getEmpresa().getCfdiPrivateKey();
			return encodedPrivateKey;
		} catch (Exception e) {
			throw new RuntimeException("Error tratando de leer la llave privada: "+ExceptionUtils.getRootCauseMessage(e),ExceptionUtils.getRootCause(e));
		}
		
	}
	
	private Empresa empresa;
	
	private Empresa getEmpresa() {
		if(empresa==null){
			empresa= (Empresa)hibernateTemplate.find("from Empresa e").get(0);
		}
		return empresa;
	}
	
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}


	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}


	public void afterPropertiesSet() throws Exception {
		Assert.notNull(hibernateTemplate, "Se requiere registrar hibernateTemplate");
		//Inicializamos el proveedor BouncyCastle de criptografia
		java.security.Security.addProvider(new BouncyCastleProvider());
		System.out.println("BouncyCastle Provider incializado.....");
	}

}
