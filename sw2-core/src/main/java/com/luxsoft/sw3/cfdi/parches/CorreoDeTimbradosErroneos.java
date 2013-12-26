package com.luxsoft.sw3.cfdi.parches;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.CFDIPrintServices;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.cfdi.model.CFDIClienteMails;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class CorreoDeTimbradosErroneos {
	
	
	public String getDestino(CFDI cfdi){
		Venta v=ServiceLocator2.getVentasManager().get(cfdi.getOrigen());
		Cliente c=v.getCliente();
		List<CFDIClienteMails> mails=ServiceLocator2.getHibernateTemplate().find("from CFDIClienteMails c where c.cliente.id=?",c.getId());
		return mails.isEmpty()?c.getEmai3():mails.get(0).getEmail1();
				
	}
	
	public  void run(){
		List<CFDI> list=ServiceLocator2.getHibernateTemplate().find("from CFDI c where c.comentario=?","RE PROGRAMAR ENVIO");
		int count=0;
		for(final CFDI cfdi:list){
			
			try {
				String to=getDestino(cfdi);
				String cc="soporte_sist@papelsa.com.mx";
				String ccc="cpradoglez@gmail.com";
				
				JavaMailSender mailSender=ServiceLocator2.getCFDIMailServices().getMailSender();//.mandarPorCorreo(cfdi.getId());
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
				
				JasperPrint jp=(JasperPrint)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
					public Object doInHibernate(Session session) throws HibernateException,SQLException {
						Venta venta=(Venta)session.get(Venta.class, cfdi.getOrigen());
						JasperPrint jp=CFDIPrintServices.impripirComprobante(venta, cfdi, "", false);
						return jp;
					}
				});
				if(jp!=null){
					
					byte[] pdf=JasperExportManager.exportReportToPdf(jp);
					InputStreamSource sourcePdf=new ByteArrayResource(pdf);
					String pdfName=StringUtils.replace(cfdi.getXmlFilePath(), ".xml", ".pdf");
					messageHelper.addAttachment(pdfName,sourcePdf);
					
					/*
					String pdfName=StringUtils.replace(cfd.getXmlFilePath(), ".xml", ".pdf");
					String pdfPath=System.getProperty("user.home")+"/"+pdfName;
					
					File dest=new File(pdfPath);
					FileOutputStream fout=new FileOutputStream(dest);
					JasperExportManager.exportReportToPdfFile(jp, pdfPath);
					*/
					//messageHelper.addAttachment(pdfName,dest);
				}
				
				mailSender.send(msg);
				cfdi.setComentario("Correo enviado "+DateUtil.getDateTime("dd/MM/yyyy hh:mm:ss", new Date())+" To:"+to );
				ServiceLocator2.getHibernateTemplate().merge(cfdi);
				System.out.println("Correo enviado para cfdi: "+cfdi.getUUID()+"  email:"+to);
				count++;
			} catch (Exception e) {
				String msg=ExceptionUtils.getRootCauseMessage(e);
				//cfdi.setComentario("Correo enviado "+DateUtil.getDateTime("dd/MM/yyyy hh:mm:ss", new Date()));
				//ServiceLocator2.getHibernateTemplate().merge(cfdi);
				System.out.println("Error: "+msg);
			}
		}
		System.out.println("Correos de correccion enviados: "+count);
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
	
	
	public static void main(String[] args) {
		new CorreoDeTimbradosErroneos().run();
	}

}
