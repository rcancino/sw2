package com.luxsoft.sw3.cfd.services;

import java.util.Map;

import mx.gob.sat.cfd.x2.ComprobanteDocument;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscalCreationException;

/**
 * Definición de las taread a realizar para la generacion y persistencia de un CFD
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface CFDSupport {
	
	/**
	 * Inicializa el comprobante fiscal digital con los atributos generales
	 * 
	 * @return El documento del comprobante correctamente inicializado
	 */
	public ComprobanteDocument inicializar();
	
	/**
	 * Registra el emisor del comprobante fiscal digital
	 * 
	 * @param cfd
	 */
	public void registrarEmisor(Comprobante cfd);
	
	public void registrarExpedidoEn(Comprobante cfd,Sucursal sucursal);
	
	/**
	 * Registra y configura el Receptor y sus propiedades tomando como base 
	 * un cliente de SIIPAP
	 * 
	 * @param cfd El comprobante fiscal digital que requiere el receptor
	 * @param cliente El cliente de SIIPAP
	 */
	public void registrarReceptor(Comprobante cfd,Cliente cliente);
	
	
	/**
	 * Asigna la serie y el folio
	 * 
	 * @param serie
	 * @param sucursal
	 * @param cfd
	 */
	public void registrarSerieFolio(String serie,Sucursal sucursal,Comprobante cfd);
	
	/**
	 * Registra una instancia de {@link ComprobanteFiscal} vinculada al CFD
	 * 
	 * @param document
	 * @param tipo
	 * @param origen
	 * @return
	 */
	public ComprobanteFiscal registrarComprobante(ComprobanteDocument document,String tipo,String origen);
	
	public String generarCadenaOriginal(ComprobanteDocument document);
	
	public String registrarSelloDigital(ComprobanteDocument documentl);
	
	/**
	 * Depuracion final del CFD antes de la persistencia
	 * 
	 * @param cfdDocument
	 */
	public void depuracionFinal(ComprobanteDocument cfdDocument);
	
	/**
	 *  Regresa el nombre del archivo XML del CFD con el que sera persistido
	 *  
	 * @param cfd
	 * @param source
	 * @return
	 */
	public String getDocumentXMLFileName(Comprobante cfd,Object  source);
	
	public void validarDocumento(ComprobanteDocument document);
	
	/**
	 * Persiste el CFD (XML)
	 * 
	 * @param xmlName
	 * @param docto
	 * @param cf
	 * @return
	 * @throws ComprobanteFiscalCreationException
	 */
	public ComprobanteFiscal salvar(String xmlName,ComprobanteDocument docto,ComprobanteFiscal cf) throws ComprobanteFiscalCreationException;
	
	

}
