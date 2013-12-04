package com.luxsoft.siipap.dao.legacy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.legacy.Deposito;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;

public class DepositoDaoImpl extends JdbcDaoSupport implements DepositoDao{

	public List<Deposito> buscarDepositos(Date fechaIni,Date fechaFin) {
		final String sql="select * from SW_DEPOSITOS where fechaCobranza between ? and ?";
		Object[] args={new SqlParameterValue(Types.DATE,fechaIni),new SqlParameterValue(Types.DATE,fechaFin)};
		return getJdbcTemplate().query(sql, args, new DepositoMapper());
		
	}
	
	private class DepositoMapper implements RowMapper{

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Deposito d=new Deposito();
			d.setId(rs.getLong("DEOPSITO_ID"));
			d.setBanco(rs.getString("BANCO"));
			d.setCuenta(rs.getString("CUENTA"));
			d.setCuentaDestino(rs.getString("CUENTADESTINO"));
			d.setFecha(rs.getDate("FECHA"));
			d.setFolio(rs.getInt("FOLIO"));
			d.setFormaDePago(rs.getString("FORMADP"));
			d.setImporte(CantidadMonetaria.pesos(rs.getDouble("IMPORTE")));
			d.setOrigen(rs.getString("ORIGEN"));
			d.setSucursal(rs.getInt("SUCURSALID"));
			Number val=(Number)rs.getObject("REVISADA");
			d.setRevisada(rs.getBoolean("REVISADA"));
			/*
			if(val!=null){
				d.setRevisada(val.intValue()==0);
			}
			*/
			d.setFechaCobranza(rs.getDate("FECHA"));
			return d;
		}
		
	}
	
	public Deposito save(Deposito d){
		String sql="update SW_DEPOSITOS set " +
				"REVISADA=?" +
				",FECHACOBRANZA=?" +
				",FORMADP=?" +
				" WHERE DEOPSITO_ID=?";
		Object[] args={new SqlParameterValue(Types.NUMERIC,d.isRevisada())
					,new SqlParameterValue(Types.DATE,d.getFechaCobranza())
					,new SqlParameterValue(Types.VARCHAR,d.getFormaDePago())
					,new SqlParameterValue(Types.NUMERIC,d.getId())
		};
		
		getJdbcTemplate().update(sql, args);
		List l=getJdbcTemplate().query("select * from sw_depositos where deopsito_id=?",new Long[]{d.getId()}, new DepositoMapper());
		return (Deposito)l.get(0);
	}
	
	public static void main(String[] args) {
		DepositoDao dao=(DepositoDao)ServiceLocator2.instance().getContext().getBean("depositoDao");
		List<Deposito> deps=dao.buscarDepositos(DateUtil.toDate("01/07/2008"),DateUtil.toDate("05/07/2008"));
		System.out.println("Deps: "+deps.size());
	}

}
