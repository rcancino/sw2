package com.luxsoft.sw3.cfdi.parches;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

import com.edicom.ediwinws.cfdi.client.CfdiClient;
import com.edicom.ediwinws.service.cfdi.CancelaResponse;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.sw3.cfdi.model.CFDI;

/**
 * Parche para generar las cancelaciones pendientes de CFDIs en el SAT
 * 
 * @author Ruben Cancino
 *
 */
public class ValidarUUID {
	
	CfdiClient client;
	Empresa empresa;
	
	
	
	
	public void validacion (Periodo periodo){
		
		DBUtils.whereWeAre();
		empresa=ServiceLocator2.getConfiguracion().getSucursal().getEmpresa();
		
		
		String sql="SELECT x.CFD_ID FROM SX_CFDI X WHERE UUID IS null AND date(x.creado) between ? and ?  ";
			
		
		Object[] args=new Object[]{
				new SqlParameterValue(Types.DATE, periodo.getFechaInicial()),
				new SqlParameterValue(Types.DATE, periodo.getFechaFinal())
		};
		List<String> rows=ServiceLocator2.getJdbcTemplate().queryForList(
				sql
				, args
				, String.class);
		List<String> porArreglar=new ArrayList<String>();
		for(String id:rows){
			CFDI cfdi=ServiceLocator2.getCFDIManager().getCFDI(id);
			if(cfdi.getTimbreFiscal().getUUID()!=null){
				porArreglar.add(cfdi.getTimbreFiscal().getUUID());
			}
			
		}
		for(String uuid:porArreglar){
			System.out.println("Correccion para cfdi: "+uuid);
		}
		
		String[] array=porArreglar.toArray(new String[0]);
		System.out.println("Cargos por Arreglar: : "+array.length);
		
		try {
			
			for(String id:rows){
				CFDI cfdi=ServiceLocator2.getCFDIManager().getCFDI(id);
				String uUID=cfdi.getTimbreFiscal().getUUID();
				cfdi.setUUID(uUID);
				cfdi=(CFDI)ServiceLocator2.getHibernateTemplate().merge(cfdi);
				System.out.println("Corrigiendo: "+cfdi +"-----------------------------"+uUID);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	

	
	public static void main(String[] args) {
		ValidarUUID task=new ValidarUUID();
		task.validacion(new Periodo("21/10/2014","23/10/2014"));
	}

}
