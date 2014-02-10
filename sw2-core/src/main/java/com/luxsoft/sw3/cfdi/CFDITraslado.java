package com.luxsoft.sw3.cfdi;


import static com.luxsoft.sw3.cfdi.CFDIUtils.getFecha;
import static com.luxsoft.sw3.cfdi.CFDIUtils.registrarDatosDeEmisor;
import static com.luxsoft.sw3.cfdi.CFDIUtils.registrarReceptor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.security.cert.CertificateEncodingException;
import java.util.Date;
import java.util.List;

import mx.gob.sat.cfd.x3.ComprobanteDocument;
import mx.gob.sat.cfd.x3.TUbicacion;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Conceptos;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Impuestos;


import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Receptor;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.TipoDeComprobante;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlOptions;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;


import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.inventarios.model.TrasladoDet;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.cfd.CFDUtils;
import com.luxsoft.sw3.cfd.dao.FolioFiscalDao;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscalCreationException;
import com.luxsoft.sw3.cfd.model.FolioFiscal;
import com.luxsoft.sw3.cfdi.model.CFDI;
/**
 * Genera CFDI para Nota de cargo
 * 
 * @author Ruben Cancino 
 *
 */
@Service("cfdiTraslado")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class CFDITraslado implements InitializingBean,ITraslado{
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private FolioFiscalDao folioFiscalDao;
	
	@Autowired
	private CFDICadenaOriginalBuilder cadenaBuilder;
	
	private Empresa empresa;
	
	@Autowired
	private CFDISellador sellador;
	
	@Transactional(propagation=Propagation.REQUIRED)
	public CFDI generar(Traslado tps){
		Assert.isTrue(tps.getTipo().equals("TPS"),"El CFDI es sobre TPS");
		Assert.isNull(tps.getCfdi(),"Ya existe un CFDI para el Traslado: "+tps.getDocumento());
		tps=(Traslado)hibernateTemplate.load(Traslado.class, tps.getId());
		//Preparamos el cfdi del SAT
		final ComprobanteDocument document=ComprobanteDocument.Factory.newInstance();
		Comprobante cfdi=document.addNewComprobante();
		CFDIUtils.depurar(document);
		cfdi.setVersion("3.2");
		cfdi.setFecha(getFecha());
		cfdi.setTipoDeComprobante(TipoDeComprobante.INGRESO);
		cfdi.setFormaDePago("PAGO EN UNA SOLA EXHIBICION");
		cfdi.setMetodoDePago("NO IDENTIFICADO");
		
		cfdi.setMoneda(MonedasUtils.PESOS.getCurrencyCode());
		cfdi.setTipoCambio(BigDecimal.ONE.toString());
		cfdi.setTipoDeComprobante(TipoDeComprobante.INGRESO);
		
		
		// Emisor,regimen fiscal,domicilioFiscal
		registrarDatosDeEmisor(cfdi, getEmpresa());
		cfdi.setLugarExpedicion(tps.getSucursal().getDireccion().getPais());	
		
		// Expedido en
		TUbicacion domicilio=cfdi.getEmisor().addNewExpedidoEn();
		Direccion direccion=tps.getSucursal().getDireccion();
		domicilio.setCalle(StringUtils.defaultString(direccion.getCalle()));
		domicilio.setCodigoPostal(StringUtils.defaultString(direccion.getCp()));
		domicilio.setColonia(StringUtils.defaultString(direccion.getColonia()));
		//domicilio.setEstado(StringUtils.defaultString(direccion.getEstado()));
		domicilio.setEstado(StringUtils.defaultIfEmpty(direccion.getEstado(),"."));
		domicilio.setMunicipio(StringUtils.defaultString(direccion.getMunicipio()));
		domicilio.setNoExterior(StringUtils.defaultIfEmpty(direccion.getNumero(),"."));
		domicilio.setNoInterior(StringUtils.defaultIfEmpty(direccion.getNumeroInterior(),"."));
		domicilio.setPais(CFDUtils.limpiarCodigoPostal(direccion.getPais()));
		
		
		//Receptor
		Receptor receptor=cfdi.addNewReceptor();
		receptor.setNombre("PUBLICO EN GENERAL");
		receptor.setRfc("XAXX010101000");
		
		//Conceptos
		generarConceptos(cfdi, tps);
		
		
		//Totales
		cfdi.setTotal(BigDecimal.ZERO);
		cfdi.setSubTotal(BigDecimal.ZERO);
		Impuestos impuestos=cfdi.addNewImpuestos();
		impuestos.setTotalImpuestosTrasladados(BigDecimal.ZERO);
		
		
		Sucursal oficinas=(Sucursal)getHibernateTemplate().get(Sucursal.class,1L);
		registrarSerieFolio("TPS", oficinas, cfdi);
		//Sello digital y datos relacionados con el certificado digital
		String cadena=cadenaBuilder.generarCadena(document);
		registrarSelloDigital(document,cadena);
		
		CFDI comprobanteFiscal=new CFDI(document);
		comprobanteFiscal.setTipo("TRASLADO");
		comprobanteFiscal.setOrigen(tps.getId());	
		comprobanteFiscal.setNumeroDeCertificado(document.getComprobante().getNoCertificado());
		comprobanteFiscal.setCadenaOriginal(cadena);
		CFDIUtils.validarDocumento(document);
		comprobanteFiscal=salvar(document,comprobanteFiscal);
		
		
		
		return comprobanteFiscal;
	}
	
	private void generarConceptos(Comprobante cfd, com.luxsoft.siipap.inventarios.model.Traslado tps){
		Conceptos conceptos=cfd.addNewConceptos();
		for(TrasladoDet det:tps.getPartidas()){
			Concepto c=conceptos.addNewConcepto();
			c.setCantidad(BigDecimal.valueOf(det.getCantidadEnUnidad()).abs());
			c.setUnidad("No Aplica");
			c.setDescripcion(det.getProducto().getDescripcion());
			c.setValorUnitario(BigDecimal.valueOf(0));
			c.setImporte(BigDecimal.valueOf(0).setScale(2));
		}
		
	}
	
	
	//@Transactional(propagation=Propagation.MANDATORY)
	public void registrarSerieFolio(String serie,Sucursal sucursal,Comprobante cfd) {
		FolioFiscal folio=getFolioFiscalDao().buscarFolio(sucursal, serie);
		cfd.setSerie(serie);
		cfd.setFolio(String.valueOf(folio.next()));
	}
	
	public String registrarSelloDigital(ComprobanteDocument document,String cadena){
		
		String sello= sellador.generarSello(cadena);
		document.getComprobante().setSello(sello);
		try {
			byte[] encodedCert = Base64.encode(sellador.getCertificado().getEncoded());
				document.getComprobante().setCertificado(new String(encodedCert));
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
		document.getComprobante().setNoCertificado(empresa.getNumeroDeCertificado());
		return sello;
	}

	@Transactional(propagation=Propagation.MANDATORY)
	CFDI salvar(ComprobanteDocument document,CFDI cf) throws ComprobanteFiscalCreationException{
		
		try {
			Comprobante comprobante=document.getComprobante();
			cf.setTotal(comprobante.getTotal());
			cf.setImpuesto(comprobante.getImpuestos().getTotalImpuestosTrasladados());
			cf.setEstado("1");
			cf.setRfc(comprobante.getReceptor().getRfc());
			
			cf.setTipoCfd(StringUtils.substring(
					comprobante.getTipoDeComprobante().toString(),0,1).toUpperCase());
			
			//File destino=new File(getCfdDirPath(),xmlName+".xml");
			
			registrarBitacora(cf);
			cf.getLog().setCreado(comprobante.getFecha().getTime());
			
			XmlOptions options = new XmlOptions();
			options.setCharacterEncoding("UTF-8");
	        options.put( XmlOptions.SAVE_INNER );
	        options.put( XmlOptions.SAVE_PRETTY_PRINT );
	        options.put( XmlOptions.SAVE_AGGRESSIVE_NAMESPACES );
	        options.put( XmlOptions.SAVE_USE_DEFAULT_NAMESPACE );
	        options.put(XmlOptions.SAVE_NAMESPACES_FIRST);
			
			ByteArrayOutputStream os=new ByteArrayOutputStream();
			document.save(os, options);
			cf.setXml(os.toByteArray());
			cf.setXmlFilePath(cf.getSerie()+"-"+cf.getFolio()+".xml");
			//Salvamos el xml en la base de datos
			
			System.out.println(CFDIUtils.validarPersistencia(cf));
			cf=(CFDI)hibernateTemplate.merge(cf);
			
			String path=System.getProperty("cfd.dir.path")+"/cfdi/"+cf.getXmlFilePath();
			File xmlFile=new File(path);
			document.save(xmlFile,options);
			return cf;
		} catch (Exception e) {
			throw new ComprobanteFiscalCreationException("",e);
		}
	}
	
	//@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	private void registrarBitacora(CFDI bean){
		Date time=new Date();		
		String user=KernellSecurity.instance().getCurrentUserName();
		bean.getLog().setModificado(time);
		bean.getLog().setUpdateUser(user);
		if(bean.getId()==null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
		}
	}
	
	
	
	
	
	/**
	 * Regresa la empresa registrada en el sistema
	 * 
	 */
	private Empresa getEmpresa() {
		if(empresa==null){
			empresa= (Empresa)getHibernateTemplate().find("from Empresa e").get(0);
		}
		return empresa;
	}
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}


	public FolioFiscalDao getFolioFiscalDao() {
		return folioFiscalDao;
	}

	public void setFolioFiscalDao(FolioFiscalDao folioFiscalDao) {
		this.folioFiscalDao = folioFiscalDao;
	}
	

	public CFDICadenaOriginalBuilder getCadenaBuilder() {
		return cadenaBuilder;
	}

	public void setCadenaBuilder(CFDICadenaOriginalBuilder cadenaBuilder) {
		this.cadenaBuilder = cadenaBuilder;
	}
	public void setSellador(CFDISellador sellador) {
		this.sellador = sellador;
	}

	public void afterPropertiesSet() throws Exception {
		//Inicializamos el proveedor BouncyCastle de criptografia
		Assert.notNull(hibernateTemplate, "Se requiere registrar hibernateTemplate");
		Assert.notNull(sellador, "Se requiere registrar un sellador criptografico");
		System.out.println("CFDIFactura inicializada...");
	}

}
