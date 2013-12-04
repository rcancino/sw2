package com.luxsoft.sw2.replica.valida;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

public class ExportadorDeClientes {
	
	Logger logger=LoggerHelper.getLogger();
	
	
	public ExportadorDeClientes() {
		
	}
	
	public void exportar(){
		exportar(new Date());
	}
	
	public void exportar(String sfecha){
		exportar(DateUtil.toDate(sfecha));
	}
	
	public void exportar(Date fecha){
		
		String sql= "select distinct(clave) from sx_clientes where date(modificado)=? ";
		List<String> rows=ServiceLocator2.getJdbcTemplate()
				.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha)
						,String.class);
		for(String  row:rows){
			Cliente target=ServiceLocator2.getClienteManager().buscarPorClave(row);
			EntityLog log=new EntityLog(target,target.getId(),"OFICINAS",EntityLog.Tipo.CAMBIO);
			ServiceLocator2.getReplicaMessageCreator().enviar(log);
		}
	}
	
	public static void main(String[] args) {
		ExportadorDeClientes exp=new ExportadorDeClientes();
		exp.exportar();
	}

}
