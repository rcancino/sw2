package com.luxsoft.sw3.cfd.services;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.MimeMessage;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.utils.LoggerHelper;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class CFDMailServices {
	
	
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	
	
	private Set fallidos=new HashSet();
	private Set exitosos=new HashSet();
	private long totalCfds=0;
	
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
	
	public void madarPorCorreo(Date dia){
		fallidos=new HashSet();
		exitosos=new HashSet();
		//String hql="select c.id from ComprobanteFiscal c where date(c.log.creado)=?";
		String hql="select c.id from ComprobanteFiscal c where date(c.log.creado)=? and c.rfc!='XAXX010101000'"
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
	
	
	public void madarPorCorreoPorOrigen(Date dia,String origen){
		String hql="";
		fallidos=new HashSet();
		exitosos=new HashSet();
		System.out.println("Enviando Cfd's deñ dia: " +dia +" Origen: " + origen );
		 	
		 if (origen.equals(new String("CRE"))){
			
		     hql="select c.id from ComprobanteFiscal c where date(c.log.creado)=? and c.rfc!='XAXX010101000'"
						+"  and c.serie like '%CRE'  ";
		 }
		 if (origen.equals(new String("CAM"))){
				
			  hql="select c.id from ComprobanteFiscal c where date(c.log.creado)=? and c.rfc!='XAXX010101000'"
						+"  and c.serie like '%CAM'  ";
			
		 }
		 if (origen.equals(new String("MOS"))){
				
			 hql="select c.id from ComprobanteFiscal c where date(c.log.creado)=? and c.rfc!='XAXX010101000'"
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
		//logger.info("Mandando correo para cfd: "+cfdId);
		final ComprobanteFiscal cfd=(ComprobanteFiscal)getHibernateTemplate().get(ComprobanteFiscal.class, cfdId);
		final Cargo cargo;
		Boolean enviar= true;
			
		if(cfd.getTipoCfd().equals("I")){
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
		if(StringUtils.isNotBlank(c.getEmai3())){
			if(enviar){
				madarPorCorreo(cfd,c.getEmai3(),"cfd_auxiliar@papelsa.com.mx");
			//	madarPorCorreo(cfd,"soporte_sist@papelsa.com.mx","cfd_auxiliar@papelsa.com.mx");
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
	
	public void madarPorCorreo(ComprobanteFiscal cfd,String to,String cc){
		
		Assert.notNull(cfd,"El comprobante no puede ser nulo");
		try {
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
			
			String fileName=cfd.getSerie()+cfd.getFolio();
			
			URL url;
			if(cfd.getXmlPath().startsWith("file")){
				 url=new URL(cfd.getXmlPath());
			}else{
				String path="file:/"+System.getProperty("cfd.dir.path")+"/"+cfd.getXmlPath();
				url=new URL(path);
			}
			
			
			File xml=new File(url.toURI());
			Assert.isTrue(xml.exists(),"No localiza el XML");			
			messageHelper.addAttachment(fileName+".xml",xml);
			
			
			File pdf=CFDPdfUtils.getStandarFile(cfd);
			
			Assert.isTrue(pdf.exists(),"No localiza el PDF: "+pdf.getAbsolutePath());
			messageHelper.addAttachment(fileName+".pdf", pdf);
		
			mailSender.send(messageHelper.getMimeMessage());
			//logger.info("Correo enviado para CFD: "+cfd.getId()+ " Emai: "+to );
			exitosos.add(cfd);
		} catch (Exception e) {
			e.printStackTrace();
			String msg="Error preparando correo: \n"+ExceptionUtils.getRootCauseMessage(e);
			logger.error(msg);
			fallidos.add(cfd);
		}
		
	}
	
	public String getHtml(String clienteNombre,ComprobanteFiscal...cfds) throws Exception{
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
	//	root.put("fecha", new Date());
		root.put("fecha", creado);
		root.put("cid:papelLogo", resource);
		final Template temp=cfg.getTemplate("correoAutomaticoDeCFD.ftl");			
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
		final CFDMailServices service=ServiceLocator2.getCFDMailServices();
		//String id="8a8a8783-380f543d-0138-0f86dd6f-0096";
		//service.mandarPorCorreo(id);
		//service.madarPorCorreo(DateUtil.toDate("03/07/2012"));
		//service.madarPorCorreo("20/08/2012","20/08/2012");
		//service.madarPorCorreoPorOrigen(DateUtil.toDate("28/09/2013"), "CRE");
		//service.madarPorCorreoPorOrigen(DateUtil.toDate("28/09/2013"), "CAM");
		service.madarPorCorreoPorOrigen(DateUtil.toDate("10/12/2013"), "MOS");		
	}

}
