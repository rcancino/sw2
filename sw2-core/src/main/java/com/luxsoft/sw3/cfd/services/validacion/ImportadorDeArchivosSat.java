package com.luxsoft.sw3.cfd.services.validacion;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.utils.FTPUtils;

public class ImportadorDeArchivosSat {
	
	
	
	public static void cargarCSD(){
		List<String> rows=leerArchivo("file:C:/basura/tempo/CSD.txt");
		DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String INSERT="INSERT INTO CSD VALUES(?,?,?,?,?)";
		long line=1;
		for(String row:rows){
			try {
				String[] partes=StringUtils.split(row, '|');
				System.out.println(ArrayUtils.toString(partes));
				String serie=partes[0];
				System.out.println(partes[1]);
				Date fechaInicial=df.parse(partes[1]); 
				System.out.println("Fecha Inicial: "+fechaInicial);
				Date fechaFinal=df.parse(partes[2]);
				String rfc=partes[3];
				String estado=partes[4];
				Object args[]=new Object[]{
						serie
						,new SqlParameterValue(Types.TIMESTAMP,fechaInicial)
						,new SqlParameterValue(Types.TIMESTAMP,fechaFinal)
						,rfc
						,estado						
				};
				ServiceLocator2.getJdbcTemplate().update(INSERT, args);
				
				System.out.println("Linea: "+line+"  "+row);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			line++;
		}
	}
	
	public static List<String> leerArchivo(String path) {
		final List<String> ciudades=new ArrayList<String>();
		try {
			final DefaultResourceLoader loader=new DefaultResourceLoader();
			Resource rs=loader.getResource(path);
			Reader ri=new InputStreamReader(rs.getInputStream());
			BufferedReader reader=new BufferedReader(ri);
			String line=null;
			do{
				line=reader.readLine();
				if(line!=null)
					ciudades.add(line.trim());				
			}while(line!=null);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ciudades;
	}
	
	public static void downloadSatFilesV2() throws MalformedURLException, IOException{
		File destination=new File("c:\\basura\\tempo\\CSD.txt");
		FTPUtils.download("ftp2.sat.gob.mx"
				, null
				,null
				, "/agti_servicio_ftp/verifica_comprobante_ftp/CSD.txt"
				, destination);
	}
	
	

	
	public static void main(String[] args) throws MalformedURLException, IOException {
		downloadSatFilesV2();
		
	}

}
