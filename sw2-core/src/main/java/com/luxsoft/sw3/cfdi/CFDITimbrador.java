package com.luxsoft.sw3.cfdi;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.edicom.ediwinws.cfdi.client.CfdiClient;
import com.edicom.ediwinws.cfdi.utils.ZipUtils;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.cfdi.model.TimbreFiscal;
import com.luxsoft.utils.LoggerHelper;

import java.util.Map;


@Service("cfdiTimbrador")
public class CFDITimbrador implements InitializingBean{
	
	private CfdiClient cfdiClient;
	ZipUtils utils=new ZipUtils();
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	private Logger logger=LoggerHelper.getLogger();
	
	public CFDITimbrador(){
		
	}
	
	public CFDI timbrar(CFDI cfdi) throws Exception{
		
		String nombre=cfdi.getXmlFilePath();
		byte[] xml=cfdi.getXml();
		byte[] zipFile=utils.comprimeArchivo(nombre, xml);
		
		//byte[] res=cfdiClient.getCfdiTest("PAP830101CR3", "yqjvqfofb", zipFile);
		byte[] res=null;
		String modo =System.getProperty("cfdi.timbrado", "test");
		
		if(modo.equals("test")){
			System.out.println("Timbrando en modo de prueba...");
			res=cfdiClient.getCfdiTest("PAP830101CR3", "yqjvqfofb", zipFile);
		}else{
			System.out.println("Timbrando en modo produccion...");
			res=cfdiClient.getCfdi("PAP830101CR3", "yqjvqfofb", zipFile);
		}
		 
		
		Map<String, byte[]> map =utils.descomprimeArchivo(res);
		Map.Entry<String, byte[]> entry=map.entrySet().iterator().next();
		
		cfdi.setXmlFilePath(entry.getKey());
		cfdi.setXml(entry.getValue());
		//cfdi.setDocument(null);
		cfdi.setTimbre(new TimbreFiscal(cfdi.getComprobante()));
		cfdi.setUUID(cfdi.getTimbreFiscal().getUUID());
		cfdi.cargarTimbrado();
		cfdi=(CFDI)hibernateTemplate.merge(cfdi);
		salvarArchivoXml(cfdi);
		return cfdi;
	}
	
	private void salvarArchivoXml(CFDI cfdi){
		try {
			cfdi.salvarArchivoTimbradoXml();
		} catch (Exception e) {
			System.err.println(ExceptionUtils.getRootCauseMessage(e));
			logger.error(e);
		}
		
	}
	
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
	public void afterPropertiesSet() throws Exception {
		cfdiClient=new CfdiClient();
		
	}
	

}
