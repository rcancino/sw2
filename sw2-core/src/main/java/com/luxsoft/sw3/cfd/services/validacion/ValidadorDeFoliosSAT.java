package com.luxsoft.sw3.cfd.services.validacion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.util.Assert;

public class ValidadorDeFoliosSAT {
	
	public static String validar(String rfc,String noAprobacion,int anoAprobacion,String serie,String folio){
	//public static String validar(){
		Assert.notNull(rfc,"El RFC es mandatorio..");
		Assert.notNull(noAprobacion,"Fecha mandatoria..");
		FileReader fileReader=null;
		BufferedReader reader=null;
		int rows=0;
		try {
			DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			File file=new File("c:\\basura\\tempo\\FoliosCFD.txt");
			fileReader=new FileReader(file);
			reader=new BufferedReader(fileReader);
			String line=reader.readLine();
			rows++;
			do{
				line=reader.readLine();
				if(line==null){
					System.out.println("EOF");
					continue;
				}
				String[] partes=StringUtils.split(line, '|');
				//System.out.println(ArrayUtils.toString(partes));
				if(rfc.equals(partes[0])){
					//System.out.println(line);
					if(noAprobacion.equals(partes[1])){
						Integer anoAp=Integer.valueOf(partes[2]);
						if(anoAprobacion==anoAp){
							if(serie.equals(partes[3])){
								Integer folIni=Integer.valueOf(partes[4]);
								Integer folFin=Integer.valueOf(partes[5]);
								Integer fol=Integer.valueOf(folio);								
								if((folIni<=fol) && (fol<=folFin)){
									//System.out.println(line);
									return "La serie y folio del comprobante se encuentran dentro de los registros del SAT " +
											"\n Archivo: "+file.getName()+ " Actualización: "+df.format(new Date(file.lastModified()));
								}else throw new RuntimeException("Rango de folio no valido");
							}
						}
					}
				}
				rows++;

				//System.out.println(line);
							
			}while(line!=null);
			//return "";
			throw new RuntimeException("**FOLIO INVALIDO** Los datos no se encuentran en el archivo del SAT registros procesados: "+rows);
		} catch (Exception e) {
			e.printStackTrace();
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
	
	public static void main(String[] args) {
		//validar("INT990114450","170400",2010,"A","2552");
		validar("INT990114450","170400",2010,"A","2552000");
	}

}
