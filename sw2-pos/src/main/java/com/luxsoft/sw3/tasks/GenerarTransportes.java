package com.luxsoft.sw3.tasks;

import java.util.List;

import com.luxsoft.sw3.embarque.Chofer;
import com.luxsoft.sw3.embarque.Transporte;
import com.luxsoft.sw3.services.Services;

/**
 * Altas y bajas al catalogo de transportres
 * @author Ruben Cancino Ramos
 *
 */
public class GenerarTransportes {
	
	
	public static void altaChoferes(){
		String[] nombres={"CHOFER 1","CHOFER 2","CHOFER 3","CHOFER 4"};
		for(String s:nombres){
			Chofer chofer=new Chofer();		
			chofer.setNombre(s);
			chofer.setRadio("SIN RADIO");
			chofer.setRfc("");
			Services.getInstance().getUniversalDao().save(chofer);
		}
		
	}
	
	
	public static void altaCamionetas(){
		
		List<Chofer> choferes=Services.getInstance().getUniversalDao().getAll(Chofer.class);
		int placa=10;
		for(Chofer c:choferes){
			
			Transporte transporte=new Transporte();		
			transporte.setChofer(c);
			transporte.setDescripcion("CAMIONETA "+c.getNombre().trim());
			transporte.setPlacas("P:"+placa++);
			transporte.setPoliza("");
			transporte=(Transporte)Services.getInstance().getUniversalDao().save(transporte);
		}
	}
	
	
	public static void main(String[] args) {
		altaChoferes();
		altaCamionetas();
	}
	
	


}
