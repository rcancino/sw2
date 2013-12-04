package com.luxsoft.siipap.analisis.service;

import java.math.BigDecimal;
import java.sql.Types;

import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.util.ClassUtils;

import com.luxsoft.siipap.analisis.model.AnalisisFV;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.SQLUtils;

public class AnalisisManagerImpl extends JdbcDaoSupport implements AnalisisManager{

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.analisis.service.AnalisisManager#generarAnalisisGlobalYTD()
	 */
	public AnalisisFV generarAnalisisGlobalYTD() {
		return null;
	}

	public BigDecimal obtnerProvisionHistorica(Periodo mes) {
		String path=ClassUtils.classPackageAsResourcePath(getClass());
		String sql=SQLUtils.loadSQLQueryFromResource(path+"/provision.sql");
		SqlParameterValue p1=new SqlParameterValue(Types.DATE,mes.getFechaInicial());
		SqlParameterValue p2=new SqlParameterValue(Types.DATE,mes.getFechaFinal());
		Object[] params=new Object[]{p1,p2};
		BigDecimal val=(BigDecimal)getJdbcTemplate().queryForObject(sql,params,BigDecimal.class);
		return val;
	}
	

}
