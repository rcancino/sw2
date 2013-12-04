package com.luxsoft.sw3.cfdi;

import mx.gob.sat.cfd.x3.ComprobanteDocument;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.model.CFDI;



public class CFDIPruebas {
	
	
	public static void probarSellado(String cadena){
		
		CFDISellador sellador=new CFDISellador();
		sellador.setHibernateTemplate(ServiceLocator2.getHibernateTemplate());
		String res=sellador.generarSello(cadena);
		System.out.println(" Sello generado: "+res);
	}
	
	public static void validarSellado(String cadenaOriginal,String selloDigital){
		CFDISellador sellador=new CFDISellador();
		sellador.setHibernateTemplate(ServiceLocator2.getHibernateTemplate());
		boolean res=sellador.validarSello(cadenaOriginal, selloDigital);
		System.out.println("El sello para la cadena es valido: "+res);
	}
	
	public static void cadenaOriginal(){
		//To be implemented....
	}
	
	public static void timbreFiscal() throws Exception{
		//Cargar un cfd valido
		Resource xml=new ClassPathResource("sat/demo_cfdi.xml");
		ComprobanteDocument document= ComprobanteDocument.Factory.parse(xml.getInputStream());
		Comprobante cfdi=document.getComprobante();
		CFDI cc=new CFDI(document);
		System.out.println("CFD:"+cfdi.xmlText());
		System.out.println("Timbre fiscal: "+cc.getTimbreFiscal());
		CFDIUtils.validarDocumento(document);
	}
	
	public static void facturar(String ventaId){
		Venta venta=ServiceLocator2.getVentasManager().buscarVentaInicializada(ventaId);
		Object d1=ServiceLocator2.instance().getContext().getBean("cfdiFactura");
		System.out.println("O1: "+d1.getClass());
		CFDI cfdi=ServiceLocator2.getCFDIFactura().generar(venta);
		System.out.println("CFD: "+cfdi);
	}
	
	public static void main(String[] args) throws Exception{
		java.security.Security.addProvider(new BouncyCastleProvider());
		//String cadenaOriginal="Esta es una cadena de prueba para sello digital";
		//probarSellado(cadenaOriginal);
		//String selloDigital="nFenXfNhw4QPkM3E5FrgO0+o8Ro8bDwmvT/DVgkkZOiNwEPoQEXLrStrssIVr6Z07/WnKCqlUvxo2eEUZQmJQSV8hDp3BIHbLUXXa/BtnZhWOOv2TTS72nMpR8moTNDffoiun2n9wI98TJhKplY7OV9uXvkQa2CmVEBSGWqeYMU=";
		//validarSellado(cadenaOriginal, selloDigital);
		//cadenaOriginal();
		//timbreFiscal();
		facturar("8a8a8189-4131f97d-0141-31fca3b5-0005");
	}

}
