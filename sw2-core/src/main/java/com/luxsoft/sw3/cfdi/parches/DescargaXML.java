package com.luxsoft.sw3.cfdi.parches;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

import com.edicom.ediwinws.cfdi.client.CfdiClient;
import com.edicom.ediwinws.service.cfdi.CancelaResponse;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.sw3.cfdi.model.CFDI;

/**
 * Parche para generar las cancelaciones pendientes de CFDIs en el SAT
 * 
 * @author Ruben Cancino
 *
 */
public class DescargaXML {
	
	CfdiClient client;
	Empresa empresa;
	
	
	
	
	public void validacion (Date fecha){
		
		DBUtils.whereWeAre();
		empresa=ServiceLocator2.getConfiguracion().getSucursal().getEmpresa();
		SimpleDateFormat df=new SimpleDateFormat("ddMMyyyy");
		String strFecha=df.format(fecha);
				
		String dirPath="C://xlms//xmlPapelSA//"+strFecha;
		File dir=new File(dirPath);
		dir.mkdirs();
		
		 BufferedWriter bw;
		 
		//Assert.isTrue(dir.exists(),"No existe el directorio para cancelaciones: "+dirPath);
		//Assert.isTrue(dir.isDirectory(),"La ruta para las cancelaciones no es un directorio "+dirPath);
		
		String sql="SELECT x.CFD_ID FROM SX_CFDI X WHERE date(x.creado) = ? and xml is not null   ";
			
		
		Object[] args=new Object[]{
				new SqlParameterValue(Types.DATE, fecha)
				};
		List<String> rows=ServiceLocator2.getJdbcTemplate().queryForList(
				sql
				, args
				, String.class);
	
	
		try {
			
			for(String id:rows){
				CFDI cfdi=ServiceLocator2.getCFDIManager().getCFDI(id);
				
				//////Opcion 1
				System.out.println("Descargando..."+cfdi.getUUID());
				String xmlFile=empresa.getClave()+"-"+cfdi.getSerie()+"-"+cfdi.getFolio();
				File xml=new File(dir,xmlFile+".xml");
				FileOutputStream out=new FileOutputStream(xml);
				out.write(cfdi.getXml());
				out.flush();
				out.close();
				/////
				/*
				byte[] uUID=cfdi.getXml();
				String xlm=new String(uUID, "UTF-8");
				String xmlFile=empresa.getClave()+"-"+cfdi.getSerie()+"-"+cfdi.getFolio();
				//String xmlFile="QUERETARO"+"_CANCELACIONES_"+periodo.toString2();
				File msgFile=new File(dir,xmlFile+".xml");
				
				 bw = new BufferedWriter(new FileWriter(msgFile));
		         bw.write(xlm);
		         bw.close();
				*/
				//System.out.println("Descargando..."+xlm);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	

	
	public static void main(String[] args) {
		DescargaXML task=new DescargaXML();
		//task.validacion(new Periodo("21/10/2014","21/10/2014"));
		task.validacion(new Date("2014/12/20"));
	}

}
