package com.luxsoft.sw3.cxc.services;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.Assert;

import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

/**
 * Servicios de correo para el modulo de cuentas por cobrar
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CXCMailServices {
	
	@Autowired
	private JavaMailSender mailSender;
	
	
	public MimeMessageHelper prepararCorreoElectronicoOld(
			String from
			,String to
			,ComprobanteFiscal...cfds ){
		Assert.notEmpty(cfds,"La lista de CFD a enviar esta vacia");
		
		try {
			MimeMessage mimeMessage=mailSender.createMimeMessage();
			MimeMessageHelper messageHelper=new MimeMessageHelper(mimeMessage,true);
			messageHelper.setFrom(from);
			messageHelper.setTo(to);
			messageHelper.setSubject("PRUEBA DE CORREO CON ATTACHMENT");
			messageHelper.setText("PRUEBA DE CORREO ELECTRONICO AUTOMATICO CON ATTACHMENT");
			for(ComprobanteFiscal cfd:cfds){
				String path="file:/"+System.getProperty("cfd.dir.path")+"/"+cfd.getXmlPath();
				if(cfd.getXmlPath().startsWith("file")){
					path=cfd.getXmlPath();
				}
				URL url=new URL(path);
				File file=new File(url.toURI());
				String pattern="CFD {0} serie: {1} folio:{2} {3}";
				messageHelper.addAttachment(MessageFormat.format(pattern, cfd.getTipo(),cfd.getSerie(),cfd.getFolio(),".xml"),file );
			}
			return messageHelper;
		} catch (Exception e) {
			String msg="Error preparando correo: \n"+ExceptionUtils.getRootCauseMessage(e);
			throw new RuntimeException(msg,e);
		}
		
	}
	
	public MimeMessageHelper prepararCorreoElectronico(
			String from
			,String to
			,ComprobanteFiscal...cfds ){
		Assert.notEmpty(cfds,"La lista de CFD a enviar esta vacia");
		
		try {
			MimeMessage mimeMessage=mailSender.createMimeMessage();
			MimeMessageHelper messageHelper=new MimeMessageHelper(mimeMessage,true);
			messageHelper.setFrom(from);
			messageHelper.setTo(to);
			messageHelper.setSubject("");
			messageHelper.setText("");
			for(ComprobanteFiscal cfd:cfds){
				//XML
				//String fileName=StringUtils.substringAfterLast(cfd.getXmlPath(), "/");
				String path="file:/"+System.getProperty("cfd.dir.path")+"/"+cfd.getXmlPath();
				//String path=(""+path1);
				if(cfd.getXmlPath().startsWith("file")){
					path=cfd.getXmlPath();
				}
				//System.out.println("**********************************"+path);
				URL url=new URL(path);
				//System.out.println("**********************************"+url);
				File file=new File(url.toURI());
				messageHelper.addAttachment(file.getName(),file );
				//PDF
				try {
					String pdfURL=StringUtils.replace( path, ".xml", ".pdf");
					URL pdfUrl=new URL(pdfURL);
					File pdf=new File(pdfUrl.toURI());
					if(!pdf.exists()){
						throw new RuntimeException("No existe el archivo: "+pdf);
					}
					//System.out.println("/***************"+pdf.getName() +"---------"+pdf);
					messageHelper.addAttachment(pdf.getName(),pdf);
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(e);
				}
				
			}
			return messageHelper;
		} catch (Exception e) {
			String msg="Error preparando correo: \n"+ExceptionUtils.getRootCauseMessage(e);
			throw new RuntimeException(msg,e);
		}
		
	}
	
	public void mandarCorreo(MimeMessageHelper msg){
		try {
			getMailSender().send(msg.getMimeMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(ExceptionUtils.getRootCauseMessage(e),e);
		}
	}


	public JavaMailSender getMailSender() {
		return mailSender;
	}


	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	

}
