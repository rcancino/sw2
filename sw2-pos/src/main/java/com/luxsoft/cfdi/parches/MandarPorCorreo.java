package com.luxsoft.cfdi.parches;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.services.Services;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class MandarPorCorreo {
	
	public void mandar(String id) throws Exception{
		CFDI cfdi=Services.getCFDIManager().getCFDI(id);
		email(cfdi);
	}
	
	public void email(CFDI cfdi){
		String to="cpradoglez@gmail.com";
		String cc="soporte_sist@papelsa.com.mx";
		try {
			JavaMailSender mailSender=Services.getMailSender();
			MimeMessage mimeMessage=mailSender.createMimeMessage();
			MimeMessageHelper messageHelper=new MimeMessageHelper(mimeMessage,true);
			messageHelper.setFrom("ventas@papelsa.com.mx");
			messageHelper.setTo(to);
			messageHelper.setCc(cc);
			messageHelper.setSubject("Comprobante fiscal digital (CFDI) de ");
			String text=getHtml(cfdi);
			messageHelper.setText(text);
			messageHelper.setReplyTo("noreplay");
			byte[] xml=cfdi.getXml();
			InputStreamSource source=new ByteArrayResource(xml);
			messageHelper.addAttachment(cfdi.getXmlFilePath(),source);
			//System.out.println("HTML: "+text);
			MimeMessage msg=messageHelper.getMimeMessage();
			
			mailSender.send(msg);
			System.out.println("Correo enviado para:  "+cfdi.getUUID());
		} catch (MailException e) {
			System.out.println(e.getMessage());
		} catch( MessagingException me){
			System.out.println("Error MessagingEx: "+me.getMessage());
		} catch(IOException ioe){
			System.out.println("Error IOEx: "+ioe.getMessage());
		} catch(TemplateException te){
			System.out.println("Error TemlEx: "+te.getMessage());
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getHtml(CFDI cfdi) throws IOException, TemplateException{
		String emisor=cfdi.getEmisor();
		Date creado=cfdi.getLog().getCreado();
		Configuration cfg=new Configuration();
		cfg.setDateFormat("dd/MM/yyyy");
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setClassForTemplateLoading(getClass(), "");
		
		String path="images2/papelLogo.jpg";
		ClassPathResource resource=new ClassPathResource(path);
		
		Map root=new HashMap();
		root.put("cliente", cfdi.getReceptor());
		root.put("total", cfdi.getTotalAsString());
		root.put("emisor", emisor);		
		root.put("uuid", cfdi.getUUID());
		root.put("serie", cfdi.getSerie());
		root.put("folio", cfdi.getFolio());
		root.put("fecha", creado);
		root.put("cid:papelLogo", resource);
		root.put("", cfdi.getUUID());
		final Template temp=cfg.getTemplate("envioDeCfdiReTimbrados.ftl");			
		StringWriter out=new StringWriter();
		temp.process(root, out);			
		return out.toString();
	}
	
	public static void main(String[] args) throws Exception{
		MandarPorCorreo task=new MandarPorCorreo();
		task.mandar("8a8a81e6-43021f32-0143-022b607c-0014");
	}

}
