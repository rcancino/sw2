package com.luxsoft.sw3.tasks.parches;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.sw3.replica.ReplicadorDeClientes;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Actualiza las direcciones de clientes a partir de DBFs
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class ActualizarTelefonosDeClientesDesdeDBF {
	
	ReplicadorDeClientes replicador;
	
	private DataSource getDataSource(){
		BasicDataSource ds=new BasicDataSource();
		ds.setDriverClassName("sun.jdbc.odbc.JdbcOdbcDriver");
		ds.setUrl("jdbc:odbc:SIIPAP");
		return ds;
	}
	
	public void execute(){
		replicador=new ReplicadorDeClientes();
		JdbcTemplate template=new JdbcTemplate(getDataSource());		
		String sql="select * from CLIENTES   order by CLICLAVE desc";		
		template.query(sql, new RowCallbackHandler(){			
			public void processRow(ResultSet rs) throws SQLException {
				
				String clave=rs.getString("CLICLAVE");
				String tel1=rs.getString("CLITEL1");
				String fax1=rs.getString("CLIFAX");
				Cliente c=Services.getInstance().getClientesManager().buscarPorClave(clave);
				if(c!=null){
					String t1=c.getTelefono1();
					if(StringUtils.isNotBlank(tel1)){
						if(StringUtils.isBlank(t1)){
							c.setTelefono1(tel1);
							c.setFax(fax1);
							Services.getInstance().getUniversalDao().save(c);
							System.out.println("Actualizando telefonos de cliente: "+c.getClave()+ "  "+c.getNombre());
						}
					}
				}
			}		
		});
	}
	
	
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		new ActualizarTelefonosDeClientesDesdeDBF().execute();
		
		
	}

}
