package com.luxsoft.sw2.replica.valida;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

public class ActualizadorDeDocumentoYSAF {
	
	public void actualizar(){
		Date fecha= new Date();
	    actualizarDocumento(fecha);	
	
	}
	
	Logger logger=LoggerHelper.getLogger();
	
	
	//Metodo que actualiza el Folio de Ventas tomandolo del CFD
	public void actualizarDocumento(Date fecha){
		logger.info("fecha: "+fecha);
		Object[] args=new Object[]{fecha};
				
		int res=ServiceLocator2.getJdbcTemplate().update("UPDATE sx_ventas V set v.docto=(SELECT t.folio FROM sx_cfd t where t.origen_id=v.cargo_id) " 
		+ " WHERE v.fecha=? and v.docto=0 ", args);
		logger.info("Ventas Actualizadas "+res);
		
		int res2=ServiceLocator2.getJdbcTemplate().update("UPDATE sx_ventasdet V set v.documento=(SELECT t.folio FROM sx_cfd t where t.origen_id=v.venta_id) " 
				+ " WHERE date(v.fecha)=? and v.documento=0", args);
				logger.info("Detalles de Ventas Actualizadas "+res2);
				
		int res3=ServiceLocator2.getJdbcTemplate().update("UPDATE sx_cxc_aplicaciones V set v.car_docto=(SELECT t.folio FROM sx_cfd t where t.origen_id=v.cargo_id) " 
         +" WHERE v.fecha=? and v.car_docto=0 ", args);
				logger.info("Aplicaciones Actualizadas "+res3);
		
		int res4=ServiceLocator2.getJdbcTemplate().update("UPDATE sx_cxc_aplicaciones A SET A.ABN_DESCRIPCION=REPLACE(ABN_DESCRIPCION,'SAF ','') "
                 + " where ABN_DESCRIPCION like 'SAF %' AND A.FECHA=(SELECT X.SAF FROM sx_cxc_abonos X WHERE X.ABONO_ID=A.ABONO_ID)");
		   		logger.info("SAF actulizados "+res4);
				
			
	}
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
	//	 new ActualizadorDeDocumentoYSAF().actualizarDocumento(DateUtil.toDate("27/08/2012"));
		new ActualizadorDeDocumentoYSAF().actualizar();
	}

}
