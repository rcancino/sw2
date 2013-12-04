package com.luxsoft.sw2.replica.task;

import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw2.replica.valida.ValUtils;

public class ActualizadorDeSaldosDeCliente {
	
	
	public void execute(Date fecha){
		//Buscar todos los clientes con aplicaciones en el dia
		String sql="select CLAVE from sx_cxc_aplicacion where date(creado)=? ";
		List<String> claves=ServiceLocator2.getJdbcTemplate().queryForList(sql,ValUtils.getDateAsSqlParameter(fecha));
		claves.addAll(ServiceLocator2.getJdbcTemplate()
				.queryForList("select distinct clave from sx_cxc_abonos where TIPO_ID like \'NOTA%\' and date(creado)=?",ValUtils.getDateAsSqlParameter(fecha)));
		
	}

}
