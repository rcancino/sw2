package com.luxsoft.sw3.cfd.services.validacion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.luxsoft.utils.LoggerHelper;

/**
 * Valida la existencia y vigencia del certificado de sello digital
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ValidarVigenciaDelCSD {
	
	private static Logger logger=LoggerHelper.getLogger();
	
	public static String validar(String serieCSD,String rfc,Date fecha){
		Assert.notNull(rfc,"El RFC es mandatorio..");
		Assert.notNull(fecha,"Fecha mandatoria..");
		FileReader fileReader=null;
		BufferedReader reader=null;
		try {
			DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			File file=new File("c:\\basura\\tempo\\CSD.txt");
			fileReader=new FileReader(file);
			reader=new BufferedReader(fileReader);
			String line=reader.readLine();
			do{
				line=reader.readLine();
				if(line!=null){
					
					String[] partes=StringUtils.split(line, '|');
					String serie=partes[0];
					Date fechaInicial=df.parse(partes[1]);
					Date fechaFinal=df.parse(partes[2]);	
					if(rfc.equals(partes[3])){
						System.out.println(line);
						if(serie.equalsIgnoreCase(serieCSD) ){						
							if((fechaInicial.compareTo(fecha)<=0) 
									&&(fechaFinal.compareTo(fecha)>=0) 
									){
								//System.out.println("Serie del CSD OK: "+line);								
								String pattern="No Serie CSD: {0} : OK" +
										"\n RFC {1}:  OK" +
										"\n Vigencia: {2,date,long} - {3,date,long}: OK" +
										"\n Archivo usado: {4} ";
								String msg=MessageFormat.format(pattern
										, serie
										,rfc
										,fechaInicial
										,fechaFinal
										,file.getAbsolutePath()
										);
								logger.info(msg);
								return "La serie del certificado digital y su vigencia son correctos segun consta en los registros del SAT" +
										"\n Archivo: "+file.getName()+ " Actualizado:"+df.format(new Date(file.lastModified()));
							}else{
								throw new RuntimeException("La número de serie solicitado existe " +
										"en los registros del SAT pero la fecha indicada no esta en el periodo de vigencia del CSD "
										+"\n Fecha indicada: "+df.format(fecha)
										+ "Periodo del CSD:"+partes[1]+ " - "+partes[2]);
							}
						}
						
					}
				}
							
			}while(line!=null);
			throw new RuntimeException("No existen registros de CSD para el RFC solicitado: "+rfc);
		} catch (Exception e) {
			throw new RuntimeException(ExceptionUtils.getRootCauseMessage(e)
					,ExceptionUtils.getRootCause(e));
			
		}finally{
			try {
				reader.close();
				fileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	
		
	}
	public static void main(String[] args) throws ParseException {
		DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(validar("00001000000101054201","INT990114450",df.parse("2009-12-15 21:02:36")));
	}

}
