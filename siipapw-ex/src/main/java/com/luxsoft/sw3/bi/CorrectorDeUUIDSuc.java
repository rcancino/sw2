package com.luxsoft.sw3.bi;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.utils.LoggerHelper;

public class CorrectorDeUUIDSuc {

	
	protected Logger logger=LoggerHelper.getLogger();
    protected Set<Long> sucursales=new HashSet<Long>();
	
	
	public void corregirUUID(Periodo periodo){
		for(Long sucursalId:sucursales){
			
			 System.out.println("Validando UUID's para Sucursal: "+sucursalId +" Del Dia :" +periodo.getFechaInicialAsString()+"-"+periodo.getFechaFinal());
					corregirUUIDSuc(periodo, sucursalId);
					
				}
	}
	
	public void corregirUUIDSuc(Periodo periodo,Long sucursalId){
		
		JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
		
		
		System.out.println("Conectando a la Sucursal:"+sucursalId);
		
    String sql="SELECT x.CFD_ID FROM SX_CFDI X WHERE UUID IS null AND date(x.creado) between ? and ?  ";
			
		
		Object[] args=new Object[]{
				new SqlParameterValue(Types.DATE, periodo.getFechaInicial()),
				new SqlParameterValue(Types.DATE, periodo.getFechaFinal())
		};
		
		//List<Map<String,Object>> rows=template.queryForList(sql,args);
		List<String> rows=template.queryForList(
				sql
				, args
				, String.class);
		List<String> porArreglar=new ArrayList<String>();
		
		for(String id:rows){
		CFDI cfdi=ServiceLocator2.getCFDIManager().getCFDI(id);
			//System.out.println("Arreglado CFDI"+id + "--" );
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
				if (uUID!=null){
					Object[] arguments=new Object[]{uUID,id};
					int res=template.update("UPDATE SX_CFDI SET UUID=? WHERE CFD_ID=?", arguments);
					
					System.out.println("Corrigiendo: ,"+id +","+uUID);
				}
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public CorrectorDeUUIDSuc addSucursal(Long...ids){
		sucursales.clear();
		for(Long id:ids){
			sucursales.add(id);
		}
		return this;
	}

	
	public static void main(String[] args) {
		new CorrectorDeUUIDSuc()
		//.addSucursal(2L,3L,5L,6L,9L,11L)
		.addSucursal(3L)
		//.actualizarExistenciasOficinas(DateUtil.toDate("14/02/2014"));
		.corregirUUID(new Periodo("01/01/2015","20/02/2015"));
		
	}
	

}
