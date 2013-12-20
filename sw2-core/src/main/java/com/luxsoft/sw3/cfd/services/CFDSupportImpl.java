package com.luxsoft.sw3.cfd.services;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import mx.gob.sat.cfd.x2.ComprobanteDocument;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Emisor;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Emisor.RegimenFiscal;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Receptor;
import mx.gob.sat.cfd.x2.TUbicacion;
import mx.gob.sat.cfd.x2.TUbicacionFiscal;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.sw3.cfd.CFDUtils;
import com.luxsoft.sw3.cfd.dao.CertificadoDeSelloDigitalDao;
import com.luxsoft.sw3.cfd.dao.FolioFiscalDao;
import com.luxsoft.sw3.cfd.model.CFDException;
import com.luxsoft.sw3.cfd.model.CertificadoDeSelloDigital;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscalCreationException;
import com.luxsoft.sw3.cfd.model.Conversiones;
import com.luxsoft.sw3.cfd.model.FolioFiscal;
import com.luxsoft.utils.LoggerHelper;


@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public abstract class CFDSupportImpl  implements CFDSupport{
	
	private Empresa empresa;
	
	private String cfdDirPath;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private FolioFiscalDao folioFiscalDao;
	
	@Autowired
	private CadenaOriginalBuilder cadenaBuider;
	
	@Autowired
	private SelladorDigital sellador;
	
	private Logger logger=LoggerHelper.getLogger();
	
	private String cfdVersion="2.2";
	
	private String cfdSchemaLocation="http://www.sat.gob.mx/cfd/2 http://www.sat.gob.mx/sitio_internet/cfd/2/cfdv22.xsd";
	
	@Autowired
	private CertificadoDeSelloDigitalDao certificadoDeSelloDigitalDao;
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.cfd.services.CFDSupport#inicializar(mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante)
	 */
	public ComprobanteDocument inicializar() {
		final ComprobanteDocument document=ComprobanteDocument.Factory.newInstance();
		Comprobante cfd=document.addNewComprobante();
		cfd.setVersion(cfdVersion);
		
		Calendar c=Calendar.getInstance();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		XmlDateTime xmlDateTime = XmlDateTime.Factory.newInstance();
		xmlDateTime.setStringValue(df.format(c.getTime()));
		
		cfd.setFecha(xmlDateTime.getCalendarValue());
		
		return document;
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.cfd.services.CFDSupport#registrarEmisor(mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante)
	 */
	public void registrarEmisor(Comprobante cfd){
		
		Emisor emisor=cfd.addNewEmisor();
		emisor.setNombre(getEmpresa().getNombre());
		emisor.setRfc(getEmpresa().getRfc());
		//RegimenFiscal rf=emisor.addNewRegimenFiscal();
		//rf.setRegimen("Régimen General de Ley Personas Morales");
		String regimen=getEmpresa().getRegimen();
		String[] regs=StringUtils.split(regimen, ';');
		for(String r:regs){
			RegimenFiscal rf=emisor.addNewRegimenFiscal();
			rf.setRegimen(r);
		}
		
		TUbicacionFiscal domicilioFiscal=emisor.addNewDomicilioFiscal();
		Conversiones.getTUbicacionFiscal(
					getEmpresa().getDireccion()
					, domicilioFiscal
					);		
	}
	
	public void registrarExpedidoEn(Comprobante cfd,Sucursal sucursal){
		Emisor emisor=cfd.getEmisor();
		TUbicacion ubicacion=emisor.addNewExpedidoEn();
		Conversiones.getTUbicacion(sucursal.getDireccion(),ubicacion);
	}
	
	public void registrarLugarDeExpedicion(Comprobante cfd,Sucursal sucursal){
		cfd.setLugarExpedicion(sucursal.getDireccion().getPais());
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.cfd.services.CFDSupport#configurarReceptor(mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante, com.luxsoft.siipap.model.core.Cliente)
	 */
	public void registrarReceptor(Comprobante cfd,Cliente cliente){
		Receptor receptor=cfd.addNewReceptor();
		receptor.setNombre(cliente.getNombre());
		receptor.setRfc(CFDUtils.limpiarRfc(cliente.getRfc()));
		Direccion direccion=cliente.getDireccionFiscal();
		if(!cliente.getClave().equals("1"))
			Conversiones.getTUbicacion(direccion,receptor.addNewDomicilio());
		else{
			TUbicacion ub=receptor.addNewDomicilio();
			ub.setPais("MEXICO");
		}
		
		if(getEmpresa().getTipoDeComprobante().equals(Empresa.TipoComprobante.CFDI) &&
				!(receptor.getRfc().equals("UCI840109JU0")) ){
			throw new RuntimeException("Solo se pueden generar comprobantes tipo CFDI");
		}
	}
	
	
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.cfd.services.CFDSupport#registrarSerieFolio(java.lang.String, mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante)
	 */
	@Transactional(propagation=Propagation.MANDATORY)
	public void registrarSerieFolio(String serie,Sucursal sucursal,Comprobante cfd) {
		FolioFiscal folio=getFolioFiscalDao().buscarFolio(sucursal, serie);
		cfd.setSerie(serie);
		cfd.setFolio(String.valueOf(folio.next()));
		cfd.setAnoAprobacion(folio.getAnoAprobacion());
		cfd.setNoAprobacion(new BigInteger(folio.getNoAprobacion().toString()));
				
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.cfd.services.CFDSupport#registrarComprobante(mx.gob.sat.cfd.x2.ComprobanteDocument, java.lang.String, java.lang.String)
	 */
	public ComprobanteFiscal registrarComprobante(ComprobanteDocument document,String tipo,String origen){
		ComprobanteFiscal comprobanteFiscal=new ComprobanteFiscal(document);
		comprobanteFiscal.setTipo(tipo);
		comprobanteFiscal.setOrigen(origen);	
		comprobanteFiscal.setNumeroDeCertificado(document.getComprobante().getNoCertificado());
		return comprobanteFiscal;
	}
	
	public String generarCadenaOriginal(ComprobanteDocument document){
		String cadenaOriginal=cadenaBuider.obtenerCadena(document);
		return cadenaOriginal;
	}
	
	public String registrarSelloDigital(ComprobanteDocument document){
		String cadena=generarCadenaOriginal(document);
		String sello= sellador.getSello(cadena);
		document.getComprobante().setSello(sello);
		try {
			byte[] encodedCert = Base64.encode(sellador.getCertificado().getEncoded());
				document.getComprobante().setCertificado(new String(encodedCert));
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
		//String certificado=System.getProperty("cfd.cer.num","00001000000102129215");
		document.getComprobante().setNoCertificado(getCertificadoDigital().getNumeroDeCertificado());
		return sello;
	}

	
	public void depuracionFinal(ComprobanteDocument document){
		//Configuracion final
		
		XmlCursor cursor=document.newCursor();
		
		if(cursor.toFirstChild()){
			cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance","schemaLocation","xsi")
			,cfdSchemaLocation );
			//cursor.setAttributeText(new QName("xmlns"),"http://prueba");
			//cursor.insertAttribute("http://prueba_2");
			
			QName name=cursor.getName();
			boolean isNs=cursor.isNamespace();
			System.out.println("NS: "+isNs);
			String ns=cursor.namespaceForPrefix("");
			System.out.println("NS: "+ns);
			System.out.println("Name: "+name.getNamespaceURI()+" Local part:  "+name.getLocalPart()+" Prefix: "+name.getPrefix()+ " Text: "+cursor.getTextValue());
			while(cursor.toNextAttribute()){
				name=cursor.getName();
				System.out.println("Name: "+name.getNamespaceURI()+" Local part:  "+name.getLocalPart()+" Prefix: "+name.getPrefix()+ " Text: "+cursor.getTextValue());
				
			}
			
			
		}	
		
	}

	public void validarDocumento(ComprobanteDocument document) {
		List<XmlValidationError> errores=CFDUtils.validar(document);
		if(errores.size()>0){
			StringBuffer buff=new StringBuffer();
			for(XmlValidationError e:errores){
				logger.info(e.getMessage());
				buff.append(e.getMessage()+"\n");
			}
			throw new CFDException("Datos para generar el comprobante fiscal (CFD) incorrectos "+buff.toString());
		}
	}

	@Transactional(propagation=Propagation.MANDATORY)
	public ComprobanteFiscal salvar(String xmlName,ComprobanteDocument docto,ComprobanteFiscal cf) throws ComprobanteFiscalCreationException{
		
		try {
			/*
			File destinoDir=new File(getCfdDirPath());
			if(!destinoDir.exists()){
				destinoDir.mkdir();
			}*/
			cf.setTotal(docto.getComprobante().getTotal());
			cf.setImpuesto(docto.getComprobante().getImpuestos().getTotalImpuestosTrasladados());
			cf.setEstado("1");
			cf.setRfc(docto.getComprobante().getReceptor().getRfc());
			
			cf.setTipoCfd(StringUtils.substring(
					docto.getComprobante().getTipoDeComprobante().toString(),0,1).toUpperCase());
			//cf.setCuentaPredial(docto.getComprobante().getC)
			File destino=new File(getCfdDirPath(),xmlName+".xml");
			
			//cf.setXmlPath(destino.toURI().toURL().toString());
			cf.setXmlPath(xmlName+".xml");
			registrarBitacora(cf);
			cf.getLog().setCreado(docto.getComprobante().getFecha().getTime());
			cf=(ComprobanteFiscal)hibernateTemplate.merge(cf);
			
			XmlOptions options = new XmlOptions();
			options.setCharacterEncoding("UTF-8");
	        options.put( XmlOptions.SAVE_INNER );
	        options.put( XmlOptions.SAVE_PRETTY_PRINT );
	        options.put( XmlOptions.SAVE_AGGRESSIVE_NAMESPACES );
	        options.put( XmlOptions.SAVE_USE_DEFAULT_NAMESPACE );
	        options.put(XmlOptions.SAVE_NAMESPACES_FIRST);
			Map suggestedPrefix=new HashMap();
			suggestedPrefix.put("", "");
			//
			//OutputStream ous=new FileOutputStream(destino);
			//BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destino),"UTF8"));
			docto.save(destino,options);
			return cf;
		} catch (IOException e) {
			throw new ComprobanteFiscalCreationException("",e);
		}
	}
	
	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	private void registrarBitacora(ComprobanteFiscal bean){
		Date time=new Date();		
		String user=KernellSecurity.instance().getCurrentUserName();	
		//String ip=KernellSecurity.getIPAdress();
		//String mac=KernellSecurity.getMacAdress();
		
		bean.getLog().setModificado(time);
		bean.getLog().setUpdateUser(user);
		//bean.getAddresLog().setUpdatedIp(ip);
		//bean.getAddresLog().setUpdatedMac(mac);
		
		
		if(bean.getId()==null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			//bean.getAddresLog().setCreatedIp(ip);
			//bean.getAddresLog().setUpdatedMac(mac);
		}
		
	}

	

	/**
	 * Regresa la empresa registrada en el sistema
	 * 
	 */
	private Empresa getEmpresa() {
		if(empresa==null){
			return (Empresa)getHibernateTemplate().get(Empresa.class, 1L);
		}
		return empresa;
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public String getCfdDirPath() {
		if(cfdDirPath==null){
			
			cfdDirPath=System.getProperty("cfd.dir.path");
		}
		return cfdDirPath;
	}

	public FolioFiscalDao getFolioFiscalDao() {
		return folioFiscalDao;
	}

	public void setFolioFiscalDao(FolioFiscalDao folioFiscalDao) {
		this.folioFiscalDao = folioFiscalDao;
	}

	

	public CadenaOriginalBuilder getCadenaBuider() {
		return cadenaBuider;
	}

	public void setCadenaBuider(CadenaOriginalBuilder cadenaBuider) {
		this.cadenaBuider = cadenaBuider;
	}
	

	public SelladorDigital getSellador() {
		return sellador;
	}

	public void setSellador(SelladorDigital sellador) {
		this.sellador = sellador;
	}

	
	public void setCfdVersion(String cfdVersion) {
		this.cfdVersion = cfdVersion;
	}	
	
	public void setCfdSchemaLocation(String cfdSchemaLocation) {
		this.cfdSchemaLocation = cfdSchemaLocation;
	}
	
	
	private CertificadoDeSelloDigital certificadoDigital;
	
	public CertificadoDeSelloDigital getCertificadoDigital(){
		if(certificadoDigital==null){
			certificadoDigital=certificadoDeSelloDigitalDao.get(1L);
		}
		return certificadoDigital;
	}
	public void setCertificadoDeSelloDigitalDao(
			CertificadoDeSelloDigitalDao certificadoDeSelloDigitalDao) {
		this.certificadoDeSelloDigitalDao = certificadoDeSelloDigitalDao;
	}
	
	public static void main(String[] args) {
		Calendar c=Calendar.getInstance();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		XmlDateTime xmlDateTime = XmlDateTime.Factory.newInstance();
		//xmlDateTime.setCalendarValue(Calendar.getInstance());
		xmlDateTime.setStringValue(df.format(c.getTime()));
		System.out.println(xmlDateTime.xmlText());
		
		GDateBuilder gdb = new GDateBuilder(xmlDateTime.getDateValue());
		gdb.normalize();
		xmlDateTime.setGDateValue(gdb.toGDate());

		System.out.println(xmlDateTime.xmlText());
		
	}

}
