package com.luxsoft.sw3.cfd.ui.validaciones;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.jgoodies.binding.beans.Model;
import com.jgoodies.uif.util.SystemUtils;
import com.luxsoft.utils.LoggerHelper;

/**
 * Controlador central para la validacion de comprobantes fiscales digitales
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CFDValidacionController extends Model {
	
	public static final String CFD_FILE_CHANGED_PROPERTY="cfd_changed";
	public static final String VALIDANDO_PROPERTY_NAME="validando";
	public static final String VALIDANDO_MESSAGE_PROPERTY_NAME="validandoMessage";
	public static final String VALIDACION_HTML_RESULT_PROPERTY="validacionHTMLResult";
	
	private File xmlFile;
	
	private boolean validando;
	
	private String validandoMessage;
	
	private String validacionHTMLResult;
	
	private Validador validador;
	
	private Logger logger=LoggerHelper.getLogger();
	
	public CFDValidacionController(){
		
	}
	
	public void registrarComprobante(final File xmlFile){
		Assert.notNull(xmlFile,"Archivo XML del CFD no puede ser nulo");
		Assert.isTrue(xmlFile.exists(),"El objeto File no existe en el sistema de archivos");
		Assert.isTrue(xmlFile.isFile(),"El objeto File DEBE SER UN ARCHIVO no un directorio");
		Object old=this.xmlFile;
		this.xmlFile = xmlFile;
		firePropertyChange(CFD_FILE_CHANGED_PROPERTY, old, xmlFile);
		validar();
	}
	
	private void definirTipoDeComprobante(){
		logger.info("Definiendo tipo de archivo CFD ....");
		setValidador(new Validador2());
	}
	
	private String getHtmlResult(){
		return getValidador().getHTMLResult();
	}
	
	
	
	/**
	 * Valida el archivo XML registrado para verificar si es un CFD o CFDI valido
	 * Este metodo debe ser llamado de fomra asyncrona fuera del EDT para mejor desempeño
	 * 
	 */
	public void validar(){
		setValidando(true);
		final SwingWorker<String,String> worker=new SwingWorker<String,String>(){
			protected String doInBackground() throws Exception {
				logger.info("Validando archivo CFD(I):"+getFileDesc());				
				publish("Verificando versión de comprobante 2 (CFD) o 3 (CFDI)");
				definirTipoDeComprobante();
				SystemUtils.sleep(2000);
				
				publish("Generando lista de resultados (HTML)");
				getValidador().validar(getXmlFile());
				SystemUtils.sleep(3000);
				return getHtmlResult();
			}			
			
			protected void process(List<String> chunks) {
				for(String s:chunks){
					setValidandoMessage(s);
				}
			}

			protected void done() {
				String res;
				try {
					res = get();
				} catch (InterruptedException e) {
					res=ExceptionUtils.getFullStackTrace(e);
					logger.error(e);
					e.printStackTrace();
				} catch (ExecutionException e) {
					res=ExceptionUtils.getFullStackTrace(e);
					logger.error(e);
					e.printStackTrace();
				} finally{
					setValidando(false);
					setValidandoMessage("Validación terminada");
				}
				setValidacionHTMLResult(res);
			}			
			
		};
		worker.execute();
	}
	

	public File getXmlFile() {
		return xmlFile;
	}

	public String getFileDesc(){
		return getXmlFile().getAbsolutePath();
	}

	public boolean isValidando() {
		return validando;
	}

	public void setValidando(boolean validando) {
		boolean old=this.validando;
		this.validando = validando;
		firePropertyChange(VALIDANDO_PROPERTY_NAME, old, validando);
	}

	public String getValidandoMessage() {
		return validandoMessage;
	}

	public void setValidandoMessage(String validandoMessage) {
		Object old=this.validandoMessage;
		this.validandoMessage = validandoMessage;
		firePropertyChange(VALIDANDO_MESSAGE_PROPERTY_NAME, old, validandoMessage);
	}

	public String getValidacionHTMLResult() {
		return validacionHTMLResult;
	}

	public void setValidacionHTMLResult(String validacionHTMLResult) {
		Object old=this.validacionHTMLResult;
		this.validacionHTMLResult = validacionHTMLResult;
		firePropertyChange(VALIDACION_HTML_RESULT_PROPERTY, old, validacionHTMLResult);
	}

	public Validador getValidador() {
		return validador;
	}

	public void setValidador(Validador validador) {
		this.validador = validador;
	}

	
	
	
}
