package com.luxsoft.sw3.cfdi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.internet.MimeMessage;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.cfdi.model.CFDIClienteMails;
import com.luxsoft.utils.LoggerHelper;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;


public class CFDI_MailServices {
	
	
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	
	
	
	private Set fallidos=new HashSet();
	private Set exitosos=new HashSet();
	long totalCfds=0;
	
	Logger logger=LoggerHelper.getLogger();
	
	public void madarPorCorreo(){
		madarPorCorreo(DateUtils.addDays(new Date(), -1));
	}
	
	public void  madarPorCorreo(String fechaIni,String fechaFin){
 		Periodo periodo=new Periodo(fechaIni,fechaFin);
 		for(Date dia:periodo.getListaDeDias()){
 			 madarPorCorreo(dia);
 		}
 	}
	@SuppressWarnings("unchecked")
	public void madarPorCorreo(Date dia){
		fallidos=new HashSet();
		exitosos=new HashSet();
		//String hql="select c.id from ComprobanteFiscal c where date(c.log.creado)=?";
		String hql="select c.id from CFDI c where date(c.log.creado)=? and c.rfc!='XAXX010101000'"
				+"  and c.serie like \'%FACCRE\' ";
		
		List<String> cfds=ServiceLocator2.getHibernateTemplate().find(hql, new Object[]{dia});
		totalCfds=cfds.size();
		for(String cfd:cfds){
			try {
				mandarPorCorreo(cfd);
			} catch (Exception e) {
				logger.error(ExceptionUtils.getRootCauseMessage(e),e);
			}
		}
		mandarInforme(dia);
	}
	
	@SuppressWarnings("unchecked")
	public void madarPorCorreoPorOrigen(Date dia,String origen){
		
		String hql="";
		fallidos=new HashSet();
		exitosos=new HashSet();
		System.out.println("Enviando Cfdi's del dia: " +dia +" Origen: " + origen );
		 	
		 if (origen.equals(new String("CRE"))){
			
		     hql="select c.id from CFDI c where date(c.log.creado)=? and c.rfc!='XAXX010101000'"
						+"  and c.serie like '%CRE'  ";
		 }
		 if (origen.equals(new String("CAM"))){
				
			  hql="select c.id from CFDI c where date(c.log.creado)=? and c.rfc!='XAXX010101000'"
						+"  and c.serie like '%CAM'  ";
			
		 }
		 if (origen.equals(new String("MOS"))){
				
			 hql="select c.id from CFDI c where date(c.log.creado)=? and c.rfc!='XAXX010101000'"
						+"  and c.serie like '%MOS'  ";
				
		 }
		 
		  
		  
		 List<String> cfds=ServiceLocator2.getHibernateTemplate().find(hql
					, new Object[]{ dia}
					);
		totalCfds=cfds.size();
		
		for(String cfd:cfds){
			try {
				mandarPorCorreo(cfd);
			} catch (Exception e) {
				logger.error(ExceptionUtils.getRootCauseMessage(e),e);
			}
		}
		mandarInforme(dia);
	}
	

	
	public void mandarPorCorreo(final String cfdId){
		final CFDI cfd=(CFDI)getHibernateTemplate().get(CFDI.class, cfdId);
		final Cargo cargo;
		Boolean enviar= true;
			
		if(cfd.getTipo().equals("I")){
			 cargo=(Cargo)getHibernateTemplate().get(Cargo.class,cfd.getOrigen());
				
			if(cargo.getComentario2()!=null)
			    enviar=!cargo.getComentario2().endsWith("CANCELADO");
			
		}
		
		if(cfd.getTipoCfd().equals("E")){
			final Abono abono=(Abono)getHibernateTemplate().get(Abono.class, cfd.getOrigen());
			if(abono.getComentario()!=null)
			   enviar=!abono.getComentario().endsWith("CANCELADO");
		}
	     	
		List<Cliente> data=getHibernateTemplate().find("from Cliente c where c.rfc=?",cfd.getRfc());
		Assert.notEmpty(data,"No existe el cliente del CFD: "+cfdId);
		Cliente c=data.get(0);
		
		List<String> mailCte=getHibernateTemplate().find("select email1 from CFDIClienteMails c where c.cliente.id=?",c.getId());
		Assert.notEmpty(mailCte,"No existe el mail del Cte: "+c.getNombre());
		 String mailEnv=mailCte.get(0);
		
		System.out.println("Enviando correo a:-----------------------  "+mailEnv + " CFDI   "  + cfd.getFolio() +" Cliente: " +cfd.getReceptor()+" Origen_id: "+cfd.getOrigen());
		//if(StringUtils.isNotBlank(c.getEmai3())){
		if(mailEnv!=null){	
			if(enviar){
				//madarPorCorreo(cfd,c.getEmai3(),"cfd_auxiliar@papelsa.com.mx");
				//madarPorCorreo(cfd,"cpradoglez@gmail.com","cfd_auxiliar@papelsa.com.mx");
				
				madarPorCorreo(cfd,mailEnv,"cfd_auxiliar@papelsa.com.mx");
			}else{
				System.out.println("El Documento esta cancelado  "+ cfd.getFolio() +" Cliente: " +cfd.getReceptor()+" Origen_id: "+cfd.getOrigen());
			}
		// madarPorCorreo(cfd,c.getEmai3(),"cfd_auxiliar@papelsa.com.mx");			
		//	madarPorCorreo(cfd,"sisi@papelsa.com.mx","cfd_auxiliar@papelsa.com.mx");
		//	madarPorCorreo(cfd,"cfd_auxiliar@papelsa.com.mx","cfd_auxiliar@papelsa.com.mx");
		}else{
			fallidos.add(cfd);
			logger.info("CFD con cliente sin correo para CFD definido cliente:"+c+"  CFD: "+cfd);
		}
		//madarPorCorreo(cfd,"csanchez@papelsa.com.mx","rsanchez@papelsa.com.mx");		
	}
	
	public void madarPorCorreo(final CFDI cfd,String to,String cc){
		
		Assert.notNull(cfd,"El comprobante no puede ser nulo");
		try {
			/*
			JavaMailSenderImpl mailSender=new JavaMailSenderImpl();
			mailSender.setHost("smtp.gmail.com");
			mailSender.setUsername("creditopapelsa1@gmail.com");
			mailSender.setPassword("papelsahijk");
			Properties props=new Properties();
			props.setProperty("mail.smtp.auth", "true");
			props.setProperty("mail.smtp.starttls.enable", "true");
			props.setProperty("mail.smtp.port", "587");
			props.setProperty("mail.debug", "true");
			*/
			MimeMessage mimeMessage=mailSender.createMimeMessage();
			
			MimeMessageHelper messageHelper=new MimeMessageHelper(mimeMessage,true);
			
			messageHelper.setFrom("ventas@papelsa.com.mx");
			messageHelper.setTo(to);
			messageHelper.setCc(cc);
			messageHelper.setSubject("Comprobante fiscal digital (CFD) de "+cfd.getEmisor());
			messageHelper.setText(getHtml(cfd.getReceptor(), cfd));
			String text=getHtml(cfd.getReceptor(), cfd);
			messageHelper.setText(text);
			messageHelper.setReplyTo("noreplay");
			
			byte[] xml=cfd.getXml();
			InputStreamSource source=new ByteArrayResource(xml);
			messageHelper.addAttachment(cfd.getXmlFilePath(),source);
			
			JasperPrint jp=null;
			
			if(cfd.getTipo().equals("FACTURA")){
				jp=(JasperPrint)hibernateTemplate.execute(new HibernateCallback() {
					public Object doInHibernate(Session session) throws HibernateException,SQLException {
						Venta venta=(Venta)session.get(Venta.class, cfd.getOrigen());
						JasperPrint jp=CFDIPrintServices.impripirComprobante(venta, cfd, "", false);
						return jp;
					}
				});	
			}
			
			if(cfd.getTipo().equals("NOTA_CARGO")){
				jp=(JasperPrint)hibernateTemplate.execute(new HibernateCallback() {
					public Object doInHibernate(Session session) throws HibernateException,SQLException {
						NotaDeCargo nota=(NotaDeCargo)session.get(NotaDeCargo.class, cfd.getOrigen());
						//JasperPrint jp=CFDIPrintServices.impripirComprobante(venta, cfd, "", false);
						JasperPrint jp=CFDINotaDeCargoPrintServices.impripirComprobante(nota, cfd);
						return jp;
					}
				});
			}
			if(cfd.getTipo().equals("NOTA_CREDITO")){
				jp=(JasperPrint)hibernateTemplate.execute(new HibernateCallback() {
					public Object doInHibernate(Session session) throws HibernateException,SQLException {
						NotaDeCredito nota=(NotaDeCredito)session.get(NotaDeCredito.class, cfd.getOrigen());
						JasperPrint jp=CFDINotaPrintServices.impripirComprobante(nota, cfd);
						return jp;
					}
				});
			}
			
			if(jp!=null){
				
				byte[] pdf=JasperExportManager.exportReportToPdf(jp);
				InputStreamSource sourcePdf=new ByteArrayResource(pdf);
				String pdfName=StringUtils.replace(cfd.getXmlFilePath(), ".xml", ".pdf");
				messageHelper.addAttachment(pdfName,sourcePdf);
				
			}
			
			
			//messageHelper.addAttachment(fileName+".xml",xml);
			//messageHelper.addAttachment(fileName+".pdf", pdf);
			mailSender.send(messageHelper.getMimeMessage());
			exitosos.add(cfd);
		} catch (Exception e) {
			e.printStackTrace();
			String msg="Error preparando correo: \n"+ExceptionUtils.getRootCauseMessage(e);
			logger.error(msg);
			fallidos.add(cfd);
		}
		
	}
	
	public String getHtml(String clienteNombre,CFDI...cfds) throws Exception{
		String emisor=cfds[0].getEmisor();
		
		Date creado=cfds[0].getLog().getCreado();
		
		Configuration cfg=new Configuration();
		cfg.setDateFormat("dd/MM/yyyy");
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setClassForTemplateLoading(getClass(), "");
		
		String path="images2/papelLogo.jpg";
		ClassPathResource resource=new ClassPathResource(path);
		Map root=new HashMap();
		root.put("cliente", clienteNombre);
		root.put("emisor", emisor);
		root.put("cfds", cfds);
		root.put("fecha", creado);
		root.put("cid:papelLogo", resource);
		final Template temp=cfg.getTemplate("correoCFDI.ftl");			
		StringWriter out=new StringWriter();
		temp.process(root, out);			
		return out.toString();
					
	}
	
	protected void mandarInforme(Date fecha){
		try {
			MimeMessage mimeMessage=mailSender.createMimeMessage();
			MimeMessageHelper messageHelper=new MimeMessageHelper(mimeMessage,true);
			messageHelper.setFrom("cfd_auxiliar@papelsa.com.mx");
			messageHelper.setTo(new String[]{"lgodines@papelsa.com.mx"});
		//	messageHelper.setTo(new String[]{"luzmam_papelsabjio@prodigy.net.mx"});
		//	messageHelper.setTo(new String[]{"soporte_sist@papelsa.com.mx"});
			messageHelper.setCc("soporte_sist@papelsa.com.mx");
			messageHelper.setSubject("Reporte diario de envio de (CFD) de ");
			String text=getReportHtml(fecha);
			messageHelper.setText(text,true);
			messageHelper.setReplyTo("noreplay");
			mailSender.send(messageHelper.getMimeMessage());
		} catch (Exception e) {
			logger.error("Imposible mandar el reporte de envios CFD"+ExceptionUtils.getRootCauseMessage(e));
		}		
	}

	public String getReportHtml(Date fecha) throws Exception{		
		Configuration cfg=new Configuration();
		cfg.setDateFormat("dd/MM/yyyy");
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setClassForTemplateLoading(getClass(), "");
		Map root=new HashMap();
		root.put("totalExitosos", exitosos.size());
		root.put("totalFallidos", fallidos.size());
		root.put("fallidos", fallidos);
		root.put("total", exitosos.size()+fallidos.size());
		root.put("fecha", fecha);
		final Template temp=cfg.getTemplate("reporteDeCorreosCFD2.ftl");			
		StringWriter out=new StringWriter();
		temp.process(root, out);			
		return out.toString();
	}
	
	public String localizarEmail(Cliente c){
		List<CFDIClienteMails> mails=getHibernateTemplate().find("from CFDIClienteMails c where c.cliente.id=?",c.getId());
		return mails.isEmpty()?c.getEmai3():mails.get(0).getEmail1();
	}

	public JavaMailSender getMailSender() {
		return mailSender;
	}


	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}


	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
	public static void main(String[] args) throws Exception{
		final CFDI_MailServices service=ServiceLocator2.getCFDIMailServices();
		//String id="8a8a8199-4301c863-0143-01e30dc0-000b";
		//service.mandarPorCorreo(id);
		//service.madarPorCorreo(DateUtil.toDate("03/07/2012"));
		//service.madarPorCorreo("20/08/2012","20/08/2012");
		service.madarPorCorreoPorOrigen(DateUtil.toDate("10/01/2014"), "CRE");
		service.madarPorCorreoPorOrigen(DateUtil.toDate("10/01/2014"), "CAM");
		service.madarPorCorreoPorOrigen(DateUtil.toDate("10/01/2014"), "MOS");		
	}

}
