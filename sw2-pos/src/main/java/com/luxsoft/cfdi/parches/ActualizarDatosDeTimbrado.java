package com.luxsoft.cfdi.parches;

import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.cfdi.parches.CorreccionDeTimbradosPruebas;
import com.luxsoft.sw3.services.Services;

/**
 * Quita el timbrado a las facturas que se generaron incorrectamente
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ActualizarDatosDeTimbrado {
	
	
	public static void run() throws Exception{
		List<CFDI> cfdis=Services.getInstance().getHibernateTemplate().find("from CFDI c ");
		for(CFDI cfdi:cfdis){
			cfdi.setUUID(cfdi.getTimbreFiscal().getUUID());
			cfdi.setTimbrado(cfdi.getTimbreFiscal().getFechaTimbrado());
			cfdi=(CFDI)Services.getInstance().getHibernateTemplate().merge(cfdi);
			System.out.println("CFDI actualizado: "+cfdi.getId()+" UUID: "+cfdi.getUUID());
		}
	}
	
	public static void main(String[] args) throws Exception{
		run();
	}

}
