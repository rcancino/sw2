package com.luxsoft.sw3.services;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.sw3.ventas.CheckPlusCliente;
import com.luxsoft.sw3.ventas.CheckPlusOpcion;


@Service("checkplusManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class CheckplusManager {
	
	@SuppressWarnings("unused")
	private Logger logger=Logger.getLogger(getClass());
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	

	@Transactional(propagation=Propagation.NEVER,readOnly=false)
	public void generarOpcionesIniciales(){
		Map<Integer, BigDecimal> map=new HashMap<Integer, BigDecimal>();
		map.put(0, BigDecimal.valueOf(2));
		map.put(8, BigDecimal.valueOf(2.5));
		map.put(15, BigDecimal.valueOf(3));
		map.put(23, BigDecimal.valueOf(3.5));
		map.put(30, BigDecimal.valueOf(4));
		map.put(38, BigDecimal.valueOf(4.5));
		map.put(45, BigDecimal.valueOf(5));
		
		for (Map.Entry<Integer,BigDecimal> e:map.entrySet()) {
			try {
				CheckPlusOpcion cp=new CheckPlusOpcion();
				cp.setPlazo(e.getKey());
				cp.setCargo(e.getValue());
				cp.setComentario("Dias");
				salvar(cp);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}	
	}
	
	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	public CheckPlusOpcion salvar(CheckPlusOpcion opcion){
		KernellSecurity.instance().registrarUserLog(opcion, "log");
		KernellSecurity.instance().registrarAddressLog(opcion, "addresLog");
		return (CheckPlusOpcion)hibernateTemplate.merge(opcion);
	}
	
	public CheckPlusCliente buscarCliente(final String id){
		return (CheckPlusCliente)hibernateTemplate.execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				CheckPlusCliente c=(CheckPlusCliente)session.load(CheckPlusCliente.class,id);
				c.getDigitalizacion();
				Hibernate.initialize(c.getDocumentos());
				Hibernate.initialize(c.getReferenciasBancarias());
				return c;
			}
		});
	}


	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
}
