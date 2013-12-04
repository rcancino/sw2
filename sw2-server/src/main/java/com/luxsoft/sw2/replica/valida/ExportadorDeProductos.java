package com.luxsoft.sw2.replica.valida;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;


import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

public class ExportadorDeProductos {
	
	Logger logger=LoggerHelper.getLogger();
	
	
	public ExportadorDeProductos() {
		
	}
	
	public void exportar(){
		exportar(new Date());
	}
	
	public void exportar(String sfecha){
		exportar(DateUtil.toDate(sfecha));
	}
	
	public void exportar(Date fecha){
		System.out.println("Exportando Productos modificados en la fecha" + fecha);
		String sql= "select distinct(clave) from sx_productos where date(modificado)=? ";
		List<String> rows=ServiceLocator2.getJdbcTemplate()
				.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha)
						,String.class);
		for(String  row:rows){
			Producto target=ServiceLocator2.getProductoManager().buscarPorClave(row);
			EntityLog log=new EntityLog(target,target.getId(),"OFICINAS",EntityLog.Tipo.CAMBIO);
			ServiceLocator2.getReplicaMessageCreator().enviar(log);
		}
	}
	
	public static void main(String[] args) {
		ExportadorDeProductos exp=new ExportadorDeProductos();
		exp.exportar("13/08/2012");
	}

}
