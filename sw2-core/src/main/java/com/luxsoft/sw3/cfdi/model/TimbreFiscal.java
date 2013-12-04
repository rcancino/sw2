package com.luxsoft.sw3.cfdi.model;

import java.text.MessageFormat;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.xmlbeans.XmlObject;

import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Complemento;

public class TimbreFiscal {
	
	public String version;
	public String UUID;
	public String FechaTimbrado;
	public String selloCFD;
	public String selloSAT;
	public String noCertificadoSAT;
	
	
	
	public TimbreFiscal(Comprobante cfdi){
		
		Complemento complemento=cfdi.getComplemento();
		String queryExpression =
			    "declare namespace tfd='http://www.sat.gob.mx/TimbreFiscalDigital';" +
			    "$this/tfd:TimbreFiscalDigital";
		XmlObject[] res=complemento.selectPath(queryExpression);
		if(res.length>0){
			XmlObject timbre=res[0];
			version=timbre.getDomNode().getAttributes().getNamedItem("version").getNodeValue();
			UUID=timbre.getDomNode().getAttributes().getNamedItem("UUID").getNodeValue();
			FechaTimbrado=timbre.getDomNode().getAttributes().getNamedItem("FechaTimbrado").getNodeValue();
			selloCFD=timbre.getDomNode().getAttributes().getNamedItem("selloCFD").getNodeValue();
			selloSAT=timbre.getDomNode().getAttributes().getNamedItem("selloSAT").getNodeValue();
			noCertificadoSAT=timbre.getDomNode().getAttributes().getNamedItem("noCertificadoSAT").getNodeValue();
		}
	}
	

	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
	public String cadenaOriginal(){
		String pattern="||{0}|{1}|{2}|{3}|{4}||";
		return MessageFormat.format(pattern, version,UUID,FechaTimbrado,selloCFD,noCertificadoSAT);
	}


	public String getVersion() {
		return version;
	}


	public String getUUID() {
		return UUID;
	}


	public String getFechaTimbrado() {
		return FechaTimbrado;
	}


	public String getSelloCFD() {
		return selloCFD;
	}


	public String getSelloSAT() {
		return selloSAT;
	}


	public String getNoCertificadoSAT() {
		return noCertificadoSAT;
	}
	
	
}
