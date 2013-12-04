package com.luxsoft.siipap.cxc.ui.form;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.model.core.Comentario;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.replica.reader.DBFException;
import com.luxsoft.siipap.replica.reader.DBFMapConverter;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;

public class ProductoFormModel extends DefaultFormModel{
	
	
	private EventList<Comentario> comentarios;
	

	public ProductoFormModel() {
		super(Producto.class);
	}

	public ProductoFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public ProductoFormModel(Object bean) {
		super(bean);
	}
	
	protected void init(){
		comentarios=GlazedLists.eventList(new BasicEventList<Comentario>());
	}
	
	protected Producto getProducto(){
		return (Producto)getBaseBean();
	}
	
	public EventList<Comentario> getComentarios(){
		return GlazedLists.readOnlyList(comentarios);
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
	
	public static final class Familia implements Comparable<Familia>{
		private String clave;
		private String nombre;
		
		public Familia(String clave, String nombre) {
			this.clave = clave;
			this.nombre = nombre.trim();
		}
		
		public String getClave() {
			return clave;
		}
		
		public void setClave(String clave) {
			this.clave = clave;
		}
		
		public String getNombre() {
			return nombre;
		}
		
		public void setNombre(String nombre) {
			this.nombre = nombre;
		}
		public String toString(){
			return clave+" ("+nombre+")";
		}

		public int compareTo(Familia o) {
			return clave.compareTo(o.getClave());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clave == null) ? 0 : clave.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Familia other = (Familia) obj;
			if (clave == null) {
				if (other.clave != null)
					return false;
			} else if (!clave.equals(other.clave))
				return false;
			return true;
		}
		
	}

}
