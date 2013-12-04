package com.luxsoft.siipap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.util.Assert;



/**
 * Diversas utilerias para el manejo de archivos
 * 
 * @author Ruben Cancino
 *
 */
public final class FileUtils {
	
	/**
	 * Copia el contenido de un directorio a otro, si el directorio destino no existe lo crea
	 * 
	 * @param sourceDirPath
	 * @param targetDirPath
	 * @param filter
	 * @throws IOException 
	 */
	public static void copyDir(final String sourceDirPath,final String targetDirPath,final FilenameFilter filter) throws IOException{
		File sourceDir=new File(sourceDirPath);		
		File targetDir=new File(targetDirPath);
		if(!targetDir.exists()){
			targetDir.mkdirs();
		}
		copyDir(sourceDir, targetDir, filter);		
	}	
	
	/**
	 * Copia el contenido de un directorio a otro
	 * 
	 * 
	 * @param sourceDir
	 * @param targetDir
	 * @param filter
	 * @throws IOException 
	 */
	public static void copyDir(final File sourceDir,final File targetDir,final FilenameFilter filter) throws IOException{
		Assert.isTrue(sourceDir.isDirectory());
		Assert.isTrue(targetDir.isDirectory());
		final File[] files;
		if(filter!=null)
			files=sourceDir.listFiles();
		else
			files=sourceDir.listFiles(filter);
		for(File file:files){
			if(file.isFile()){				
				copiFile(file,targetDir);
			}
		}
	}
	
	/**
	 * Copia un archivo a otro directorio
	 * 
	 * @param f
	 * @param targetDir
	 * @throws IOException 
	 */
	public static void copiFile(final File f,final File targetDir) throws IOException{
		
		Assert.isTrue(f.isFile());		
		Assert.isTrue(targetDir.isDirectory());
		
		String name=f.getName();
		File target=new File(targetDir,name);		
		target.createNewFile();
		FileChannel srcChannel=new FileInputStream(f).getChannel();
		FileChannel targetChannel=new FileOutputStream(target).getChannel();
		targetChannel.transferFrom(srcChannel, 0, srcChannel.size());
		targetChannel.close();
		srcChannel.close();
	}
	
	
	
	/**
	 * Copia el contenido de un archivo en otro archivo 
	 * Util para copiar un archivo con otro nombre ya sea en el mismo directorio o en otro
	 * utiliza las rutas indicadas para crear el objeto File correspondiente para el origen
	 * y el destino
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public static void copyFile(String source,String target)throws IOException{		
		File in=new File(source);
		File out=new File(target);
		copyFile(in, out);
	}
	
	/**
	 * Copia el contenido de un archivo en otro archivo 
	 * Util para copiar un archivo con otro nombre ya sea en el mismo directorio o en otro
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public static void copyFile(File source,File target) throws IOException{
		Assert.isTrue(source.isFile(),"El parametro source no es archivo :"+source.getAbsolutePath());		
		target.createNewFile();
		FileChannel srcChannel=new FileInputStream(source).getChannel();
		FileChannel targetChannel=new FileOutputStream(target).getChannel();
		targetChannel.transferFrom(srcChannel, 0, srcChannel.size());		
	}
	
	/**
	 * Salva a un archivo el contenido del string
	 * 
	 * @param data
	 * @param target
	 */
	public static void saveFile(Collection<String> data,String target) {		
		
		try {
			File f=new File(target);
			if(!f.exists()){
				f.createNewFile();
			}
			FileOutputStream os=new FileOutputStream(f,false);
			OutputStreamWriter osw=new OutputStreamWriter(os,"US-ASCII");
			PrintWriter writer=new PrintWriter(osw);
			for(String d:data){
				writer.println(d);
			}			
			writer.flush();
			writer.close();
			osw.close();
		} catch (Exception e) {
			throw new RuntimeException(ExceptionUtils.getRootCauseMessage(e),e);
		}
	}
	
	
	
	
	public static void main(String[] args) throws IOException {
		/*long start=System.currentTimeMillis();
		copyFile("C:\\basura\\ARSALD05.ACU", "C:\\basura\\ARSALD.dbf");
		long end=System.currentTimeMillis();
		System.out.println("Elapsed: "+(end-start));*/
		
		List<String> data=new ArrayList<String>();
		data.add("TEST1");
		data.add("TEST1");
		data.add("TEST1");
		saveFile(data, "C:\\basura\\DATA.txt");
	}

}
