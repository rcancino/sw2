package com.luxsoft.sw2.replica.valida;

import java.sql.Types;
import java.util.Date;

import org.springframework.jdbc.core.SqlParameterValue;

public class ValUtils {
	
	
	public static SqlParameterValue getPamaeter(final Date fecha){
		return new SqlParameterValue(Types.DATE, fecha);
	}
	
	public static Object[] getDateAsSqlParameter(final Date fecha){
		return new Object[]{
				getPamaeter(fecha)
		};
	}

}
