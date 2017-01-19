package com.luxsoft.sw3.cfdi;


import static com.luxsoft.sw3.cfdi.CFDIUtils.getFecha;
import static com.luxsoft.sw3.cfdi.CFDIUtils.registrarDatosDeEmisor;
import static com.luxsoft.sw3.cfdi.CFDIUtils.registrarReceptor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.security.cert.CertificateEncodingException;
import java.util.Date;

import mx.gob.sat.cfd.x3.ComprobanteDocument;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Conceptos;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Impuestos;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Impuestos.Traslados;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Impuestos.Traslados.Traslado;
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

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.util.MonedasUtils;
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
@Service("cfdiNotaDeCargo")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class CFDINotaDeCargo implements InitializingBean,INotaDeCargo{
	
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
	public CFDI generar(NotaDeCargo nota){
		
		//Preparamos el cfdi del SAT
		final ComprobanteDocument document=ComprobanteDocument.Factory.newInstance();
		Comprobante cfdi=document.addNewComprobante();
		CFDIUtils.depurar(document);
		cfdi.setVersion("3.2");
		cfdi.setFecha(getFecha());
		cfdi.setTipoDeComprobante(TipoDeComprobante.INGRESO);
		cfdi.setFormaDePago("PAGO EN UNA SOLA EXHIBICION");
		
		System.out.println("Cliente:  "+nota.getCliente()+"  Clave:  "+nota.getClave() +"----"+ nota.getCliente().getId());
		
		cfdi.setMetodoDePago("NA 98");
		
		if(nota.getCliente().getId()==5352L){
			
			cfdi.setMetodoDePago("NA");
		}if(nota.getCliente().getId()==304945L){
			
			cfdi.setMetodoDePago("03");
		}
	
		cfdi.setMoneda(nota.getMoneda().getCurrencyCode());
		cfdi.setTipoCambio(BigDecimal.valueOf(nota.getTc()).toString());
		cfdi.setTipoDeComprobante(TipoDeComprobante.INGRESO);
		
		
		// Emisor,regimen fiscal,domicilioFiscal
		registrarDatosDeEmisor(cfdi, getEmpresa());
		cfdi.setLugarExpedicion(nota.getSucursal().getDireccion().getPais());
		
		//Receptor
		Receptor rec=registrarReceptor(cfdi, nota.getCliente());
		rec.setNombre(nota.getNombre());
		
		//Conceptos
		generarConceptos(cfdi, nota);
		
		
		//Totales
		cfdi.setTotal(nota.getTotal());
		cfdi.setSubTotal(nota.getImporte());
		Impuestos impuestos=cfdi.addNewImpuestos();
		
		if(nota.getClave().equals("1")){			
			cfdi.setSubTotal(cfdi.getTotal());
		}else{			
			impuestos.setTotalImpuestosTrasladados(nota.getImpuesto());			
			Traslados traslados=impuestos.addNewTraslados();
			Traslado traslado=traslados.addNewTraslado();
			traslado.setImpuesto(Traslado.Impuesto.IVA);
			traslado.setImporte(nota.getImpuesto());
			traslado.setTasa(MonedasUtils.IVA.multiply(BigDecimal.valueOf(100)));
		}
		
		Sucursal oficinas=(Sucursal)getHibernateTemplate().get(Sucursal.class,1L);
		String tipoSerie;
		OrigenDeOperacion origen=nota.getOrigen();
		switch (origen) {
		case CRE:
			tipoSerie=System.getProperty("cfd.ncargo_cre.serie", "TACARCRE");
			break;
		case CAM:
			tipoSerie=System.getProperty("cfd.ncargo_cam.serie", "TACARCRE");
			break;
		case MOS:
			tipoSerie=System.getProperty("cfd.ncargo_mos.serie", "TACARCRE");
			break;
		default:
			tipoSerie=System.getProperty("cfd.ncargo_cre.serie", "TACARCRE");
			break;
		}

		
		registrarSerieFolio(tipoSerie, oficinas, cfdi);
		//Sello digital y datos relacionados con el certificado digital
		String cadena=cadenaBuilder.generarCadena(document);
		registrarSelloDigital(document,cadena);
		
		CFDI comprobanteFiscal=new CFDI(document);
		comprobanteFiscal.setTipo("NOTA_CARGO");
		comprobanteFiscal.setOrigen(nota.getId());	
		comprobanteFiscal.setNumeroDeCertificado(document.getComprobante().getNoCertificado());
		comprobanteFiscal.setCadenaOriginal(cadena);
		CFDIUtils.validarDocumento(document);
		comprobanteFiscal=salvar(document,comprobanteFiscal);
		
		nota.setDocumento(new Long(comprobanteFiscal.getFolio()));
		getHibernateTemplate().update(nota);
		
		return comprobanteFiscal;
	}
	
	private void generarConceptos(Comprobante cfd, NotaDeCargo nota){
		Conceptos conceptos=cfd.addNewConceptos();
		if(nota.getConceptos().isEmpty()){
			Concepto c=conceptos.addNewConcepto();
			c.setCantidad(BigDecimal.ONE);
			c.setUnidad("No Aplica");
			c.setDescripcion(nota.getComentario());
			c.setValorUnitario(nota.getImporte().setScale(2));
			c.setImporte(nota.getImporte().setScale(2));
		}else{
			for(NotaDeCargoDet det:nota.getConceptos()){
				Concepto c=conceptos.addNewConcepto();
				c.setCantidad(BigDecimal.ONE);
				c.setUnidad("No Aplica");
				c.setDescripcion(det.getComentario());
				c.setValorUnitario(det.getImporte().setScale(2));
				c.setImporte(det.getImporte().setScale(2));
				
			}
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
