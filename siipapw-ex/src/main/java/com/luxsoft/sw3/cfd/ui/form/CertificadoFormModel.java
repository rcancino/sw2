package com.luxsoft.sw3.cfd.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.core.io.FileSystemResource;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.cfd.model.CertificadoDeSelloDigital;

/**
 * FormModel para el mantenimiento de Certificados digitales
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CertificadoFormModel extends DefaultFormModel{
	
	private ValueModel messageHolder;

	public CertificadoFormModel(CertificadoDeSelloDigital bean) {
		super(bean);
	}
	
	public void init(){
		super.init();
		java.security.Security.addProvider(new BouncyCastleProvider());
		getModel("certificadoPath").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				getCertificado().resolverNumero();
				getCertificado().resolverVencimiento();
			}
		});
		getModel("privateKeyPath").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				//validarConsistencia();
			}			
		});
		messageHolder=new ValueHolder(null);
		/*messageHolder.addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				validate();
			}			
		});*/
	}
	
	boolean certificadoValido=false;
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		super.addValidation(support);
		if(!certificadoValido)
			//support.getResult().addError("Certificado y llave privada no validados");
		if(StringUtils.isNotBlank(getCertificado().getCertificadoPath())
				&&StringUtils.isNotBlank(getCertificado().getPrivateKeyPath())
				&&StringUtils.isNotBlank(getCertificado().getAlgoritmo())
		)
		{
			messageHolder.setValue(validarConsistencia());
		}
	}

	public CertificadoDeSelloDigital getCertificado(){
		return (CertificadoDeSelloDigital)getBaseBean();
	}

	public CertificadoDeSelloDigital commit() {
		CertificadoDeSelloDigital res=getCertificado();
		res=(CertificadoDeSelloDigital)ServiceLocator2.getCertificadoDeSelloDigitalDao().save(res);
		return res;
	}
	
	public String validarConsistencia(){
		String cadena="CADENA DE PRUEBA PARA PROBAR SELLADO DIGITAL";
		String sello=getSello(cadena);
		boolean res=validar(cadena, sello);
		String pattern="Cadena de prueba {0}" +
				    "\n Sello generado:  {1}" +
				    "\n Resultado:       {2}" +
				    "\n Certificado\n {3}";
		String resultado="";
		if(res){
			resultado="OK el certificado y la llave privada corresponden correctamente";
			certificadoValido=true;
			
		}
		else{
			resultado="ERROR certificado y llave public no corresponden a la llave privada";
			certificadoValido=false;
		}
		return MessageFormat.format(
				pattern
				, cadena
				,sello
				,resultado
				,getCertificado().getCertificado()
				);
	}

	public ValueModel getMessageHolder() {
		return messageHolder;
	}
	
	public boolean validar(final String cadenaOriginal,final String selloDigital){
		
		try {
			Signature signature=Signature.getInstance(getCertificado().getAlgoritmo(),"BC");
			signature.initVerify(getCertificado().getCertificado());
			signature.update(cadenaOriginal.getBytes("UTF-8"));
			byte[] decodedData=Base64.decode(selloDigital);
			return signature.verify(decodedData);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error validando sello digital: "+ExceptionUtils.getRootCauseMessage(e),e);
		} 	    
	}
	
	/**
	 * 
	 */
	public String getSello(final String cadenaOrignal){		
		
		try {
			final byte[] cadena=cadenaOrignal.getBytes("UTF-8");
			String algoritmo=getCertificado().getAlgoritmo();
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
		final byte[] encodedKey=leerArchivoDeLlavePrivada();
		PKCS8EncodedKeySpec keySpec=new PKCS8EncodedKeySpec(encodedKey);
		try {
			final KeyFactory keyFactory=KeyFactory.getInstance("RSA","BC");
			logger.info("PrivateKey object successfully loaded...");
			return keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			throw new RuntimeException("Error generando la llave privada", ExceptionUtils.getRootCause(e));
		}
	}
	
	/**
	 * Lee el contenido del archivo de la llave privada y regresa su contenido
	 *  
	 */
	private byte[] leerArchivoDeLlavePrivada(){		
		FileSystemResource resource = new FileSystemResource(getCertificado().getPrivateKeyPath());
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

}
