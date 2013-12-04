package com.luxsoft.sw3.cfd.model;

import org.apache.commons.lang.StringUtils;

import mx.gob.sat.cfd.x2.TUbicacion;
import mx.gob.sat.cfd.x2.TUbicacionFiscal;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Receptor;

import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.sw3.cfd.CFDUtils;

/**
 * Clase de utilerias para la conversion de algunas entidades de SIIPAP
 *  a entidades de CFD segun SAT
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class Conversiones {
	
	/**
	 * Convierte una Direccion de SIIPAP en una TUbicacion del SAT
	 * 
	 * @param direccion
	 * @return
	 */
	public static TUbicacion getTUbicacion(final Direccion direccion){
		TUbicacion domicilio=TUbicacion.Factory.newInstance();
		return getTUbicacion(direccion, domicilio);
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
	
	/**
	 * Convierte una Direccion de SIIPAP en una TUbicacionFiscal del SAT
	 * 
	 * @param direccion
	 * @return
	 
	public static TUbicacionFiscal getTUbicacionFiscal(final Direccion direccion){
		TUbicacionFiscal domicilio=TUbicacionFiscal.Factory.newInstance();
		return getTUbicacionFiscal(direccion, domicilio);
	}*/
	
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
	
	

}
