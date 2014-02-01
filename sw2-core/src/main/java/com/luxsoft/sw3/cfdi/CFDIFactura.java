package com.luxsoft.sw3.cfdi;


import static com.luxsoft.sw3.cfdi.CFDIUtils.getFecha;
import static com.luxsoft.sw3.cfdi.CFDIUtils.registrarDatosDeEmisor;
import static com.luxsoft.sw3.cfdi.CFDIUtils.registrarReceptor;
import mx.gob.sat.cfd.x3.ComprobanteDocument;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Conceptos;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Impuestos;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Impuestos.Traslados;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Impuestos.Traslados.Traslado;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Receptor;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.TipoDeComprobante;
import mx.gob.sat.cfd.x3.TUbicacion;

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

import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.cfd.CFDUtils;
import com.luxsoft.sw3.cfd.dao.FolioFiscalDao;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscalCreationException;
import com.luxsoft.sw3.cfd.model.FolioFiscal;
import com.luxsoft.sw3.cfdi.model.CFDI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.cert.CertificateEncodingException;
import java.text.MessageFormat;
import java.util.Date;
/**
 * Genera CFDI para Venta
 * 
 * @author Ruben Cancino 
 *
 */
@Service("cfdiFactura")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class CFDIFactura implements InitializingBean,IFactura{
	
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
	public CFDI generar(Venta venta){
		Assert.notNull(getEmpresa().getTipoDeComprobante(),"No esta definido el tipo de comprobante en: Empresa.tipoDeCombrobante");
		Assert.isTrue(getEmpresa().getTipoDeComprobante().equals(Empresa.TipoComprobante.CFDI),"No esta configudada la emrpesa para trabajar con CFDI");
		
		//Preparamos el cfdi del SAT
		final ComprobanteDocument document=ComprobanteDocument.Factory.newInstance();
		Comprobante cfdi=document.addNewComprobante();
		CFDIUtils.depurar(document);
		cfdi.setVersion("3.2");
		cfdi.setFecha(getFecha());
		cfdi.setTipoDeComprobante(TipoDeComprobante.INGRESO);
		cfdi.setFormaDePago("PAGO EN UNA SOLA EXHIBICION");
		
	
	
		if(venta.getFormaDePago().equals(FormaDePago.DEPOSITO) || venta.isContraEntrega()){
	    	cfdi.setMetodoDePago("NO IDENTIFICADO");
		}else if(venta.getFormaDePago().equals(FormaDePago.CHECKPLUS) || venta.getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO) ){
			cfdi.setMetodoDePago("CHEQUE");
		}
		else
		   cfdi.setMetodoDePago(venta.getFormaDePago().name());
		
		if(StringUtils.isNotBlank(venta.getComentarioCancelacionDBF()) ){
			if(!(venta.getFormaDePago().equals(FormaDePago.DEPOSITO) || venta.isContraEntrega()))
			    cfdi.setNumCtaPago(venta.getComentarioCancelacionDBF());
		}
			
		cfdi.setMoneda(venta.getMoneda().getCurrencyCode());
		cfdi.setTipoCambio(BigDecimal.valueOf(venta.getTc()).toString());
		cfdi.setTipoDeComprobante(TipoDeComprobante.INGRESO);
		
		
		// Emisor,regimen fiscal,domicilioFiscal
		registrarDatosDeEmisor(cfdi, getEmpresa());
		cfdi.setLugarExpedicion(venta.getSucursal().getDireccion().getPais());		
		
		// Expedido en
		TUbicacion domicilio=cfdi.getEmisor().addNewExpedidoEn();
		Direccion direccion=venta.getSucursal().getDireccion();
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
		Receptor rec=registrarReceptor(cfdi, venta.getCliente());
		rec.setNombre(venta.getNombre());
		
		//Conceptos
		Conceptos conceptos=cfdi.addNewConceptos();
		for(VentaDet det:venta.getPartidas()){
			Concepto c=conceptos.addNewConcepto();
			c.setCantidad(new BigDecimal(det.getCantidadEnUnidad()).multiply(BigDecimal.valueOf(-1)).setScale(3,RoundingMode.HALF_EVEN));
			c.setUnidad(det.getUnidad().getNombre());
			c.setNoIdentificacion(det.getClave());
			c.setDescripcion(det.getDescripcion());
			c.setValorUnitario(det.getPrecio().setScale(2));
			c.setImporte(det.getImporte().setScale(2));
			if(venta.getClave().equals("1")){
				c.setValorUnitario(det.getPrecioConIva());
				c.setImporte(det.getImporteConIva());
			}
		}
		
		//Totales
		cfdi.setTotal(venta.getTotal());
		cfdi.setSubTotal(venta.getImporteBruto());
		Impuestos impuestos=cfdi.addNewImpuestos();
		if(venta.getClave().equals("1")){
			
			CantidadMonetaria subTotal=CantidadMonetaria.pesos(venta.getImporteBruto());
			subTotal.multiply(1d+MonedasUtils.IVA.doubleValue());
			cfdi.setSubTotal(subTotal.amount());
			CantidadMonetaria  desc=CantidadMonetaria.pesos(venta.getImporteDescuento());
			desc=desc.multiply(1d+MonedasUtils.IVA.doubleValue());
			cfdi.setDescuento(desc.amount());
		}else{
			
			impuestos.setTotalImpuestosTrasladados(venta.getImpuesto());
			Traslados traslados=impuestos.addNewTraslados();
			Traslado traslado=traslados.addNewTraslado();
			traslado.setImpuesto(Traslado.Impuesto.IVA);
			traslado.setImporte(venta.getImpuesto());
			traslado.setTasa(MonedasUtils.IVA.multiply(BigDecimal.valueOf(100)));
			cfdi.setDescuento(venta.getImporteDescuento());
		}
		
		
		registrarSerieFolio(getSerie(venta), venta.getSucursal(), cfdi);
		//Sello digital y datos relacionados con el certificado digital
		String cadena=cadenaBuilder.generarCadena(document);
		registrarSelloDigital(document,cadena);
		
		CFDI comprobanteFiscal=new CFDI(document);
		comprobanteFiscal.setTipo("FACTURA");
		comprobanteFiscal.setOrigen(venta.getId());	
		comprobanteFiscal.setNumeroDeCertificado(document.getComprobante().getNoCertificado());
		comprobanteFiscal.setCadenaOriginal(cadena);
		
		comprobanteFiscal=salvar(document,comprobanteFiscal);
		
		//Actualizar el folio en la venta
		venta.setDocumento(new Long(comprobanteFiscal.getFolio()));
		getHibernateTemplate().update(venta);
		return comprobanteFiscal;
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
	
	private String getSerie(Venta venta) {
		
		String pattern="{0}{1}{2}";
		String sucursal=venta.getSucursal().getNombre();
		sucursal=StringUtils.substring(sucursal, 0, 2);
		String serie=MessageFormat.format(pattern
				,sucursal
				,"FAC"
				,venta.getOrigen().name()
				);
		return serie;
	}
	
	/**
	 * Regresa la empresa registrada en el sistema
	 * 
	 */
	public Empresa getEmpresa() {
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
	
	

	public CFDISellador getSellador() {
		return sellador;
	}

	public void setSellador(CFDISellador sellador) {
		this.sellador = sellador;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(hibernateTemplate, "Se requiere registrar hibernateTemplate");
		Assert.notNull(sellador, "Se requiere registrar un sellador criptografico");
	}

}
