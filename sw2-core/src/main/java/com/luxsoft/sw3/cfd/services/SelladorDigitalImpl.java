package com.luxsoft.sw3.cfd.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.cfd.dao.CertificadoDeSelloDigitalDao;
import com.luxsoft.sw3.cfd.model.CFDException;
import com.luxsoft.sw3.cfd.model.CertificadoDeSelloDigital;
import com.luxsoft.utils.LoggerHelper;

@Component("selladorDigitalManager")
public class SelladorDigitalImpl implements SelladorDigital{
	
	@Autowired
	private CertificadoDeSelloDigitalDao certificadoDeSelloDigitalDao;
	
	private CertificadoDeSelloDigital certificadoDigital;
	
	private PrivateKey privateKey;
	
	private Logger logger=LoggerHelper.getLogger();
	
	public SelladorDigitalImpl(){
		init();
	}
	
	public void init(){
		java.security.Security.addProvider(new BouncyCastleProvider());
	}
	
	/**
	 * 
	 */
	public String getSello(final String cadenaOrignal){		
		
		try {
			final byte[] cadena=cadenaOrignal.getBytes("UTF-8");
			String algoritmo=getCertificadoDigital().getAlgoritmo();
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
	
	/**
	 * Lee el contenido del archivo de la llave privada y regresa su contenido
	 *  
	 */
	private byte[] leerArchivoDeLlavePrivada(){		
		String privateKeyPath=System.getProperty("cfd.pk.path")+"/"+getCertificadoDigital().getPrivateKeyPath();
		FileSystemResource resource = new FileSystemResource(privateKeyPath);
		/*File keyFile = resource.getFile();
		System.out.println("Encode key size: " + keyFile.length());
		*/
		FileInputStream keyfis;
		try {
			
			keyfis = new FileInputStream(resource.getFile());
			byte[] encodedPrivateKey = new byte[keyfis.available()];
			keyfis.read(encodedPrivateKey);
			keyfis.close();
			return encodedPrivateKey;
		} catch (IOException e) {
			throw new RuntimeException("Error tratando de leer la llave privada: "+ExceptionUtils.getRootCauseMessage(e),ExceptionUtils.getRootCause(e));
		}
		
	}
	
	public boolean validar(final String cadenaOriginal,final String selloDigital){
		
		try {
			/*
			String cerPath=getCertificadoDigital().getCertificadoPath();
			FileSystemResource publicKeyResource = new FileSystemResource(getCertificatePath());
			CertificateFactory fact= CertificateFactory.getInstance("X.509","BC");
			byte[] decodedData=Base64.decode(selloDigital);
		    // read the certificate
		     * 
		     */
		    //X509Certificate certificate = (X509Certificate)fact.generateCertificate(publicKeyResource.getInputStream());
			
			Signature signature=Signature.getInstance(getCertificadoDigital().getAlgoritmo(),"BC");
			signature.initVerify(getCertificado());
			signature.update(cadenaOriginal.getBytes("UTF-8"));
			byte[] decodedData=Base64.decode(selloDigital);
			return signature.verify(decodedData);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error validando sello digital",ExceptionUtils.getRootCause(e));
		} 	    
	}
	
	
	
	public X509Certificate getCertificado(){
		return getCertificadoDigital().getCertificado();
	}
	
	
	
	public CertificadoDeSelloDigital getCertificadoDigital(){
		if(certificadoDigital==null){
			certificadoDigital=certificadoDeSelloDigitalDao.get(1L);
		}
		return certificadoDigital;
	}
	

	public CertificadoDeSelloDigitalDao getCertificadoDeSelloDigitalDao() {
		return certificadoDeSelloDigitalDao;
	}

	public void setCertificadoDeSelloDigitalDao(
			CertificadoDeSelloDigitalDao certificadoDeSelloDigitalDao) {
		this.certificadoDeSelloDigitalDao = certificadoDeSelloDigitalDao;
	}

	public static void main(String[] args) throws Exception{
		/*
		String cadenaOriginal="ESTA ES LA CADENA ORIGINAL A FIRMAR";
		SelladorDigitalImpl sellador=new SelladorDigitalImpl();
		sellador.init();
		sellador.setCertificadoDeSelloDigitalDao(ServiceLocator2.getCertificadoDeSelloDigitalDao());
		String sello=sellador.getSello(cadenaOriginal);
		System.out.println("Cadena original: "+cadenaOriginal);
		System.out.println("Sello digital: "+sello);
		System.out.println("Validacion: "+sellador.validar(cadenaOriginal,sello));
		System.out.println("Certificado digital: "+sellador.getCertificado().getSerialNumber());
				*/
		FileSystemResource r=new FileSystemResource("z:/cfd/cert/00001000000202171318.cer");
		if(r.exists())
			System.out.println("Cer: "+r.getPath());
	}
}
