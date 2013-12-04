package com.luxsoft.siipap.cxc.parches;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.SQLUtils;

/**
 * Suspende clientes segun las reglas de negocios
 * Manda archivos de actualizacion .DOR a C:/PRUEBAS/REPLICA
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ActualizarSaldoGeneral {
	
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
		List<Cliente> clientes=ServiceLocator2.getClienteManager().buscarClientesCredito();
		for(Cliente c:clientes){
			ServiceLocator2.getClienteServices().exportarSaldo(c);
		}
	}
	
	public static void main(String[] args) {
		new ActualizarSaldoGeneral().execute();
	}

}
