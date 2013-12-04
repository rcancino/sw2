package com.luxsoft.sw3.mail;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.uif.component.UIFButton;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;

//import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Genera un texto adecuado para recordar a un cliente de
 * un grupo de cuentas por cobrar
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public class RecordatorioDePagoFactory {
	
	/**
	 * Genera y manda un recordatorio de pago para los cargos pendientes
	 * 
	 * @param ventas
	 */
	public static void  enviarRecordatorioDePago(final List<Cargo> ventas) {		
		
		Configuration cfg=new Configuration();
		cfg.setDateFormat("dd/MM/yyyy");		
		//cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));		
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setClassForTemplateLoading(RecordatorioDePagoFactory.class, "/");
		final String nombre=ventas.get(0).getNombre();
		final String clave=ventas.get(0).getClave();
		Map root=new HashMap();
		root.put("cliente", nombre);
		root.put("cargos", ventas);
		root.put("fecha", new Date());
		
		try {
			final String pattern="{0}_rec_{1}.html";
			 
			final String fileName = MessageFormat.format(pattern, clave,obtenerConsecutivo());
			final Template temp=cfg.getTemplate("templates/recordatorio1.ftl");
			
			final String recDir=System.getProperty("recordatorios.dir",System.getProperty("user.home"));
			final File userDir=new File(recDir);
			final File target=new File(userDir,fileName);			
			final FileOutputStream os=new FileOutputStream(target);
			final Writer out=new OutputStreamWriter(os);
			temp.process(root, out);
			out.flush();
			out.close();
			os.close();
			
			boolean val=confirmar(target.toURI().toURL());
			if(val){
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}		
	}
	
	private static String obtenerConsecutivo(){
		final Date hoy=new Date();
		DateFormat df=new SimpleDateFormat("dd_MM_yy_HH_mm");
		return df.format(hoy);
	}
	
	public static boolean confirmar(final URL url){
		
		final RecordatorioPreviw dialog=new RecordatorioPreviw(url);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			return true;
		}else{
			return false;
		}
	}
	 
	
	public static void main(String[] args) throws IOException, TemplateException {
		/*ApplicationContext ctx=new ClassPathXmlApplicationContext("swx-cxc-services.xml");
		JavaMailSender sender=(JavaMailSender)ctx.getBean("mailSender");
		sender.send(new MimeMessagePreparator(){
			public void prepare(MimeMessage mimeMessage) throws MessagingException {
				 MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				 message.setFrom("rcancino@luxsoftnet.com");
				 message.setTo("siipap_win@papelsa.com.mx");
				 message.setSubject("Recordatorio de pago");
				 message.setText("HTML TEXT", true);				 
				 }
		});
		*/
		//sender.send(msg);
		
		
		
	}
	
	public static class RecordatorioPreviw extends SXAbstractDialog {
		
		private URL url;
		private String text;
		
		public RecordatorioPreviw(final URL url) {
			super("Recordatorio de pago");
			this.url=url;
		}
		
		public RecordatorioPreviw(final String text) {
			super("Recordatorio de pago");
			this.text=text;
			
		}

		JEditorPane editor;
		//final String urlPath;

		@Override
		protected JComponent buildContent() {
			final JPanel panel=new JPanel(new BorderLayout());
			editor=new JEditorPane();
			editor.setEditable(false);
			panel.add(new JScrollPane(editor),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			panel.setPreferredSize(new Dimension(800,450));
			return panel;
		}

		@Override
		protected void setResizable() {
			setResizable(true);
		}

		@Override
		protected void onWindowOpened() {
			if(url!=null)
				System.out.println("Tratando de localizar: "+"templates/"+url.getPath());
			if(url!=null){
				try {					
					editor.setPage(url);					
				} catch (IOException e) {
					e.printStackTrace();
					editor.setText("No se localizo : "+url.getPath());
				}
			}else if(text!=null){
				editor.setContentType("text/html");
				editor.setText(text);
			}
			
		}
		
		protected JComponent buildButtonBarWithOKCancel() {
			JButton[] btns=new JButton[]{
					createPrintButton(),
					createOKButton(true),
		            createCancelButton()
		            
		            };
	        JPanel bar = ButtonBarFactory.buildRightAlignedBar(btns);		            
	        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);		        
	        return bar;
	    }
		
		protected UIFButton createPrintButton() {
			final Action a=CommandUtils.createPrintAction(this, "print");
	        return new UIFButton(a);
	    }
		
		public void print(){
			try {
				editor.print();
			} catch (PrinterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	};

}
