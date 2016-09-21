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


public class CFDI_EnvioServices {
	
	
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	
	
	
	private Set fallidos=new HashSet();
	private Set exitosos=new HashSet();
	long totalCfds=0;
	
	Logger logger=LoggerHelper.getLogger();
	
	public void madarPorCorreo(){
		System.out.println("Enviando....");
		madarPorCorreo(new Date());
	}
	

	@SuppressWarnings("unchecked")
	public void madarPorCorreo(Date dia){
		
		System.out.println("Enviando Por Dia...");
		fallidos=new HashSet();
		exitosos=new HashSet();
		//String hql="select c.id from ComprobanteFiscal c where date(c.log.creado)=?";
		String hql="select c.id from CFDI c where date(c.log.creado)=? and c.rfc!='XAXX010101000'"
				+"  and  comentario is null ";
		
		List<String> cfds=ServiceLocator2.getHibernateTemplate().find(hql, new Object[]{dia});
		totalCfds=cfds.size();
		for(String cfd:cfds){
			try {
				System.out.println( "-------------"+cfd);
				mandarPorCorreo(cfd);
			} catch (Exception e) {
				logger.error(ExceptionUtils.getRootCauseMessage(e),e);
			}
		}
		System.out.println("Ya casi termino");
		//mandarInforme(dia);
	}
	
	
	

	
	public void mandarPorCorreo(final String cfdId){
		final CFDI cfd=(CFDI)getHibernateTemplate().get(CFDI.class, cfdId);
		final Cargo cargo;
		Boolean enviar= true;
			
		if(cfd.getTipo().equals("I")){
			 cargo=(Cargo)getHibernateTemplate().get(Cargo.class,cfd.getOrigen());
			 Cliente clienteVenta=cargo.getCliente();
				
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
		
		if(data.size()>1){
			Cargo  venta=(Cargo)getHibernateTemplate().get(Cargo.class,cfd.getOrigen());
			 c= venta.getCliente();
			 System.err.println("Localizando La venta para "+c.getNombre()+"-----------"+c.getRfc());
		}
		
		List<String> mailCte=getHibernateTemplate().find("select email1 from CFDIClienteMails c where c.cliente.id=?",c.getId());
		Assert.notEmpty(mailCte,"No existe el mail del Cte: "+c.getNombre());
		 String mailEnv=mailCte.get(0);
		
		System.out.println("Enviando correo a:-----------------------  "+mailEnv + " CFDI   "  + cfd.getFolio() +" Cliente: " +cfd.getReceptor()+" Origen_id: "+cfd.getOrigen());
	
		if(mailEnv!=null){	
			if(enviar){

				
			madarPorCorreo(cfd,mailEnv,"cfd_auxiliar@papelsa.com.mx");
//			madarPorCorreo(cfd,"creditopapelsa@gmail.com","cfd_auxiliar@papelsa.com.mx");
			}else{
				System.out.println("El Documento esta cancelado  "+ cfd.getFolio() +" Cliente: " +cfd.getReceptor()+" Origen_id: "+cfd.getOrigen());
			}

		}else{
			fallidos.add(cfd);
			logger.info("CFD con cliente sin correo para CFD definido cliente:"+c+"  CFD: "+cfd);
		}
		
	}
	
	public void madarPorCorreo(final CFDI cfd,String to,String cc){
	
			
			//ServiceLocator2.getHibernateTemplate().merge(cfd);
	
		
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
			
			byte[] xml=cfd.getXml();
			InputStreamSource source=new ByteArrayResource(xml);
			
			//messageHelper.addAttachment(cfd.getXmlFilePath(),source);
			String receptor=StringUtils.replace(cfd.getReceptor(),".","");
			    
				int indice=receptor.indexOf(",");
			   
			   if (indice==-1){
				   indice=receptor.length();
			   }
				   
			   System.out.println("**************************"+indice);
			String xmlName=receptor.substring(0, indice).concat("-").concat(cfd.getFolio());
			 
			messageHelper.addAttachment(xmlName+".xml",source);
			
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
				//String pdfName=StringUtils.replace(cfd.getXmlFilePath(), ".xml", ".pdf");
				String pdfName=receptor.substring(0, indice).concat("-").concat(cfd.getFolio());
				
				//messageHelper.addAttachment(pdfName,sourcePdf);
				messageHelper.addAttachment(pdfName+".pdf",sourcePdf);
				
			}
			
			mailSender.send(messageHelper.getMimeMessage());
			exitosos.add(cfd);
			cfd.setComentario("Enviado: "+DateUtil.getDateTime("dd/MM/yyyy hh:mm:ss",new Date()));
			ServiceLocator2.getHibernateTemplate().merge(cfd);
			
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
		final CFDI_EnvioServices service=ServiceLocator2.getCFDIEnvioServices();
		 Date dia = DateUtil.toDate("19/09/2016");
		service.madarPorCorreo(dia);
				//service.mandarPorCorreo("8a8a82fa-432f6a82-0143-30f0da04-002d");
	}

}
