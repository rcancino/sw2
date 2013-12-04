/**
 * 
 */
package com.luxsoft.siipap.cxc.ui.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.cxc.model.AntiguedadDeSaldo;
import com.luxsoft.siipap.cxc.model.CargoRow;
import com.luxsoft.siipap.cxc.util.CargoRowUtils;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;

public class AntiguedadDeSaldosModel{
	
	private EventList<CargoRow> cuentasPorCobrar;
	private GroupingList<CargoRow> porCliente;
	private FunctionList<List<CargoRow>,AntiguedadDeSaldo> reportList;
	
	public AntiguedadDeSaldosModel(){
		init();
	}
	
	public void init(){
		cuentasPorCobrar=GlazedLists.eventList(new ArrayList<CargoRow>());		
		Comparator<CargoRow> clienteComparator=GlazedLists.beanPropertyComparator(CargoRow.class, "clave");
		porCliente=new GroupingList<CargoRow>(cuentasPorCobrar,clienteComparator);
		FunctionList.Function<List<CargoRow>, AntiguedadDeSaldo> function=new FunctionList.Function<List<CargoRow>, AntiguedadDeSaldo>(){

			public AntiguedadDeSaldo evaluate(List<CargoRow> sourceValue) {
				return new AntiguedadDeSaldo(sourceValue);
			}
			
		};
		reportList = new FunctionList<List<CargoRow>,AntiguedadDeSaldo>(porCliente,function);
		
	}

	public FunctionList<List<CargoRow>, AntiguedadDeSaldo> getReportList() {
		return reportList;
	}
	
	public void loadData(final List<CargoRow> cargos){
		cuentasPorCobrar.clear();
		cuentasPorCobrar.addAll(cargos);
		CantidadMonetaria tot=CargoRowUtils.calcularSaldo(cargos);
		for(AntiguedadDeSaldo a:reportList){
			a.setTotalGlobal(tot);
		}
	}
	
	public  List<CargoRow> findData(){
		
		//String sql="select c.NOMBRE as SUCURSAL_NAME,b.TIPO_VENCIMIENTO,b.LIMITE,b.PLAZO, a.* from v_ventas a left join  SW_CLIENTES_CREDITO b on (a.clave=b.clave) left  join sw_sucursales c on(a.sucursal=c.sucursal_id) where a.origen=\'CRE\' and a.year>2007 and a.saldo>0";
		String sql="select S.NOMBRE AS SUCURSAL_NAME,V.TIPO,V.REVISION AS TIPO_VENCIMIENTO,C.LINEA AS LIMITE" +
				",C.PLAZO,V.CLAVE,C.NOMBRE,V.DOCTO AS NUMERO,V.FECHA,V.VTO,V.PRECIO_BRUTO AS FACTURA,V.TOTAL" +
				",V.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM sx_cxc_aplicaciones B WHERE B.CARGO_ID=V.CARGO_ID),0) AS SALDO " +
				"  FROM sx_ventas v JOIN sx_clientes_credito C ON(V.CLAVE=C.CLAVE) " +
				"  JOIN sw_sucursales S ON(S.SUCURSAL_ID=V.SUCURSAL_ID) " +
				" where v.fecha>? and v.origen=\'CRE\' AND v.cargo_id not in (select CARGO_ID from SX_JURIDICO) AND v.total-IFNULL((SELECT SUM(B.IMPORTE) FROM sx_cxc_aplicaciones B WHERE B.CARGO_ID=V.CARGO_ID),0)<>0 ";
		Object[] params =new Object[]{new SqlParameterValue(Types.DATE,DateUtil.toDate("31/12/2008"))};
		List<CargoRow> cargos=ServiceLocator2.getJdbcTemplate().query(sql,params, new CargoRowMapper());
		return cargos;
	}
	
	private class CargoRowMapper implements RowMapper{
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			CargoRow row=new CargoRow();
			row.setClave(rs.getString("CLAVE"));
			row.setNombreRazon(MessageFormat.format("{0} ({1})", rs.getString("NOMBRE"),rs.getString("CLAVE")));
			row.setDocumento(rs.getLong("NUMERO"));
			row.setFecha(rs.getDate("FECHA"));
			row.setVencimiento(rs.getDate("VTO"));
			row.setSaldo(CantidadMonetaria.pesos(rs.getBigDecimal("SALDO").doubleValue()));
			row.setTotal(CantidadMonetaria.pesos(rs.getBigDecimal("TOTAL").doubleValue()));
			row.setLimite(CantidadMonetaria.pesos(rs.getBigDecimal("LIMITE").doubleValue()));
			row.setPlazoCliente(rs.getInt("PLAZO"));
			row.setSucursalName(rs.getString("SUCURSAL_NAME"));
			row.setTipo(rs.getString("TIPO"));
			boolean tv=rs.getBoolean("TIPO_VENCIMIENTO");
			row.setTipoVencimiento(tv?"REV":"FAC");
			row.setPlazo(rs.getInt("PLAZO"));
			return row;
		}
	}
	
	public static void main(String[] args) {
		AntiguedadDeSaldosModel model=new AntiguedadDeSaldosModel();
		List<CargoRow> cargos=model.findData();
		System.out.println("Data: "+cargos.size());
	}
	
	
}