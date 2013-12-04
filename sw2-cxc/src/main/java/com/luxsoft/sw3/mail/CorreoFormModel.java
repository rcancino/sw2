package com.luxsoft.sw3.mail;

import java.io.StringWriter;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.validator.Email;
import org.hibernate.validator.NotNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;


public class CorreoFormModel extends DefaultFormModel{
	
	private List<ComprobanteFiscal> cfds;
	
	public static String DEFAULT_CC1="credito@papelsa.com.mx";

	public CorreoFormModel() {
		this(ListUtils.EMPTY_LIST);
		
	}
	
	public CorreoFormModel(List<ComprobanteFiscal> cfds) {
		super(Bean.proxy(CorreoModel.class));
		this.cfds=cfds;
		setValue("from", DEFAULT_CC1);
	}
	
	public CorreoModel getCorreo(){
		return (CorreoModel)getBaseBean();
	}
	
	private String htmlText;
	
	public String getHtml() {
		if(htmlText==null){
			Configuration cfg=new Configuration();
			cfg.setDateFormat("dd/MM/yyyy");
			cfg.setObjectWrapper(new DefaultObjectWrapper());
			cfg.setClassForTemplateLoading(getClass(), "/");
			final String nombre=getCorreo().getCliente().getNombre();
			String path="images2/papelLogo.jpg";
			ClassPathResource resource=new ClassPathResource(path);
			Map root=new HashMap();
			root.put("cliente", nombre);
			root.put("cfds", cfds);
			root.put("fecha", new Date());
			root.put("remitenteMail",DEFAULT_CC1 );
			root.put("cid:papelLogo", resource);
			try {
				String cfdiCorreoTemplate=System.getProperty("cfdi.correo.template.path", "templates/correoDeCFD.ftl");
				final Template temp=cfg.getTemplate(cfdiCorreoTemplate);			
				StringWriter out=new StringWriter();
				temp.process(root, out);			
				htmlText=out.toString();
			} catch (Exception e) {
				logger.error(e);
				htmlText="Error en template: "+ExceptionUtils.getRootCauseMessage(e);
			}
		}
		return htmlText;
					
	}
	
	public void commit(){
		//String path="images2/papelLogo.jpg";
		//ClassPathResource resource=new ClassPathResource(path);
		
		try {
			MimeMessageHelper msg=ServiceLocator2.getCXCMailServices()
			.prepararCorreoElectronico(
					getCorreo().getFrom()
					,getCorreo().getTo()
					, cfds.toArray(new ComprobanteFiscal[0]));
			
			
			if(StringUtils.isNotEmpty(getCorreo().getCc1())){
				msg.setBcc(getCorreo().getCc1());
			}
			if(StringUtils.isNotEmpty(getCorreo().getCc2())){
				msg.setBcc(getCorreo().getCc2());
			}
			if(StringUtils.isNotEmpty(getCorreo().getCc3())){
				msg.setBcc(getCorreo().getCc3());
			}
			msg.setSubject("Comprobantes fiscales digitales");
			msg.setText(getHtml(),true);
			//msg.addInline("cid:papelLogo", resource);
			ServiceLocator2.getCXCMailServices().mandarCorreo(msg);
			System.out.println("Correo enviado..");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(ExceptionUtils.getRootCauseMessage(e),e);
		}
	}
	
	
	
	public static class CorreoModel {
		
		private Cliente cliente;
		
		private URL templateUrl;
		
		@Email
		@NotNull(message="Digite el Remitente")
		private String from;
		
		@Email
		@NotNull(message="Digite el correo destino")
		private String to;
		
		@Email
		private String cc1;
		
		@Email
		private String cc2;
		
		@Email
		private String cc3;
		
		public Cliente getCliente() {
			return cliente;
		}
		public void setCliente(Cliente cliente) {
			this.cliente = cliente;
			setTo(cliente!=null?cliente.getEmai3():null);
		}
		public URL getTemplateUrl() {
			return templateUrl;
		}
		public void setTemplateUrl(URL templateUrl) {
			this.templateUrl = templateUrl;
		}
		public String getFrom() {
			return from;
		}
		public void setFrom(String from) {
			this.from = from;
		}
		public String getTo() {
			return to;
		}
		public void setTo(String to) {
			this.to = to;
		}
		public String getCc1() {
			return cc1;
		}
		public void setCc1(String cc1) {
			this.cc1 = cc1;
		}
		public String getCc2() {
			return cc2;
		}
		public void setCc2(String cc2) {
			this.cc2 = cc2;
		}
		public String getCc3() {
			return cc3;
		}
		public void setCc3(String cc3) {
			this.cc3 = cc3;
		}
		public String getClienteNombre(){
			return getCliente().getNombreRazon();
		}
		
		
	}

}
