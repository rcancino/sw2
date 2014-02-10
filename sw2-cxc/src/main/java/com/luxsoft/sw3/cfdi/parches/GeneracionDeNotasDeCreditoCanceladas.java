package com.luxsoft.sw3.cfdi.parches;

import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.cfdi.model.CFDI;
import java.util.*;

import org.apache.commons.lang.exception.ExceptionUtils;

public class GeneracionDeNotasDeCreditoCanceladas {
	
	public static void run(String... notas){
		System.out.println("Notas a timbrar: "+notas.length);
		
		for(String id:notas){
			try {
				NotaDeCredito nota=CXCUIServiceFacade.buscarNotaDeCreditoInicializada(id);
				CFDI cfdi=ServiceLocator2.getCFDIManager().buscarPorOrigen(nota.getId());
				if(cfdi==null){
					cfdi=ServiceLocator2.getCFDINotaDeCredito().generar(nota);
				}
				if(cfdi.getTimbreFiscal().getUUID()==null){
					CXCUIServiceFacade.timbrar(nota);
				}else{
					System.out.println("CFDI de nota ya timbrado: "+cfdi);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error generando nota: "+id+ "\n"+ExceptionUtils.getRootCauseMessage(e));
			}
			
		}
	}
	
	public static void generarPendientes(){
		String sql="SELECT ORIGEN_ID FROM SX_CFDI_NOTAS_PENDIENTES ";
		//String sql="SELECT ORIGEN_ID FROM sx_cfdi_notas_pendientes where ORIGEN_ID not in (SELECT ORIGEN_ID FROM sx_cfdi where date(creado)=\'2014/02/08\' and tipo= \'NOTA_CREDITO\')";
		List<String> ids=ServiceLocator2.getJdbcTemplate().queryForList(sql,  String.class);
		
		run(ids.toArray(new String[0]));
	}
	
	
	public static void main(String[] args) {
		//run("8a8a819d-440d2432-0144-0d249758-0002","8a8a8197-44082cc2-0144-096ee28c-002b","8a8a8197-44082cc2-0144-0966fc06-0027","8a8a81a8-440827ac-0144-095cdafe-0016");
		generarPendientes();
		//run("");
	}

}
