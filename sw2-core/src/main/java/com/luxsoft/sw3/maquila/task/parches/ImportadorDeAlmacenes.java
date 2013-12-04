package com.luxsoft.sw3.maquila.task.parches;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.maquila.model.Almacen;
import com.luxsoft.sw3.maquila.model.Maquilador;

/**
 * Importa de Oracle los almacenes
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ImportadorDeAlmacenes implements RowMapper{
	
	public void execute(){
		String sql="select * from sw_almacenes";
		List<Almacen> rows=ServiceLocator2.getAnalisisJdbcTemplate().query(sql, this);
		for(Almacen m:rows){
			ServiceLocator2.getHibernateTemplate().save(m);
			System.out.println("Almacen: "+m);
		}
	}

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		Almacen a=new Almacen();
		a.setId(rs.getLong("ALMACEN_ID"));
		Long maquiladorId=rs.getLong("MAQUILADOR_ID");
		//Maquilador m=(Maquilador)ServiceLocator2.getUniversalDao().get(Maquilador.class, maquiladorId);
		//a.setMaquilador(m);
		a.setNombre(rs.getString("NOMBRE"));
		return a;
	}
	
	public static void main(String[] args) {
		new ImportadorDeAlmacenes().execute();
	}

}
