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
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.luxsoft.sw3.cfd.model.CFDException;
import com.luxsoft.utils.LoggerHelper;

//@Component("selladorDigitalManager")
public class SelladorDigitalImpl_bak implements SelladorDigital{
	
	
	
	private String algoritmoDeEncriptado;
	
	private PrivateKey privateKey;
	
	private String privateKeyPath;
	
	private String certificatePath;
	
	private Logger logger=LoggerHelper.getLogger();
	
	public SelladorDigitalImpl_bak(){
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
			Signature signature=Signature.getInstance(getAlgoritmoDeEncriptado(),"BC");
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
		FileSystemResource resource = new FileSystemResource(getPrivateKeyPath());
		File keyFile = resource.getFile();
		System.out.println("Encode key size: " + keyFile.length());
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
			//FileSystemResource publicKeyResource = new FileSystemResource("Z:\\CFD\\00001000000102129215.cer");
			FileSystemResource publicKeyResource = new FileSystemResource(getCertificatePath());
			CertificateFactory fact= CertificateFactory.getInstance("X.509","BC");
			byte[] decodedData=Base64.decode(selloDigital);
		    // read the certificate
		    X509Certificate certificate = (X509Certificate)fact.generateCertificate(publicKeyResource.getInputStream());
			Signature signature=Signature.getInstance(getAlgoritmoDeEncriptado(),"BC");
			signature.initVerify(certificate);
			signature.update(cadenaOriginal.getBytes("UTF-8"));
			return signature.verify(decodedData);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error validando sello digital",ExceptionUtils.getRootCause(e));
		} 	    
	    
	}
	
	private X509Certificate certificado;
	
	public X509Certificate getCertificado(){
		if(certificado==null){
			
			try {
				FileSystemResource publicKeyResource = new FileSystemResource(getCertificatePath());
				CertificateFactory fact= CertificateFactory.getInstance("X.509","BC");
				certificado = (X509Certificate)fact.generateCertificate(publicKeyResource.getInputStream());
				certificado.checkValidity();	
				logger.info("Certificado cargado exitosamente: "+certificado);
				logger.info("No de derie: "+certificado.getSubjectX500Principal());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				throw new CFDException("Error cargando el certificado: "+getCertificatePath()
						+ "\n"+ExceptionUtils.getRootCauseMessage(e), e);
			}
			
		}
		return certificado;
	}
	
	
	public String getPrivateKeyPath() {
		if(privateKeyPath==null){
			//privateKeyPath=System.getProperty("cfd.pk.path","Z:\\CFD\\CERT\\TACUBA_CFD.KEY");
			privateKeyPath=System.getProperty("cfd.pk.path","G:\\VALIDQRO\\papelsabajio.key");
		}
		return privateKeyPath;
	}

	public String getCertificatePath() {
		if(certificatePath==null){
			//String path="C:\\sw3\\cfd\\cert\\00001000000102129215.cer";
			//certificatePath=System.getProperty("cfd.cer.path","Z:\\CFD\\CERT\\00001000000102129215.cer");
			certificatePath=System.getProperty("cfd.cer.path","G:\\VALIDQRO\\00001000000102242997.cer");
		}
		return certificatePath;
	}

	public void setPrivateKeyPath(String privateKeyPath) {
		this.privateKeyPath = privateKeyPath;
	}
	
	

	public String getAlgoritmoDeEncriptado() {
		if(algoritmoDeEncriptado==null){
			algoritmoDeEncriptado=System.getProperty("cfd.pk.alg", "MD5withRSA");
		}
		return algoritmoDeEncriptado;
	}

	public void setAlgoritmoDeEncriptado(String algoritmoDeEncriptado) {
		
		this.algoritmoDeEncriptado = algoritmoDeEncriptado;
	}

	public static void main(String[] args) throws Exception{
		//System.setProperty("cfd.pk.path", "G:\\VALIDQRO\\papelsabajio.key");
		//System.setProperty("cfd.cer.path", "G:\\VALIDQRO\\00001000000102242997.cer");
		String cadenaOriginal="ESTA ES LA CADENA ORIGINAL A FIRMAR";
		SelladorDigitalImpl_bak sellador=new SelladorDigitalImpl_bak();
		sellador.init();
		sellador.setAlgoritmoDeEncriptado(SelladorDigital.ALGORITMOS[0]);
		//sellador.setPrivateKeyPath("Z:\\CFD\\TACUBA_CFD.KEY");
		String sello=sellador.getSello(cadenaOriginal);
		System.out.println("Cadena original: "+cadenaOriginal);
		System.out.println("Sello digital: "+sello);
		System.out.println("Validacion: "+sellador.validar(cadenaOriginal,sello));
		System.out.println("Certificado digital: "+sellador.getCertificado().getSerialNumber());
				
	}
}
