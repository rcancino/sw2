package com.luxsoft.siipap.inventarios.parches;

import java.sql.Types;
import java.util.Iterator;
import java.util.List;

import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;


/**
 * Llena la tabla de SX_SALDO_MENSUAL para 
 * la implementacion de oracle
 * 
 * @author Ruben Cancino
 *
 */
public class CalculoDeInventarioMensual {
	
	public static void execute(final Periodo p){
		List<Periodo> meses=Periodo.periodosMensuales(p);
		for (Iterator<Periodo> iterator = meses.iterator(); iterator.hasNext();) {
			Periodo periodo = (Periodo) iterator.next();
			actualizar(periodo);
		}
	}
	
	private static void actualizar(Periodo mes){
		System.out.println("Actualizando inventario para: "+mes.getFechaFinal());
		int year=Periodo.obtenerYear(mes.getFechaFinal());
		int me=Periodo.obtenerMes(mes.getFechaFinal())+1;
		String delete="DELETE SX_SALDO_MENSUAL WHERE YEAR=@YEAR AND MES=@MES";
		delete=delete.replaceAll("@YEAR", String.valueOf(year));
		delete=delete.replaceAll("@MES", String.valueOf(me));
		
		int res=ServiceLocator2.getAnalisisJdbcTemplate().update(delete);
		System.out.println("Registros eliminados: "+res);
		
		String sql="INSERT INTO   SX_SALDO_MENSUAL " +
				"SELECT @YEAR, @MES,ALMARTIC,ALMSUCUR,sum(ALMCANTI/ALMUNIXUNI) " +
				" FROM SW_ALMACEN2 " +
				" WHERE ALMFECHA<=? " +
				"GROUP BY ALMARTIC,ALMSUCUR";
		
		sql=sql.replaceAll("@YEAR", String.valueOf(year));
		sql=sql.replaceAll("@MES", String.valueOf(me));
		SqlParameterValue p1=new SqlParameterValue(Types.DATE,mes.getFechaFinal());
		res=ServiceLocator2.getAnalisisJdbcTemplate().update(sql,new Object[]{p1});
		System.out.println("Registros insertados: "+res);
	}
	
	public static void crearTabla(){
		String sql="CREATE TABLE SX_SALDO_MENSUAL(   YEAR NUMBER(10),   MES NUMBER(10),   CLAVE varchar2(10) NOT NULL,   SUCURSAL decimal(10) NOT NULL,   SALDO NUMBER(19,3))";
		ServiceLocator2.getAnalisisJdbcTemplate().execute(sql);
	}
	
	public static void main(String[] args) {
		//crearTabla();
		execute(Periodo.getPeriodoEnUnMes(11, 2007));		
		//Periodo p=;
		//new CalculoDeInventarioMensual().execute(p);
	}

}
