/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.luxsoft.siipap.replica.reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author ruben
 */
public class ProductosReader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, DBFException, IOException {
    	String path = "c:\\pruebas\\siipap-dbf\\ARTICULO.DBF";
        FileInputStream inputStream = new FileInputStream(path);
    	try{
    		
            //DBFReader reader = new DBFReader(inputStream);
    		DBFMapConverter reader=new DBFMapConverter(inputStream);
            //System.out.println(reader);

            // get the field count if you want for some reasons like the following
            //
            int numberOfFields = reader.getFieldCount();

            // use this count to fetch all field information
            // if required
            //
            for (int i = 0; i < numberOfFields; i++) {

                DBFField field = reader.getField(i);

                // do something with it if you want
                // refer the JavaDoc API reference for more details
                //
                //System.out.print(field.getName()+" ");
            }

            // Now, lets us start reading the rows
            
            //Object[] rowObjects;
            Map<String, Object> rowObjects;

            while ((rowObjects = reader.nextRecord()) != null) {
            	/*

                for (int i = 0; i < rowObjects.length; i++) {

                    System.out.print(rowObjects[i]+" ");
                }                
                System.out.println("");
                */
            	System.out.println(rowObjects);
            }
            
            
    	}finally{
    		inputStream.close();
    	}
        


    }
}
