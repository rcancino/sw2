package com.luxsoft.cfdi.parches;

import java.util.Date;

import com.luxsoft.siipap.util.DateUtil;
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
	
	
	public static void run(String sdate) throws Exception{
		Date fecha=DateUtil.toDate(sdate);
		CorreccionDeTimbradosPruebas task=new CorreccionDeTimbradosPruebas(Services.getInstance().getHibernateTemplate());
		//task.corregir(fecha);
		//task.guardarErroneos();
		//task.corregirErrores("C:\\basura\\cfdiErrors.csv");
		task.reTimbrar();
	}
	
	public static void main(String[] args) throws Exception{
		run("17/12/2013");
	}

}
