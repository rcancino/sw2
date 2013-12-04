package com.luxsoft.sw3.cfd.ui.validaciones;

import java.io.File;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import mx.gob.sat.cfd.x2.ComprobanteDocument;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Conceptos.Concepto;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.springframework.util.Assert;

import com.luxsoft.sw3.cfd.services.validacion.ValidadorDeFoliosSAT;
import com.luxsoft.sw3.cfd.services.validacion.ValidarVigenciaDelCSD;

/**
 * Implementacion de {@link Validador} para la version 2 del schema de CFD del SAT
 * 
 * @author Ruben Cancino
 *
 */
public class Validador2  implements Validador{
	
	private List<Resultado> resultados;
	private File xmlFile;
	private ComprobanteDocument docto;

	public String getHTMLResult() {
		Assert.notEmpty(resultados,"Primero se debe procesar el archivo CFD antes de interpertar sus resultados");
		return ViewFactory2.getHTMLValidationView(resultados,xmlFile);
	}

	public List<Resultado> validar(File xmlFile) {
		this.xmlFile=xmlFile;
		this.resultados= new ArrayList<Resultado>();
		resultados.addAll(validarEstructura());
		resultados.add(validarCertificado());
		resultados.add(validarSerieFolio());
		resultados.add(validarContenidoBasico());
		return resultados;
	}
	
	private List<Resultado> validarEstructura(){
		
		final List<Resultado> resultados=new ArrayList<Resultado>();
		
		if(docto==null){
			try {
				docto=ComprobanteDocument.Factory.parse(xmlFile);
			} catch (Exception e) {
				throw new RuntimeException("Error procesando el archivo XML "+ExceptionUtils.getRootCauseMessage(e),e);
			}			
		}
		
		
		final XmlOptions options=new XmlOptions();
		final List<XmlValidationError> errors=new ArrayList<XmlValidationError>();
		options.setErrorListener(errors);
		boolean valid=docto.validate(options);
		
		if(valid){
			Resultado res=new Resultado("Estructura");
			res.setDescripcion("Estructura del CFD segun la versión 2 del schema del SAT");
			res.setResultado("CORRECTO");
			resultados.add(res);
		}else{
			for(XmlValidationError e:errors){
				//buff.append(e.getMessage()+"\n");
				Resultado res=new Resultado("Estructura");
				res.setDescripcion(e.getMessage());
				res.setResultado("ERROR");
				resultados.add(res);
			}
		}
		return resultados;
		
	}
	
	private Resultado validarCertificado(){
		final Comprobante comprobante=docto.getComprobante();
		try {
			String msg=ValidarVigenciaDelCSD.validar(
					comprobante.getNoCertificado()
					, comprobante.getEmisor().getRfc()
					, comprobante.getFecha().getTime()
					);
			Resultado res=new Resultado("SERIE_FOLIO");
			res.setDescripcion(msg);
			res.setResultado("CORRECTO");
			return res;
		} catch (Exception e) {
			Resultado res=new Resultado("SERIE_FOLIO");
			res.setDescripcion(ExceptionUtils.getRootCauseMessage(e));
			res.setResultado("ERROR");
			return res;
		}
		
	}
	
	private Resultado validarSerieFolio(){
		final Comprobante comprobante=docto.getComprobante();
		try {
			String msg2=ValidadorDeFoliosSAT.validar(
					comprobante.getEmisor().getRfc()
					,comprobante.getNoAprobacion().toString()
					,Integer.valueOf(comprobante.getAnoAprobacion())
					,comprobante.getSerie()
					,comprobante.getFolio());
			Resultado res=new Resultado("SERIE_FOLIO");
			res.setDescripcion(msg2);
			res.setResultado("CORRECTO");
			return res;
		} catch (Exception e) {
			Resultado res=new Resultado("SERIE_FOLIO");
			res.setDescripcion(ExceptionUtils.getRootCauseMessage(e));
			res.setResultado("ERROR");
			return res;
		}
		
	}
	
	private Resultado validarContenidoBasico(){
		final Comprobante comprobante=docto.getComprobante();
		BigDecimal subTotal=comprobante.getSubTotal();
		BigDecimal suma=BigDecimal.ZERO;
		for(Concepto c:comprobante.getConceptos().getConceptoArray()){
			suma=suma.add(c.getImporte());
		}
		Resultado res=new Resultado("CERTIFICADO");
		if(subTotal.equals(suma)){
			res.setDescripcion("Los importes de los conceptos son consistentes con el subtotal");
			res.setResultado("CORRECTO");
		}
		else{
			res.setDescripcion(MessageFormat.format("La suma de los importes de los conceptos no es igual al subtotal. Suma: {0} SubTotal: {1}", suma,subTotal));
			res.setResultado("ERROR");
			
		}
		return res;
	}
}
