package com.luxsoft.sw3.contabilidad.ui.reportes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class InformesUtils {
	
	
	public static void salvarEnArchivoDeTexto(List<String> lineas) throws Exception{
		JFileChooser chooser=new JFileChooser(new File(System.getProperty("user.dir")));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Texto","txt");
		chooser.setFileFilter(filter);

		int res=chooser.showSaveDialog(null);
		if(res==JFileChooser.APPROVE_OPTION){
			File target=chooser.getSelectedFile();
			System.out.println("Salvando a: "+target);
			FileOutputStream out=new FileOutputStream(target);
			OutputStreamWriter writer=new OutputStreamWriter(out, "UTF-8");
			BufferedWriter buf=new BufferedWriter(writer);
			for(String linea:lineas){
				buf.write(linea);
				buf.newLine();
			}
			buf.flush();
			buf.close();
			writer.close();
			out.flush();
			out.close();
		}
	}
	
	

}
