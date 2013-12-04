package com.luxsoft.siipap.inventarios.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.luxsoft.siipap.inventarios.model.CostoPromedioItem;
import com.luxsoft.siipap.model.Periodo;

/**
 * Permite mapear un registro de la sentencia de SQL a un 
 * bean {@link CostoPromedioItem}
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CostoPromedioItemMapper implements RowMapper{

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		CostoPromedioItem item=new CostoPromedioItem();
		Number cantidad=(Number)rs.getObject("CANTIDAD");
		Number costo=(Number)rs.getObject("COSTO");
		item.setCantidad(cantidad.doubleValue());
		item.setClave(rs.getString("CLAVE"));
		item.setCosto(BigDecimal.valueOf(costo.doubleValue()));
		item.setTipo(rs.getString("ORIGEN"));
		if(item.getTipo().equals("INI")){
			int year=rs.getInt("YEAR");
			int mes=rs.getInt("MES");
			Periodo p=Periodo.getPeriodoEnUnMes(mes, year);
			item.setFecha(p.getFechaFinal());
		}else{
			item.setFecha(rs.getDate("FECHA"));
		}
		item.setSucursal(rs.getInt("SUCURSAL_ID"));
		item.setDocumento(rs.getLong("DOCUMENTO"));
		
		return item;
	}

}
