package com.luxsoft.sw2.replica.valida;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

public class EliminadorDeSolicitudDeTraslado {
	
	protected Logger logger=LoggerHelper.getLogger();
	protected Set<Long> sucursales=new HashSet<Long>();
	
	public void eliminar(){
	
		
		Date fecha=DateUtils.addDays(new Date(),-30);
		
		eliminar(fecha);
	}
	
	public void eliminar(Date fecha){
		
		logger.info("fecha: "+ fecha);
		
		
				
		int res=ServiceLocator2.getJdbcTemplate().update("DELETE  FROM  sx_solicitud_trasladosdet  WHERE SOL_ID IN( SELECT s.SOL_ID FROM  sx_solicitud_traslados S WHERE DATE(S.FECHA)<=? AND S.SOL_ID NOT IN(SELECT T.SOL_ID FROM sx_traslados T WHERE T.SOL_ID=S.SOL_ID))"
				     ,ValUtils.getDateAsSqlParameter(fecha) );
		logger.info("SolesDets  Eliminados En Oficinas "+res);
		
		
		int res1=ServiceLocator2.getJdbcTemplate().update("DELETE  FROM  sx_solicitud_traslados  WHERE FECHA<=? AND SOL_ID NOT IN(SELECT SOL_ID FROM sx_traslados )"
			     ,ValUtils.getDateAsSqlParameter(fecha) );
	      logger.info("Soles Eliminados En Oficinas "+res1);
 
		
		for(Long sucursal:sucursales){
			
			JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursal);
			
			int res3=template.update("DELETE  FROM  sx_solicitud_trasladosdet  WHERE SOL_ID IN( SELECT s.SOL_ID FROM  sx_solicitud_traslados S WHERE DATE(S.FECHA)<=? AND S.SOL_ID NOT IN(SELECT T.SOL_ID FROM sx_traslados T WHERE T.SOL_ID=S.SOL_ID))"
				     ,ValUtils.getDateAsSqlParameter(fecha) );
		logger.info("Sucursal operando :  "+sucursal +"  SolesDets  Eliminados: "+res3);
		
		
		int res4=template.update("DELETE  FROM  sx_solicitud_traslados  WHERE FECHA<=? AND SOL_ID NOT IN(SELECT SOL_ID FROM sx_traslados )"
			     ,ValUtils.getDateAsSqlParameter(fecha) );
	      logger.info("Sucursal operando: " +sucursal +" Soles Eliminados En Oficinas "+res4);
			
			
			 
		}
		
	}
	
	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public EliminadorDeSolicitudDeTraslado addSucursal(Long...ids){
		sucursales.clear();
		for(Long id:ids){
			sucursales.add(id);
		}
		return this;
	}
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
	
		new EliminadorDeSolicitudDeTraslado().addSucursal(2L,3L,5L,6L,9L,11L).eliminar();
	}

}
