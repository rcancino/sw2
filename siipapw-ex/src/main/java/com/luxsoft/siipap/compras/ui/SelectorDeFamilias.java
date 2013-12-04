package com.luxsoft.siipap.compras.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.replica.reader.DBFException;
import com.luxsoft.siipap.replica.reader.DBFMapConverter;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swx.catalogos.ProductoFormModel.Familia;

public class SelectorDeFamilias extends AbstractSelector<Familia>{
	
	public SelectorDeFamilias() {
		super(Familia.class, "Selector de líneas");
		
	}

	@Override
	protected List<Familia> getData() {
		return getFamilias();
	}

	@Override
	protected TableFormat<Familia> getTableFormat() {
		return GlazedLists.tableFormat(Familia.class, new String[]{"clave","nombre"},new String[]{"Familia","Descripción"});
	}
	
	/**
	 * Servicio temporal para accesar las familias de SIIPAP
	 * 
	 * @return
	 */
	public List<Familia> getFamilias(){
		List<Familia> fams=new ArrayList<Familia>();
		FileInputStream is;
		try {
			is = new FileInputStream("G:\\SIIPAP\\ARCHIVOS\\FAMARTIC.D00");
			DBFMapConverter reader=new DBFMapConverter(is);
			Map<String, Object> rowObjects;
            while ((rowObjects = reader.nextRecord()) != null) {
            	String clave=(String)rowObjects.get("FAMCLAVE");
            	String nombre=(String)rowObjects.get("FAMNOMBRE");
            	String detalle=(String)rowObjects.get("FAMTIPO");
            	if(detalle.equals("D")){
            		Familia f=new Familia(clave,nombre);
                	fams.add(f);
            	}
            }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (DBFException e) {
			e.printStackTrace();
		}
		
		return fams;
	}
	
	public static Familia seleccionar(){
		SelectorDeFamilias s=new SelectorDeFamilias();
		s.open();
		if(!s.hasBeenCanceled()){
			return s.getSelected();
		}
		return null;
	}

}
