package com.luxsoft.sw3.cfd;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import mx.gob.sat.cfd.x2.ComprobanteDocument;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Receptor;
import mx.gob.sat.cfd.x2.TUbicacion;
import mx.gob.sat.cfd.x2.TUbicacionFiscal;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;

import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.cfd.model.Conversiones;
import com.luxsoft.sw3.cfd.services.CadenaOriginalBuilder;
import com.luxsoft.sw3.cfd.services.CadenaOriginalBuilderImpl;
import com.luxsoft.sw3.cfd.services.CadenaOriginalBuilderV2Impl;

public class CFDUtils {
	
	public static String limpiarRfc(String rfc){
		String res=StringUtils.trimToEmpty(rfc);
		res=StringUtils.stripToEmpty(res);
		res=StringUtils.deleteWhitespace(res);
		return res;
	}
	
	public static String limpiarCodigoPostal(String cp){
		String res=StringUtils.trimToEmpty(cp);
		res=StringUtils.stripToEmpty(res);
		res=StringUtils.deleteWhitespace(res);
		return res;
	}
	
	public static List validar(final XmlObject node){
		final XmlOptions options=new XmlOptions();
		final List errors=new ArrayList();
		options.setErrorListener(errors);
		node.validate(options);
		for(Object o:errors){
			System.out.println(o.getClass().getName());
		}
		return errors;
		
	} 
	
	
	public static List validarClienteParaCFD(Cliente cliente){
		ComprobanteDocument doc=ComprobanteDocument.Factory.newInstance();
		Comprobante cfd=doc.addNewComprobante();
		Receptor receptor=cfd.addNewReceptor();
		
		receptor.setNombre(cliente.getNombre());
		receptor.setRfc(CFDUtils.limpiarRfc(cliente.getRfc()));
		Direccion direccion=cliente.getDireccionFiscal();
		Conversiones.getTUbicacion(direccion,receptor.addNewDomicilio());
		
		List errores=validar(receptor);
		List<String> res=new ArrayList<String>();
		for(Object o:errores){
			XmlValidationError err=(XmlValidationError)o;
			String pattern="Campo: {0}  Valor: {1}  Desc: {2}";
			res.add(MessageFormat.format(pattern, err.getCursorLocation().getName(),err.getCursorLocation().getTextValue(),err.getMessage()));
			
		}
		return res;
		
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
	
	private static CadenaOriginalBuilder builder_2=new CadenaOriginalBuilderImpl();
	private static CadenaOriginalBuilder builder_22=new CadenaOriginalBuilderV2Impl();
	
	
	public static String generarCadenaOrignal(ComprobanteDocument cfd){
		String version=cfd.getComprobante().getVersion();
		if("2.2".equals(version)){
			return builder_22.obtenerCadena(cfd);
		}else
			return builder_2.obtenerCadena(cfd);
		
	}
		
	public static void main(String[] args) {
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave("I050266");
		validarClienteParaCFD(c);
	}
	

}
