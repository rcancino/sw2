package com.luxsoft.cfdi.parches;

import com.luxsoft.sw3.cfdi.parches.CorreccionDeTimbradosPruebas;
import com.luxsoft.sw3.services.Services;

/**
 * Quita el timbrado a las facturas que se generaron incorrectamente
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class Parche1 {
	
	
	public static void run() throws Exception{
		CorreccionDeTimbradosPruebas task=new CorreccionDeTimbradosPruebas(Services.getInstance().getHibernateTemplate());
		task.guardarErroneos("tacuba");
		
		
	}
	
	public static void main(String[] args) throws Exception{
		run();
	}

}
