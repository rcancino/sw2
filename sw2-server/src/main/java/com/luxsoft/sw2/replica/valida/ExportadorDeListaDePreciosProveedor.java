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

public class ExportadorDeListaDePreciosProveedor {
	
	Logger logger=LoggerHelper.getLogger();
	
	
	public ExportadorDeListaDePreciosProveedor() {
		
	}
	
	public void exportar(){
		exportar(new Date());
	}
	
	public void exportar(String sfecha){
		exportar(DateUtil.toDate(sfecha));
	}
	
	public void exportar(Date fecha){
		
		String sql= "select distinct(ID) from SX_LP_PROVS where date(modificado)=? ";
		List<Long> rows=ServiceLocator2.getJdbcTemplate()
				.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha)
						,Long.class);
		for(Long  row:rows){
			ListaDePrecios target=ServiceLocator2.getListaDePreciosDao().get(row);
			EntityLog log=new EntityLog(target,target.getId(),"OFICINAS",EntityLog.Tipo.CAMBIO);
			ServiceLocator2.getReplicaMessageCreator().enviar(log);
		}
	}
	
	public static void main(String[] args) {
		ExportadorDeListaDePreciosProveedor exp=new ExportadorDeListaDePreciosProveedor();
		exp.exportar("09/08/2012");
	}

}
