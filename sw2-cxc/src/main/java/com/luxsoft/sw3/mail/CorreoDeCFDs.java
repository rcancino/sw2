package com.luxsoft.sw3.mail;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.collections.ListUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class CorreoDeCFDs {
	
	public void execute(final List<ComprobanteFiscal> cfds,final Cliente cliente) throws MessagingException{
		
		String path="images2/siipapwin/papelLogo.jpg";
		final DefaultResourceLoader loader=new DefaultResourceLoader();
		final String text=crearRecordatorio(cfds,cliente);
		Resource resource=loader.getResource(path);
		if(resource.exists()){
			System.out.println("Image: "+resource.getFilename());
		}else
			System.out.println("No existe la imagen: "+path);
		if(confirmar(text,cliente)){
			
		}		
	}
	
	@SuppressWarnings("unchecked")
	public  String crearRecordatorio(final List<ComprobanteFiscal> cfds,Cliente cliente) {		
		
		Configuration cfg=new Configuration();
		cfg.setDateFormat("dd/MM/yyyy");
		Locale mx=new Locale("es","mx");
		cfg.setEncoding(mx, "UTF-8");
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setClassForTemplateLoading(getClass(), "/");
		final String nombre=cliente.getNombreRazon();		
		Map root=new HashMap();
		root.put("cliente", nombre);
		root.put("cfds", cfds);
		root.put("fecha", new Date());
		root.put("remitenteMail", "credito@papelsa.com.mx");
		try {
			final Template temp=cfg.getTemplate("templates/correoDeCFD.ftl");			
			StringWriter out=new StringWriter();
			temp.process(root, out);			
			return out.toString();
		} catch (Exception e) {
			throw new RuntimeException("Imposible cargar recordatorio",e);
		}
	}
	
	public  boolean confirmar(final String text,final Cliente c){
		
		final CorreoPreview dialog=new CorreoPreview(text,c);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			return true;
		}else{
			return false;
		}
	};
	
	

	public static void main(String[] args) throws MessagingException {
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave("U050008");
		c.setEmail("cpradoglez@gmail.com");
		new CorreoDeCFDs().execute(ListUtils.EMPTY_LIST,c);
	}

}
