package com.luxsoft.siipap.service.gastos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.luxsoft.siipap.model.contabilidad.Poliza;
import com.luxsoft.siipap.service.contabilidad.ExportadorGenericoDePolizas;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * 
 * @author Ruben Cancino
 * @deprecated Usar {@link ExportadorGenericoDePolizas}
 *
 */
public class GeneradorDePolizaDeGastos {
	
	final DateFormat df=new SimpleDateFormat("dd-MMM-yyyy");
	final DateFormat df2=new SimpleDateFormat("ddMMM");
	
	
	/**
	 * 
	 * @param fecha
	 */
	@SuppressWarnings("unchecked")
	public File generar(final Poliza poliza){
		
		final String recDir=System.getProperty("polizas.dir"				
				,"I:\\BDCOI20\\TEMPOLEG");
				//,"C:\\BASURA\\POL");
		final File dir=new File(recDir);
		if(!dir.exists()){
			dir.mkdir();
		}
		Configuration cfg=new Configuration();
		cfg.setDateFormat("dd/MM/yyyy");		
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setClassForTemplateLoading(GeneradorDePolizaDeGastos.class, "/");
		
		Map root=new HashMap();
		root.put("poliza", poliza);
		root.put("asientos", poliza.getRegistros());
		
		try {
			
			final String fileName=getFileName(poliza.getFecha());
			final Template temp=cfg.getTemplate("META-INF/templates/Poliza_Simple.ftl");			
			
			final File target=new File(dir,fileName);
			
			final FileOutputStream os=new FileOutputStream(target);
			final Writer out=new OutputStreamWriter(os);
			temp.process(root, out);
			out.flush();
			out.close();
			os.close();
			return target;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	int next=1;
	
	public String getFileName(Date fecha){
		final String pattern="G{0}{1}.POL";
		 
		final String fileName = MessageFormat.format(pattern
				, df2.format(fecha)
				,StringUtils.leftPad(String.valueOf(next++),2, '0')
				);
		return fileName;
	}
	
	
	
	public static void main(String[] args) {
		GeneradorDePolizaDeGastos poliza=new GeneradorDePolizaDeGastos();
		//Poliza pol=ServiceLocator2.getGCompraDao().generarPoliza(198936l);
		//poliza.generar(pol);
	}

}
