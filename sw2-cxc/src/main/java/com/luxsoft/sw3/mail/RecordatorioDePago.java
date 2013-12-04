package com.luxsoft.sw3.mail;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class RecordatorioDePago {
	
	private String[] destinatariosConCopia;	

	
	public void execute(final List<Cargo> ventas,final Cliente cliente) throws MessagingException{
		
		final JavaMailSenderImpl sender=new JavaMailSenderImpl();
		sender.setHost("smtp.papelsa.com.mx");
		sender.setUsername("siipap_win@papelsa.com.mx");
		sender.setPassword("siipw");
		sender.getJavaMailProperties().put("mail.smtp.auth", Boolean.TRUE);
		
		final String text=crearRecordatorio(ventas,cliente);
		
		Assert.isTrue(StringUtils.isNotBlank(cliente.getEmail()),"El cliente no cuenta con correo electónico");
		String path="images/siipapwin/papelLogo.jpg";
		final DefaultResourceLoader loader=new DefaultResourceLoader();
		Resource resource=loader.getResource(path);
		if(resource.exists()){
			System.out.println("Image: "+resource.getFilename());
		}else
			System.out.println("No existe la imagen: "+path);
		if(confirmar(text,cliente)){
			final MimeMessagePreparator preparator=new MimeMessagePreparator(){
				public void prepare(MimeMessage mimeMessage) throws Exception {
					final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
					helper.setTo(cliente.getEmail());
					helper.setFrom("Papel S.A. Credito y cobranzas <ag@papelsa.com.mx>");
					if(getDestinatariosConCopia()!=null && getDestinatariosConCopia().length!=0)
						helper.setCc(getDestinatariosConCopia());
					helper.setSubject("Prueba de sistemas para recordatorio de pago");				
					helper.setText(text,true);
					helper.addInline("papelLogo", loader.getResource("images/siipapwin/papelLogo.jpg"));
				}				
			};
			System.out.println("Sending message: "+preparator);
			System.out.println(text);
			System.out.println("To: "+cliente.getEmail());
			sender.send(preparator);
		}		
	}
	
	@SuppressWarnings("unchecked")
	public  String crearRecordatorio(final List<Cargo> ventas,Cliente cliente) {		
		
		Configuration cfg=new Configuration();
		cfg.setDateFormat("dd/MM/yyyy");
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setClassForTemplateLoading(getClass(), "/");
		final String nombre=cliente.getNombre();		
		Map root=new HashMap();
		root.put("cliente", nombre);
		root.put("facturas", ventas);
		root.put("fecha", new Date());
		
		try {
			final Template temp=cfg.getTemplate("templates/recordatorio1.ftl");			
			StringWriter out=new StringWriter();
			temp.process(root, out);			
			return out.toString();
		} catch (Exception e) {
			throw new RuntimeException("Imposible cargar recordatorio",e);
		}
	}
	
	public  boolean confirmar(final String text,final Cliente c){
		
		final RecordatorioPreview dialog=new RecordatorioPreview(text,c);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			return true;
		}else{
			return false;
		}
	};
	
	public RecordatorioDePago setDestinatariosConCopia(String...destinatarios){
		this.destinatariosConCopia=destinatarios;
		return this;
	}
	
	
	
	public String[] getDestinatariosConCopia() {
		return destinatariosConCopia;
	}

	public static void main(String[] args) throws MessagingException {
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave("U050008");
		c.setEmail("cpradoglez@gmail.com");
		new RecordatorioDePago().execute(ListUtils.EMPTY_LIST,c);
	}

}
