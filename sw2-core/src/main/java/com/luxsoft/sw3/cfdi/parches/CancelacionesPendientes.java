package com.luxsoft.sw3.cfdi.parches;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.util.Assert;

import com.edicom.ediwinws.cfdi.client.CfdiClient;
import com.edicom.ediwinws.cfdi.client.CfdiException;
import com.edicom.ediwinws.service.cfdi.CancelaResponse;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.sw3.cfdi.model.CFDI;

/**
 * Parche para generar las cancelaciones pendientes de CFDIs en el SAT
 * 
 * @author Ruben Cancino
 *
 */
public class CancelacionesPendientes {
	
	CfdiClient client;
	Empresa empresa;
	final String pfxPassword;
	
	
	 public CancelacionesPendientes(String password) {
		this.pfxPassword=password;
	}
	
	
	
	public void run(){
		DBUtils.whereWeAre();
		empresa=ServiceLocator2.getConfiguracion().getSucursal().getEmpresa();
		System.out.println("Cancelando pendientes para sucursal: "+ServiceLocator2.getConfiguracion().getSucursal());
		try {
			client=new CfdiClient();
			String sql="";
			Object args[]=new Object[]{};
			List<String> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql, args, String.class);
			for(String uuid:rows){
				System.out.println("Cancelando uuid: "+uuid);
			}
		} catch (CfdiException e) {
			e.printStackTrace();
			System.out.println("Error en EDICOM: "+ExceptionUtils.getRootCauseMessage(e));
		}
	}
	
	public void cancelar(String uuid) throws Exception{
		String[] uuidList=new String[]{uuid};
		System.out.println("Mandando canclera CFDIS: "+ArrayUtils.toString(uuidList));
		
		String dirPath=System.getProperty("cfd.dir.path")+"/cfdi/cancelados";
		File dir=new File(dirPath);
		Assert.isTrue(dir.exists(),"No existe el directorio para cancelaciones: "+dirPath);
		Assert.isTrue(dir.isDirectory(),"La ruta para las cancelaciones no es un directorio "+dirPath);
		
		
		
		CancelaResponse res=client.cancelCfdi(
				"PAP830101CR3"
				,"yqjvqfofb"
				, empresa.getRfc()
				, uuidList
				, empresa.getCertificadoDigitalPfx()
				, pfxPassword);
		String msg=res.getText();
		String aka=res.getAck();
		//String[] uuids=res.getUuids();
		CFDI cfdi=ServiceLocator2.getCFDIManager().buscarPorUUID(uuid);
		Assert.notNull(cfdi,"No eixste el CFDI con UUID:"+uuid);
				
		try {
			//byte[] d1=Base64.decode(msg.getBytes());
			String xmlFile=StringUtils.remove(cfdi.getXmlFilePath(), "xml");
			byte[] d1=msg.getBytes();
			File msgFile=new File(dir,xmlFile+"_MSG.xml");
			FileOutputStream out1=new FileOutputStream(msgFile);
			out1.write(d1);
			out1.close();
			
			
			//byte[] d2=Base64.decode(aka.getBytes());
			byte[] d2=aka.getBytes();
			File akaFile=new File(dir,xmlFile+"_AKA.xml");
			FileOutputStream out2=new FileOutputStream(akaFile);
			out2.write(d2);
			out2.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error salvando archivos de cancelacion: "+ExceptionUtils.getRootCauseMessage(e));
		}
		cfdi.setCancelacion(new Date());
		ServiceLocator2.getHibernateTemplate().merge(cfdi);
		
		
	}
	
	public static void main(String[] args) {
		CancelacionesPendientes task=new CancelacionesPendientes("certificadopapel");
		task.run();
	}

}
