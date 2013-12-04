package com.luxsoft.sw3.cfd.task;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.cfd.services.CFDPdfUtils;
import com.luxsoft.utils.LoggerHelper;

/**
 * Genera archivos PDF para los CFD
 * 
 * @author Ruben Cancino
 *
 */
public class GeneradorDePdf {

	Logger logger=LoggerHelper.getLogger();
	DateFormat df=new SimpleDateFormat("yyyy_MM_dd");
	List<String> faltantes=new ArrayList<String>();
 
 	public void generar(){
 		generar(DateUtils.addDays(new Date(), -1));
 	}
 	
 	public void generar(String fechaIni,String fechaFin){
 		Periodo periodo=new Periodo(fechaIni,fechaFin);
 		for(Date dia:periodo.getListaDeDias()){
 			generar(dia);
 		}
 	}
	
	public void generar(Date dia){
		faltantes.clear();
		String hql="from ComprobanteFiscal c where date(c.log.creado)=? and c.rfc!='XAXX010101000'";
		List<ComprobanteFiscal> cfds=ServiceLocator2.getHibernateTemplate().find(hql, new Object[]{dia});
		logger.info("Comprobantes detectados :"+cfds.size()+"  Fecha: "+dia);
		int i=0;
		for(ComprobanteFiscal cfd:cfds){
			try {
				generarPdf(cfd);
			} catch(Exception e){
				String msg=ExceptionUtils.getRootCauseMessage(e);
				logger.info("Error generando pdf: "+cfd.getId()+"  Causa:"+ExceptionUtils.getRootCauseMessage(e));
				faltantes.add(cfd.toString()+" Posible causa: "+msg);
			}
			i++;
			System.out.println(" Procesado: "+i+ " De:"+cfds.size());
		}
		System.out.println("Faltantes de genrar: "+faltantes.size());
		for(String s:faltantes){
			
			System.out.println(s);
		}
	}
	
	public void generarPdf(ComprobanteFiscal cfd) throws Exception{
		cfd.loadComprobante();
		String fileName=cfd.getSerie()+cfd.getFolio()+".pdf";
		
		File dir=new File(CFDPdfUtils.getDirPath()+df.format(cfd.getComprobante().getFecha().getTime()));
		if(!dir.exists()){
			dir.mkdir();
		}
		File dest=new File(dir,fileName);				
		if(StringUtils.equals(cfd.getTipo().trim(), "FACTURA")){
			CFDPdfUtils.generarPdfVenta(cfd, dest);
		}else if(cfd.getTipo().equals("NOTA_CREDITO")){
			CFDPdfUtils.generarPdfNotaDeCredito(cfd, dest);
		}else if(cfd.getTipo().equals("NOTA_CARGO")){
			CFDPdfUtils.generarPdfNotaDeCargo(cfd, dest);
		}
	}
	
	public File generarPdfFile(ComprobanteFiscal cfd) throws Exception{
		if(cfd.getComprobanteDocument()==null)
			cfd.loadComprobante();
		String path="file:/"+System.getProperty("cfd.dir.path")+"/"+cfd.getXmlPath();
		if(cfd.getXmlPath().startsWith("file")){
			path=cfd.getXmlPath();
		}
		String pdfURL=StringUtils.replace( path, ".xml", ".pdf");
		URL url=new URL(pdfURL);
		File pdf=new File(url.toURI());
		if(StringUtils.equals(cfd.getTipo().trim(), "FACTURA")){
			CFDPdfUtils.generarPdfVenta(cfd, pdf);
		}else if(cfd.getTipo().equals("NOTA_CREDITO")){
			CFDPdfUtils.generarPdfNotaDeCredito(cfd, pdf);
		}else if(cfd.getTipo().equals("NOTA_CARGO")){
			CFDPdfUtils.generarPdfNotaDeCargo(cfd, pdf);
		}
		return pdf;
	}
	
	

	public static void main(String[] args) throws Exception{
		
		GeneradorDePdf g=new GeneradorDePdf();
		
		//String id="8a8a8783-380f543d-0138-0f86dd6f-0096";
		//final ComprobanteFiscal cfd=(ComprobanteFiscal)ServiceLocator2.getHibernateTemplate().get(ComprobanteFiscal.class, id);
		//g.generarPdf(cfd);
		g.generar("26/09/2013","05/10/2013");
	//	g.generar();
	}

}
