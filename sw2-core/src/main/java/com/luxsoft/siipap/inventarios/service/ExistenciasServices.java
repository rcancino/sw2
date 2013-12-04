package com.luxsoft.siipap.inventarios.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.util.Assert;

import com.luxsoft.siipap.inventarios.dao.ExistenciaDao;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Tareas relacionadas con existencias
 * 
 * NOTA: Prototipo 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ExistenciasServices extends HibernateDaoSupport{
	
	private ExistenciaDao existenciaDao;
	
	private JdbcTemplate jdbcTemplate;
	
	/**
	 * Cierra un periodo anual de existencias generando 
	 * 
	 * @param year
	 * @param mes
	 */
	public void cerrar(int year){
		int count=getJdbcTemplate().queryForInt("SELECT COUNT(0) FROM SX_INVENTARIO_INI WHERE YEAR=?",new Object[]{year});
		Assert.isTrue(count==0,"Ya existe INI para :"+year);
	}

	public ExistenciaDao getExistenciaDao() {
		return existenciaDao;
	}

	public void setExistenciaDao(ExistenciaDao existenciaDao) {
		this.existenciaDao = existenciaDao;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	

	public static void main(String[] args) {
		ExistenciasServices manager=new ExistenciasServices();
		manager.setExistenciaDao(ServiceLocator2.getExistenciaDao());
		manager.setJdbcTemplate(ServiceLocator2.getJdbcTemplate());
		manager.cerrar(2010);
	}
	

}
