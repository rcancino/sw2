package com.luxsoft.siipap.service.contabilidad;

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

import com.luxsoft.siipap.model.contabilidad.AsientoContable;
import com.luxsoft.siipap.model.contabilidad.Poliza;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ExportadorGenericoDePolizas {
	
	final DateFormat df=new SimpleDateFormat("dd-MMM-yyyy");
	final DateFormat df2=new SimpleDateFormat("ddMMM");
	
	public String validar(final Poliza poliza){
		
		for(AsientoContable as:poliza.getRegistros()){
			if(StringUtils.isBlank(as.getCuenta())){
				return "El registro:\n"+as.toString()+ "\n No tiene cuenta contable valida";
			}
		}
		return null;
	}
	
	
	public File exportar(final Poliza poliza,String pref){
		return exportar(poliza, "META-INF/templates/Poliza_Simple.ftl",pref);
	}
	
	/**
	 * 
	 * @param fecha
	 */
	@SuppressWarnings("unchecked")
	public File exportar(final Poliza poliza,final String plantilla,final String preffFile){
		
		final String recDir=System.getProperty("polizas.dir","C:\\PRUEBAS\\POLIZAS");
				//,"I:\\BDCOI20\\TEMPOLEG");
		final File dir=new File(recDir);
		if(!dir.exists()){
			dir.mkdir();
		}
		Configuration cfg=new Configuration();
		cfg.setDateFormat("dd/MM/yyyy");		
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setClassForTemplateLoading(ExportadorGenericoDePolizas.class, "/");
		
		Map root=new HashMap();
		root.put("poliza", poliza);
		root.put("asientos", poliza.getRegistros());
		
		try {
			
			final String fileName=getFileName(poliza.getFecha(),preffFile);
			final Template temp=cfg.getTemplate(plantilla);			
			
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
	
	public String getFileName(Date fecha,String pref){
		final String pattern=pref+"{0}{1}.POL";
		 
		final String fileName = MessageFormat.format(pattern
				, df2.format(fecha)
				,StringUtils.leftPad(String.valueOf(next++),2, '0')
				);
		return fileName;
	}
	
	/**
	 * Exporta una poliza tomando  el nombre del archivo del objeto poliza
	 * 
	 * @param poliza
	 * @return
	 */
	public static File exportarACoi(final Poliza poliza,String recDir,String templatePath){
		if(StringUtils.isBlank(templatePath))
			templatePath="META-INF/templates/Poliza_Simple.ftl";
		if(StringUtils.isBlank(recDir))
			recDir=System.getProperty("polizas.dir","C:\\PRUEBAS\\POLIZAS");
		final File dir=new File(recDir);
		if(!dir.exists()){
			dir.mkdir();
		}
		Configuration cfg=new Configuration();
		cfg.setDateFormat("dd/MM/yyyy");		
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setClassForTemplateLoading(ExportadorGenericoDePolizas.class, "/");
		
		Map root=new HashMap();
		root.put("poliza", poliza);
		root.put("asientos", poliza.getRegistros());
		
		try {
			
			final String fileName=poliza.getExportName();
			final Template temp=cfg.getTemplate(templatePath);			
			
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
	

}
