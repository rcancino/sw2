package com.luxsoft.sw3.cfdi.parches;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;


import com.edicom.ediwinws.cfdi.client.CfdiClient;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.CFDITimbrador;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.cfdi.model.CFDIClienteMails;

public class CorreccionDeTimbradoEnModoPruebasReGenerandoXML {
	
	public CorreccionDeTimbradoEnModoPruebasReGenerandoXML() {
		
	}
	
	
	public static void run() throws Exception{
		
		final File sourceDir=new File("Y:\\cfd\\xml\\cfdi");
		Assert.isTrue(sourceDir.exists(),"No existe la carpeta de CFDI sin timbrar");
		Assert.isTrue(sourceDir.isDirectory());
		System.out.println("Corrigiendo timbrado en URL: "+System.getProperty("jdbc.url"));
		System.out.println("Procesando carpeta: "+sourceDir.getAbsolutePath());
		
		List<String> ids=ServiceLocator2.getJdbcTemplate()
				.queryForList("SELECT CFD_ID FROM SX_CFDI C WHERE C.UUID LIKE ?"
						,new Object[]{"%-7E57-%"},String.class);
		
		System.out.println("Comprobantes a procesar: "+ids.size());
		int row=0;
		for(String id:ids){
			
			try{
				
				CFDI cfdi=ServiceLocator2.getCFDIManager().getCFDI(id);
				Venta venta=ServiceLocator2.getVentasManager().buscarVentaInicializada(cfdi.getOrigen());
				ServiceLocator2.getHibernateTemplate().delete(cfdi);
				CFDI newCfdi=ServiceLocator2.getCFDIManager().generarFactura(venta);
				
				// Timbrando nuevamente
				newCfdi.setComentario("RE TIMBRADO POR ERROR DE PRUEBAS ");
				ServiceLocator2.getCFDIManager().timbrar(newCfdi);
				
				row++;
			}catch(Exception e){
				System.out.println("Error procesando cfdi "+ExceptionUtils.getRootCauseMessage(e));
			}
		}
		
		System.out.println("Total de cfdi procesados: "+row);
	}
	
	
	public static void main(String[] args) throws Exception{
		//-Djdbc.url=jdbc:mysql://10.10.1.101/produccion -DsucursalOrigen=TACUBA
		System.setProperty("jdbc.url", "jdbc:mysql://10.10.7.1/produccion");
		System.setProperty("sucursalOrigen", "CF5FEBRERO");
		System.setProperty("cfdi.timbrado", "produccion");
		System.setProperty("cfd.dir.path", "C:\\cfdi\\correcciones");
		run();
	}

}
