package com.luxsoft.sw3.services;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import com.luxsoft.siipap.dao.gastos.ActivoFijoDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.ActivoFijo;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.INPC;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.utils.LoggerHelper;

@Service("activoFijoManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class ActivoFijoManager {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	
	
	Logger logger=LoggerHelper.getLogger();
	
	@Transactional(propagation=Propagation.REQUIRED)
	public ActivoFijo salvar(ActivoFijo af){
		registrarBitacura(af);
		return (ActivoFijo)getHibernateTemplate().merge(af);
	}
	
	
	
	@Transactional(propagation=Propagation.MANDATORY)
	private void registrarBitacura(ActivoFijo bean){
		Date time=new Date();
		String user=KernellSecurity.instance().getCurrentUserName();	
		//String ip=KernellSecurity.getIPAdress();
		//String mac=KernellSecurity.getMacAdress();
		
		bean.getLog().setModificado(time);
		bean.getLog().setUpdateUser(user);
		
		if(bean.getId()==null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			
		}
	}
	
	public INPC buscarIndice(Date fecha){
		int year=Periodo.obtenerYear(fecha);
		int mes=Periodo.obtenerMes(fecha)+1;
		String hql="from INPC x where x.year=? and x.mes=?";
		List<INPC> res=getHibernateTemplate().find(hql,new Object[]{year,mes});
		return res.isEmpty()?null:res.get(0);
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}



	public Collection buscarTodos() {
		return getHibernateTemplate().find("from ActivoFijo af where af.venta is null");
	}
	

}
