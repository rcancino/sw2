package com.luxsoft.siipap.service.cxp;

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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.luxsoft.siipap.dao.cxp.CxPDao;
import com.luxsoft.siipap.model.cxp.CxPOldSupport;
import com.luxsoft.siipap.util.DateUtil;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Poliza_EgresosCxP {
	
	final DateFormat df=new SimpleDateFormat("dd-MMM-yyyy");
	final DateFormat df2=new SimpleDateFormat("ddMMM");
	
	int next=1;
	
	/**
	 * 
	 * @param rows
	 */
	@SuppressWarnings("unchecked")
	public void procesar(final Date fecha){
		
		List<CxPOldSupport.CxPGrupo> pagos=CxPDao.buscarGruposDePagos(fecha);
		for(CxPOldSupport.CxPGrupo pago:pagos){
			final Map root=new HashMap();			
			root.put("titulo", pago.getDescripcion()+" "+df.format(fecha));
			root.put("tituloBanco", StringUtils.substring(pago.getDescripcion(), 0, 30));
			root.put("fecha", df.format(fecha));
			root.put("pagos", pago.getPagos());
			root.put("total", pago.getTotal().doubleValue());
			root.put("cuentaBancaria", getCuenta(pago.getBanco()));
			root.put("tipo", pago.getFormaDePago().startsWith("T")?"Dr":getTipo(pago.getBanco()));
			root.put("iva", pago.getIva().doubleValue());
			root.put("ietu", pago.getIetu().doubleValue());
			generar(fecha, root);
		} 
	}
	
	private String getCuenta(final String banco){
		if(banco.startsWith("BAN")){
			return "102-0002-000";
		}else if(banco.startsWith("SCO")){
			return "102-0005-000";
		}else if(banco.startsWith("BBV")){
			return "102-0001-000";
		}else if(banco.startsWith("HSB")){
			return "102-0002-000";
		}else if(banco.startsWith("SAN")){
			return "102-0008-000";
		}else{
			return "000-0000-000";
		}
	}
	
	private String getTipo(final String banco){
		if(banco.startsWith("BAN")){
			return "E2";
		}else if(banco.startsWith("SCO")){
			return "E5";
		}else if(banco.startsWith("BBV")){
			return "E1";
		}else if(banco.startsWith("HSB")){
			return "E4";
		}else{
			return "E9";
		}
	}
	
	 
	
	/*
	public Cuenta localizarCuenta(final String banco){
		if(bancos==null){
			bancos=ServiceLocator2.getLookupManager().getBancos();
		}
		if(banco.startsWith("BANAMEX")){
			Banco banco=
		}else if(banco.startsWith("")){
			
		}else if(banco.startsWith("")){
			
		}
	}*/
	
	/**
	 * 
	 * @param fecha
	 */
	@SuppressWarnings("unchecked")
	public void generar(final Date fecha,Map root){
		
		final String recDir=System.getProperty("polizas.dir"
				//,System.getProperty("user.home")+"/POLIZA_EGRESOS");
			//,"I:\\BDCOI20\\TEMPOLEC");
			  ,"C:\\BASURA\\POL");
		final File dir=new File(recDir);
		if(!dir.exists()){
			dir.mkdir();
		}
		Configuration cfg=new Configuration();
		cfg.setDateFormat("dd/MM/yyyy");		
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setClassForTemplateLoading(Poliza_EgresosCxP.class, "/");
		
		
		try {
			
			final String fileName=getFileName(fecha);
			final Template temp=cfg.getTemplate("META-INF/templates/Poliza_EgresosCxP.ftl");			
			
			final File target=new File(dir,fileName);
			
			final FileOutputStream os=new FileOutputStream(target);
			final Writer out=new OutputStreamWriter(os);
			temp.process(root, out);
			out.flush();
			out.close();
			os.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
		
	}
	
	public String getFileName(Date fecha){
		final String pattern="E{0}{1}.POL";
		 
		final String fileName = MessageFormat.format(pattern
				, df2.format(fecha)
				,StringUtils.leftPad(String.valueOf(next++),2, '0')
				);
		return fileName;
	}
	
	public static void main(String[] args) {
		final Date fecha=DateUtil.toDate("13/06/2008");
		Poliza_EgresosCxP poliza=new Poliza_EgresosCxP();
		//poliza.generar(fecha);
		poliza.procesar(fecha);
	}

}
