package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_CompraMoneda implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		
		String sql="select C.DESCRIPCION AS BANCO,C.NUMERO,B.* from sw_bcargoabono B JOIN SW_CUENTAS C ON(B.CUENTA_ID=C.ID )where fecha=? and concepto_id in (492721,737342)";
		List<Map<String, Object>> rows=ServiceLocator2
			.getJdbcTemplate()
			.queryForList(sql,
					new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())}
					);
		String asiento="Compra Dolares";
			
		for(Map<String, Object> row:rows){			
						
			Long concepto= (Long) row.get("NUMERO");		
			String afavor=(String)row.get("AFAVOR");
			Number importe=(Number)row.get("IMPORTE");
			Long id= (Long)row.get("CARGOABONO_ID");
			String comentario= (String)row.get("COMENTARIO");
			Number tc=(Number)row.get("TC");
			String desc2="Compra de Dolares: "+id+" "+comentario;
			BigDecimal importe1=new BigDecimal(importe.doubleValue()*tc.doubleValue()).abs();
			
			if((Long)row.get("CONCEPTO_ID")==492721L){
				PolizaDetFactory.generarPolizaDet(poliza, "102",""+concepto,false, importe1, desc2, afavor, "OFICINAS", asiento);	
			}
			
			if((Long)row.get("CONCEPTO_ID")==737342L){
				PolizaDetFactory.generarPolizaDet(poliza, "102",""+concepto,true, importe1, desc2, afavor, "OFICINAS", asiento);		
			}			
		}		
	}
	
}
