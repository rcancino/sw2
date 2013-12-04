package com.luxsoft.sw3.cfdi;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;


import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.core.Cliente;

import com.luxsoft.sw3.cfd.CFDUtils;
import com.luxsoft.sw3.cfd.model.CFDException;

import mx.gob.sat.cfd.x3.ComprobanteDocument;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Emisor;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Emisor.RegimenFiscal;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Receptor;
import mx.gob.sat.cfd.x3.TUbicacion;
import mx.gob.sat.cfd.x3.TUbicacionFiscal;

public class CFDIUtils {
	
	public static void depurar(ComprobanteDocument document){
		XmlCursor cursor=document.newCursor();
		
		if(cursor.toFirstChild()){
			
			QName qname=new QName("http://www.w3.org/2001/XMLSchema-instance","schemaLocation","xsi");
			cursor.setAttributeText(qname,"http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv32.xsd" );
			cursor.toNextToken();
			cursor.insertNamespace("cfdi", "http://www.sat.gob.mx/cfd/3");
		}	
	}
	
	public static Calendar getFecha(){
		return getFecha(new Date());
	}
	
	public static Calendar getFecha(Date fecha){
		Calendar c=Calendar.getInstance();
		c.setTime(fecha);
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		XmlDateTime xmlDateTime = XmlDateTime.Factory.newInstance();
		xmlDateTime.setStringValue(df.format(c.getTime()));
		return xmlDateTime.getCalendarValue();
	}
	
	public static void registrarDatosDeEmisor(Comprobante cfd,Empresa empresa){
		Emisor emisor=cfd.addNewEmisor();
		emisor.setNombre(empresa.getNombre());
		emisor.setRfc(empresa.getRfc());
		String regimen=empresa.getRegimen();
		String[] regs=StringUtils.split(regimen, ';');
		for(String r:regs){
			RegimenFiscal rf=emisor.addNewRegimenFiscal();
			rf.setRegimen(r);
		}
		TUbicacionFiscal domicilioFiscal=emisor.addNewDomicilioFiscal();
		getTUbicacionFiscal(
					empresa.getDireccion()
					, domicilioFiscal
					);		
	}
	
	public static Receptor registrarReceptor(Comprobante cfd,Cliente cliente){
		Receptor receptor=cfd.addNewReceptor();
		receptor.setNombre(cliente.getNombre());
		receptor.setRfc(CFDUtils.limpiarRfc(cliente.getRfc()));
		Direccion direccion=cliente.getDireccionFiscal();
		if(!cliente.getClave().equals("1"))
			getTUbicacion(direccion,receptor.addNewDomicilio());
		else{
			TUbicacion ub=receptor.addNewDomicilio();
			ub.setPais("MEXICO");
		}
		return receptor;
	}
	
	public static TUbicacion getTUbicacion(final Direccion direccion,TUbicacion domicilio){
		domicilio.setCalle(StringUtils.defaultString(direccion.getCalle()));
		domicilio.setCodigoPostal(StringUtils.defaultString(direccion.getCp()));
		domicilio.setColonia(StringUtils.defaultString(direccion.getColonia()));
		//domicilio.setEstado(StringUtils.defaultString(direccion.getEstado()));
		domicilio.setEstado(StringUtils.defaultIfEmpty(direccion.getEstado(),"."));
		domicilio.setMunicipio(StringUtils.defaultString(direccion.getMunicipio()));
		domicilio.setNoExterior(StringUtils.defaultIfEmpty(direccion.getNumero(),"."));
		domicilio.setNoInterior(StringUtils.defaultIfEmpty(direccion.getNumeroInterior(),"."));
		domicilio.setPais(CFDUtils.limpiarCodigoPostal(direccion.getPais()));
		return domicilio;
	}
	
	
	public static TUbicacionFiscal getTUbicacionFiscal(final Direccion direccion,final TUbicacionFiscal domicilio){
		domicilio.setCalle(direccion.getCalle());
		domicilio.setCodigoPostal(direccion.getCp());
		domicilio.setColonia(direccion.getColonia());
		domicilio.setEstado(direccion.getEstado());
		domicilio.setMunicipio(direccion.getMunicipio());
		domicilio.setNoExterior(direccion.getNumero());
		domicilio.setPais(CFDUtils.limpiarCodigoPostal(direccion.getPais()));
		return domicilio;
	}
	
	public static void validarDocumento(ComprobanteDocument document) {
		List<XmlValidationError> errores=validar(document);
		if(errores.size()>0){
			StringBuffer buff=new StringBuffer();
			for(XmlValidationError e:errores){
				System.out.println("Error validacion: "+e.getMessage());
				buff.append(e.getMessage()+"\n");
			}
			throw new CFDException("Datos para generar el comprobante fiscal (CFD) incorrectos "+buff.toString());
		}
	}
	
	private static List validar(final XmlObject node){
		final XmlOptions options=new XmlOptions();
		final List errors=new ArrayList();
		options.setErrorListener(errors);
		node.validate(options);
		for(Object o:errors){
			System.out.println(o.getClass().getName());
		}
		return errors;
		
	}
	
	public static String validarPersistencia(Object bean){
		final Class clazz=bean.getClass();
		final ClassValidator validator=new ClassValidator(clazz);
		final InvalidValue[] invalid=validator.getInvalidValues(bean);
		StringBuffer buffer=new StringBuffer();
		for(InvalidValue iv:invalid){
			String propName=iv.getPropertyName();
			buffer.append(propName+ " Error:"+iv.getMessage());
		}
		return buffer.toString();
	}
	
	public static String getDireccionEnFormatoEstandar(TUbicacionFiscal u){
		String pattern="{0} {1} {2} {3}" +
				" {4} {5} {6}" +
				" {7} {8}";
		//StringUtils.
		return MessageFormat.format(pattern 
				,u.getCalle() !=null?u.getCalle():""
				,(u.getNoExterior()!=null && !u.getNoExterior().equals(".") )?"NO."+u.getNoExterior():""
				,(u.getNoInterior()!=null && !u.getNoInterior().equals(".") )?"INT."+u.getNoInterior():""
				,u.getColonia()!=null?","+u.getColonia():""
				,u.getCodigoPostal() !=null?","+u.getCodigoPostal():""
				,u.getMunicipio()!=null?","+u.getMunicipio():""
				,u.getLocalidad()!=null?","+u.getLocalidad():""
				,u.getEstado()!=null?","+u.getEstado()+",":""
				,u.getPais()!=null?u.getPais():""
				);
	}
	
	public static String getDireccionEnFormatoEstandar(TUbicacion u){
		String pattern="{0} {1} {2} {3}" +
				" {4} {5} {6}" +
				" {7} {8}";
		//StringUtils.
		return MessageFormat.format(pattern 
				,u.getCalle() !=null?u.getCalle():""
				,(u.getNoExterior()!=null && !u.getNoExterior().equals(".") )?"NO."+u.getNoExterior():""
				,(u.getNoInterior()!=null && !u.getNoInterior().equals(".") )?"INT."+u.getNoInterior():""
				,u.getColonia()!=null?","+u.getColonia():""
				,u.getCodigoPostal() !=null?","+u.getCodigoPostal():""
				,u.getMunicipio()!=null?","+u.getMunicipio():""
				,u.getLocalidad()!=null?","+u.getLocalidad():""
				,u.getEstado()!=null?","+u.getEstado()+",":""
				,u.getPais()!=null?u.getPais():""
				);
	}
	
	/*
	public static void depuracionFinal(ComprobanteDocument document){
		//Configuracion final
		
		XmlCursor cursor=document.newCursor();
		if(cursor.toFirstChild()){
			cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance","schemaLocation","xsi")
			,"http://www.sat.gob.mx/cfd/2 http://www.sat.gob.mx/sitio_internet/cfd/2/cfdv22.xsd");
			
		}	
		
	}
	*/
}
