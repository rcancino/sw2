package com.luxsoft.sw3.tasks.parches;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.sw3.embarque.Chofer;
import com.luxsoft.sw3.embarque.ChoferFacturista;
import com.luxsoft.sw3.embarque.Transporte;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Importa los choferes y facturistas desde SIIPAP
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class ImportadorDeChoferes {
	
	private DataSource getDataSource(){
		BasicDataSource ds=new BasicDataSource();
		ds.setDriverClassName("sun.jdbc.odbc.JdbcOdbcDriver");
		ds.setUrl("jdbc:odbc:SIIPAP");
		return ds;
	}
	
	public void importarFacturistas(){
		JdbcTemplate template=new JdbcTemplate(getDataSource());
		
		String sql="select * from FACTCHOF";
		
		template.query(sql, new RowCallbackHandler(){

			public void processRow(ResultSet rs) throws SQLException {
				ChoferFacturista fac=new ChoferFacturista();
				String fax=rs.getString("FCHFAX");
				Long id=rs.getLong("FCHCLAVE");
				String nombre=rs.getString("FCHNOMBR");
				String telefono1=rs.getString("FCHTEL1");
				String telefono2=rs.getString("FCHTEL2");
				String rfc=rs.getString("FCHRFC");
				fac.setFax(fax);
				fac.setId(id);
				fac.setNombre(nombre);
				fac.setTelefono1(telefono1);
				fac.setTelefono2(telefono2);
				fac.setRfc(rfc);
				
				String calle=rs.getString("FCHCALLE");
				String colonia=rs.getString("FCHCOLON");
				String municipio=rs.getString("FCHDELEG");
				String cp=rs.getString("FCHPOSTAL");
				Direccion direccion=new Direccion();
				direccion.setCalle(calle);
				direccion.setCiudad(municipio);
				direccion.setColonia(colonia);
				direccion.setCp(cp);
				direccion.setEstado("Distrito Federal");
				direccion.setMunicipio(municipio);
				direccion.setNumero("");
				
				fac.setDireccion(direccion);
				try {
					Services.getInstance().getUniversalDao().save(fac);
				} catch (Exception e) {
					System.out.println(ExceptionUtils.getRootCauseMessage(e));
				}
			}
		
		});
		
		
	}
	
	public void importarChoferes(){
		JdbcTemplate template=new JdbcTemplate(getDataSource());
		
		String sql="select * from CHOFERES";
		template.query(sql, new RowCallbackHandler(){
			public void processRow(ResultSet rs) throws SQLException {
				String nombre=rs.getString("CHONOMBR");
				Integer clave=rs.getInt("CHOCLAVE");
				String rfc=rs.getString("CHORFC");
				Long facturista=rs.getLong("CHOFACTURI");
				if(StringUtils.isNotBlank(nombre)){
					
					
					try {
						ChoferFacturista f=(ChoferFacturista)Services
						.getInstance().getHibernateTemplate().get(ChoferFacturista.class, facturista);
						Chofer c=new Chofer();
						c.setNombre(nombre);
						c.setRadio(rfc);
						c.setFacturista(f);
						c=(Chofer)Services.getInstance().getUniversalDao().save(c);
						
						System.out.println("Chofer: "+c);
						
					} catch (Exception e) {
						System.out.println("Imposible importar: "+nombre+ "Clave: "+clave);
						System.out.println(ExceptionUtils.getRootCauseMessage(e));
					}
					
					
					
					
				}
			}
		});
		
	}
	
	public void importarTransportes(){
		List<Chofer> choferes=Services.getInstance().getHibernateTemplate().find("from Chofer");
		int placa=1;
		for(Chofer c:choferes){
			Transporte t=new Transporte();
			t.setChofer(c);
			t.setPlacas(""+placa++);
			t.setDescripcion("Camioneta: "+c.getNombre());
			try {
				Services.getInstance().getUniversalDao().save(t);
			} catch (Exception e) {
				System.out.println(ExceptionUtils.getRootCauseMessage(e));
			}
			
		}
	}
	
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		new ImportadorDeChoferes().importarFacturistas();
		new ImportadorDeChoferes().importarChoferes();
		new ImportadorDeChoferes().importarTransportes();
		
	}

}
