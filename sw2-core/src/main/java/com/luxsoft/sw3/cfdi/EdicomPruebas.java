package com.luxsoft.sw3.cfdi;

import org.apache.commons.lang.ArrayUtils;
import org.bouncycastle.util.encoders.Base64;

import com.edicom.ediwinws.cfdi.client.CfdiClient;
import com.edicom.ediwinws.cfdi.client.CfdiException;
import com.edicom.ediwinws.cfdi.utils.FileUtils;
import com.edicom.ediwinws.cfdi.utils.ZipUtils;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.cfdi.model.CFDI;

import java.io.File;
import java.io.FileOutputStream;

public class EdicomPruebas {
	
	public static void validarCadena(){
		CFDI cfdi=(CFDI)ServiceLocator2.getHibernateTemplate().get(CFDI.class, "402881c6-42df3b26-0142-df3be4de-0006");
		System.out.println("Cadena original: "+cfdi.getCadenaOriginal());
		byte[] encoded=Base64.encode(cfdi.getCadenaOriginal().getBytes());
		System.out.println("B64<"+new String(encoded));
	}
	
	public static void timbrar()throws Exception{
		try {
			CfdiClient ws=new CfdiClient();
			
			
			
			File xml=new File(System.getProperty("user.home"),"402881c6-42df6171-0142-df63224c-0006.xml");
			byte[] archivo=FileUtils.lecturaFicheroBinario(xml);
			
			//System.out.println("archivo lenght: "+xml.length());
			//FileOutputStream out=new FileOutputStream("factura2.xml");
			//out.write(archivo);
			//out.flush();
			//out.close();
			
			
			System.out.println("archivo lenght: "+archivo.length);
			
			ZipUtils utils=new ZipUtils();
			byte[] zipFile=utils.comprimeArchivo("facturaCfdi3.xml", archivo);
			
			
			byte[] res=ws.getCfdiTest("PAP830101CR3", "yqjvqfofb", zipFile);
			
			FileOutputStream out=new FileOutputStream("factura3.zip");
			out.write(res);
			out.flush();
			out.close();
		} catch (CfdiException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws Exception{
		timbrar();
		//validarCadena();
	}

}
