package com.luxsoft.siipap.replica;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.Periodo;

@SuppressWarnings("unchecked")
public class ExportUtils {
	
	static Logger logger=Logger.getLogger(ExportUtils.class);
	
	
	/**
	 * Resuelve el nombre de un archivo fechado eje ALMACE para enero es ALMACE01
	 * 
	 * @param archivo
	 * @param date
	 * @return
	 */
	public static String resolverArchivo(final String archivo,final Date date){		
		Calendar c=Calendar.getInstance();
		c.setTime(date);
		c.getTime();
		String month=String.valueOf(c.get(Calendar.MONTH)+1);
		String suf=StringUtils.leftPad(month,2,'0');
		return archivo+suf;
	}
		
	/**
	 * Valida que el periodo proporcionado esta comprendido en el mismo año mes
	 * 
	 * @param mes
	 */
	public static void validarMismoMes(final Periodo mes){
		final int year1=Periodo.obtenerYear(mes.getFechaInicial());
		final int year2=Periodo.obtenerYear(mes.getFechaFinal());		
		Assert.isTrue(year1==year2,"El periodo no esta contenido en un mismo año"+mes);
		final int mes1=Periodo.obtenerMes(mes.getFechaInicial());
		final int mes2=Periodo.obtenerMes(mes.getFechaFinal());
		Assert.isTrue(mes1==mes2,"El periodo no esta contenido en un mismo mes "+mes);
	}
	
	/**
	 * Obtiene el sufijo adecuado para un archivo fechado para el mes indicado
	 * El mes es el proporcionado es 1 based
	 * Eje: Enero=1
	 * 		Febrero=2
	 * @param mes
	 * @return
	 */
	public static String resolverSufijoDeArchivo(int mes){
		String month=String.valueOf(mes);
		String suf=StringUtils.leftPad(month,2,'0');
		return suf;
	}
	
	/**
	 * Genera un archivo de texto ASCII tipo DOR
	 * a String ASCII adecuado para ser analizado 
	 * 
	 * @param data
	 * @param name
	 */
	public static String toDORFile(final String data){
		
		if(StringUtils.isBlank(data))
			return null;
		File dir=new File(getDestino());
		if(!dir.exists())
			dir.mkdir();
		final File target=new File(getDestino(),folio()+".DOR");			
		FileOutputStream os;
		try {
			os = new FileOutputStream(target);
			final Writer out=new OutputStreamWriter(os,"ISO-8859-1");
			out.write(data);
			out.flush();
			out.close();
			os.close();
			String msg="Archivo de replica generado: "+target.getAbsolutePath();
			logger.info(msg);
			return msg;
		} catch (Exception e) {
			logger.info("Error al generar archivo de replica: "+e.getMessage());
			logger.error(e);
			return null;
		}
	}
	
	
	public static String getDestino() {
		return System.getProperty("replica.target","C:\\PRUEBAS\\REPLICA\\");
	}
	
	/**
	 * Genera un consecutivo adecuado para usar como nombre del archivo de replica
	 * 
	 * @return
	 */
	public static String folio(){
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long time=System.currentTimeMillis();
		String res="20"+StringUtils.substring(String.valueOf(time),-6);
		return res;
	}
	
}
