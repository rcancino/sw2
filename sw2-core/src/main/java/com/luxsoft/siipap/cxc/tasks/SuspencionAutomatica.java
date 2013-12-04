package com.luxsoft.siipap.cxc.tasks;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;

/**
 * Suspende clientes segun las reglas de negocios
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SuspencionAutomatica {
	
	public void execute(){
		/*
		DataSource ds=new  DriverManagerDataSource(
				"com.mysql.jdbc.Driver"
				,"jdbc:mysql://ser-ofi-d/produccion"
				,"root"
				,"sys"				
				);
		JdbcTemplate template=new JdbcTemplate(ds);
		*/
		DBUtils.whereWeAre();
		String sql=SQLUtils.loadSQLQueryFromResource("sql/suspencion_automatica_clientes.sql");
		//System.out.println(sql);
		List<Map<String, Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql);
		System.out.println("Registros a procesar a procesar: "+rows.size());
		int count=1;
		for(Map<String,Object> row :rows){
			String clave=(String)row.get("CLAVE");
			Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(clave);
			System.out.println("Cliente: "+c);
			if(!c.getCredito().isSuspendido()){
				System.out.println("Actualizando suspendido cliente: "+c);
				c.getCredito().setSuspendido(true);
				c.agregarComentario("SUSP_AUT", DateUtil.getDate(new Date()));
				ServiceLocator2.getClienteManager().save(c);
				
			}
			ServiceLocator2.getClienteServices().exportarCliente(c);
			System.out.println("Row: "+(count++));
		}

		
	}
	
	public static void main(String[] args) {
		new SuspencionAutomatica().execute();
	}

}
