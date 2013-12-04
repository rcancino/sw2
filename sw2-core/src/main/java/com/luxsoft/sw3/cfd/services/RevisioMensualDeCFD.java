package com.luxsoft.sw3.cfd.services;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

public class RevisioMensualDeCFD {
	
	public void sumarImportes(){
		List<String> pendientes=ServiceLocator2.getJdbcTemplate().queryForList("select CFD_ID from SX_CFD where CREADO is null", String.class);
		System.out.println("CFD a corregir: "+pendientes.size());
		int total=pendientes.size();
		int count=1;
		for(String cfdId:pendientes){			
			ComprobanteFiscal cf=(ComprobanteFiscal)ServiceLocator2.getHibernateTemplate().get(ComprobanteFiscal.class, cfdId);
			cf.loadComprobante();
			cf.setTotal(cf.getComprobante().getTotal());
			cf.setImpuesto(cf.getComprobante().getImpuestos().getTotalImpuestosTrasladados());
			String rfc=cf.getComprobante().getReceptor().getRfc();
			
			cf.setRfc(cf.getComprobante().getReceptor().getRfc());
			cf.setTipoCfd(StringUtils.substring(
					cf.getComprobante().getTipoDeComprobante().toString(),0,1).toUpperCase());
			System.out.println("Actualizado: "+cfdId+ count +" de "+total);
			count++;
		}
		
		
		
	}

}
