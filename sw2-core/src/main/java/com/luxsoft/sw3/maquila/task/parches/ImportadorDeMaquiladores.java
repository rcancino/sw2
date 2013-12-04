package com.luxsoft.sw3.maquila.task.parches;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.maquila.model.Maquilador;

/**
 * Importa de ORacle los maquiladores
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ImportadorDeMaquiladores implements RowMapper{
	
	public void execute(){
		String sql="select * from sw_maquiladores";
		List<Maquilador> rows=ServiceLocator2.getAnalisisJdbcTemplate().query(sql, this);
		for(Maquilador m:rows){
			ServiceLocator2.getHibernateTemplate().save(m);
			System.out.println("Maquilador: "+m);
		}
	}

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		Maquilador m=new Maquilador();
		m.setId(rs.getLong("MAQUILADOR_ID"));
		m.setClave(rs.getString("CLAVE"));
		m.setNombre(rs.getString("NOMBRE"));
		return m;
	}
	
	public static void main(String[] args) {
		new ImportadorDeMaquiladores().execute();
	}

}
