package com.luxsoft.siipap.pos.pruebas;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class PruebasDeImpresion {

	public static void main(String[] args) {
		try {
			FileOutputStream fout=new FileOutputStream("lpt1");
			OutputStreamWriter out = new OutputStreamWriter(fout,"ISO-8859-1");//new FileWriter("lpt1");
			out.write("Prueba de impresión directa");
			out.write(0x0D); // CR
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
