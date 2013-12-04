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
public class ActualizarDireccionesDeClientesDesdeDBF {
	
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
		
		String sql="select * from CLIENTES";
		
		template.query(sql, new RowCallbackHandler(){
			int row=1;
			public void processRow(ResultSet rs) throws SQLException {
				
				String clave=rs.getString("CLICLAVE");
				String calle=rs.getString("CLICALLE");
				String colonia=rs.getString("CLICOLON");
				String municipio=rs.getString("CLIDELEG");
				String cp=rs.getString("CLIPOSTAL");
				if(StringUtils.isBlank(cp))
					cp=" ";
				Cliente c=Services.getInstance().getClientesManager().buscarPorClave(clave);
				if(c!=null){
					if(c.getDireccionFiscal()!=null){						
						boolean actualizar=false;
						if(StringUtils.isBlank(c.getDireccionFiscal().getCp()) 
								){
							c.getDireccionFiscal().setCp(cp);
							actualizar=true;
						}
						if(StringUtils.isBlank(c.getDireccionFiscal().getCalle())&& 
								StringUtils.isNotBlank(calle)){
							c.getDireccionFiscal().setCalle(calle);
							actualizar=true;
						}
						if(StringUtils.isBlank(c.getDireccionFiscal().getColonia()) 
								&& StringUtils.isNotBlank(colonia)){
							c.getDireccionFiscal().setColonia(colonia);
							actualizar=true;
						}
						if(StringUtils.isBlank(c.getDireccionFiscal().getMunicipio())
								&& StringUtils.isNotBlank(municipio)){
							c.getDireccionFiscal().setMunicipio(municipio);
							actualizar=true;
						}
						if(actualizar){
							System.out.println("Actualizar: "+c+ "  "+c.getClave()+ "row: "+row++);
							Services.getInstance().getClientesManager().save(c);
							replicador.replicarDeProduccion(c.getClave());
						}
					}	
				}
			}
		
		});
		
		
	}
	
	
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		new ActualizarDireccionesDeClientesDesdeDBF().execute();
		
		
	}

}
