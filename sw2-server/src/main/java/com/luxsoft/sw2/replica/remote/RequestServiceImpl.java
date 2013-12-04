package com.luxsoft.sw2.replica.remote;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

import com.luxsoft.utils.LoggerHelper;

public class RequestServiceImpl  implements RequestService ,InitializingBean{
	
	Logger logger=LoggerHelper.getLogger();
	
	private DataSource dataSource;
	
	private JdbcTemplate jdbcTemplate;
	
	
	
	public List<Map<String, Object>> requestSqlData(String sql,Object... params) {
		return jdbcTemplate.queryForList(sql, params);
	}
	
	public void afterPropertiesSet() throws Exception {
		jdbcTemplate=new JdbcTemplate(dataSource);
		jdbcTemplate.setMaxRows(1000);
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
