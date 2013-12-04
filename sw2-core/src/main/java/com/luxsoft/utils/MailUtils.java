 package com.luxsoft.utils;

import java.io.File;
import java.net.URL;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.luxsoft.siipap.service.MailEngine;
import com.luxsoft.siipap.service.ServiceLocator2;

public class MailUtils {
	
	public static void testAttachementMail(){		
		try {
			JavaMailSender mailSender=(JavaMailSender)ServiceLocator2.instance().getContext().getBean("mailSender");
			MimeMessage mimeMessage=mailSender.createMimeMessage();
			MimeMessageHelper messageHelper=new MimeMessageHelper(mimeMessage,true);
			URL url=new URL("file:/Z:/IMPAP/CFD/xml/NOTA_CREDITO_0000013.xml");
			File file=new File(url.toURI());
			URL url2=new URL("file:/Z:/IMPAP/CFD/xml/NOTA_CREDITO_0000002.xml");
			File file2=new File(url2.toURI());
			//FileSystemResource resource=new FileSystemResource("file:/Z:/IMPAP/CFD/xml/NOTA_CREDITO_0000013.xml");
			messageHelper.setFrom("cuentasporpagar@papelsa.com.mx");
			messageHelper.setTo("lquintanilla@gmail.com");
			messageHelper.addCc("soporte_sist@papelsa.com.mx");
			messageHelper.addCc("cprado@papelsa.com.mx");
			messageHelper.setCc("cpradoglez@gmail.com");
			messageHelper.setCc("rcancino@luxsoftnet.com");
			
			messageHelper.setSubject("PRUEBA DE CORREO CON ATTACHMENT");
			messageHelper.setText("PRUEBA DE CORREO ELECTRONICO AUTOMATICO CON ATTACHMENT");
			messageHelper.addAttachment("NOTA_CREDITO_0000013.xml", file);
			messageHelper.addAttachment("NOTA_CREDITO_0000000.xml", file2);
			mailSender.send(mimeMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void testMail(){
		JavaMailSender mailSender=(JavaMailSender)ServiceLocator2.instance().getContext().getBean("mailSender");
		SimpleMailMessage msg=new SimpleMailMessage();		
		msg.setText("Mail de prueba");
		msg.setFrom("noreplay@papelsa.com.mx");
		
		msg.setTo("lquintanilla@gmail.com");
		msg.setBcc("soporte_sist@papelsa.com.mx");
		
		msg.setBcc("rubencancino6@gmail.com");
		msg.setBcc("rcancino@luxsoftnet.com");
		
		msg.setBcc("cprado@papelsa.com.mx");
		msg.setBcc("cpradoglez@gmail.com");
		
		msg.setSentDate(new Date());
		msg.setSubject("PRUEBA DE CORREO ELECTRONICO AUTOMATICO");
		msg.setText("PRUEBA DE CORREO ELECTRONICO AUTOMATICO");
		
		try {
			mailSender.send(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//testMail();
		testAttachementMail();
	}

}
