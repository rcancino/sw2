package com.luxsoft.sw2.replica.valida;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;



import com.luxsoft.siipap.compras.model.ListaDePrecios;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

public class ExportadorDeListaDePreciosCliente {
	
	Logger logger=LoggerHelper.getLogger();
	
	
	public ExportadorDeListaDePreciosCliente() {
		
	}
	
	public void exportar(){
		exportar(new Date());
	}
	
	public void exportar(String sfecha){
		exportar(DateUtil.toDate(sfecha));
	}
	
	public void exportar(Date fecha){
		
		String sql= "select distinct(lISTA_id) from SX_LP_CLIENTE where date(modificado)=? ";
		List<Long> rows=ServiceLocator2.getJdbcTemplate()
				.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha)
						,Long.class);
		for(Long  row:rows){
			ListaDePreciosCliente target=ServiceLocator2.getListaDePreciosClienteManager().get(row);
			EntityLog log=new EntityLog(target,target.getId(),"OFICINAS",EntityLog.Tipo.CAMBIO);
			ServiceLocator2.getReplicaMessageCreator().enviar(log);
		}
	}
	
	public static void main(String[] args) {
		ExportadorDeListaDePreciosCliente exp=new ExportadorDeListaDePreciosCliente();
		exp.exportar("10/08/2012");
	}

}
