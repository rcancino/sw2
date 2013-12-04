package com.luxsoft.sw3.contabilidad.db;

import java.sql.Types;
import java.text.MessageFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.core.Conventions;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.ClassUtils;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.utils.LoggerHelper;

public class VentasLoader {
	
	Logger logger=LoggerHelper.getLogger();
	
	public void cargar(final Date fecha){
		logger.info("Procesando..");
		elimiar(fecha);
		String path=ClassUtils.addResourcePathToPackagePath(getClass(), Conventions.getVariableName(this)+".sql");
		String sql=SQLUtils.loadSQLQueryFromResource(path);
		Object[] args=new Object[]{
				new SqlParameterValue(Types.DATE, fecha)
		};
		int res=ServiceLocator2.getJdbcTemplate().update(sql, args);
		String msg=MessageFormat
			.format("Fecha: {0,date,short}, Ventas importadas: {1}",fecha,res);
		logger.info(msg);
	}
	
	public void elimiar(Date fecha){
		String sql="DELETE FROM C_VENTAS WHERE FECHA=?";
		Object[] args=new Object[]{
				new SqlParameterValue(Types.DATE, fecha)
		};
		int res=ServiceLocator2.getJdbcTemplate().update(sql, args);
		String msg=MessageFormat
		.format("Fecha: {0,date,short}, Ventas eliminadas: {1}",fecha,res);
		logger.info(msg);
	}
	
	public static void main(String[] args) {
		//DBUtils.whereWeAre();
		VentasLoader loader=new VentasLoader();
		loader.cargar(DateUtil.toDate("31/10/2011"));
	}

}
