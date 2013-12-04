package com.luxsoft.sw3.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.core.FolioDao;
import com.luxsoft.siipap.model.core.Folio;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.sw3.solicitudes.SolicitudDeModificacion;


@Service("solicitudDeModificacionesManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class SolicitudDeModificacionesManager {
	
	@SuppressWarnings("unused")
	private Logger logger=Logger.getLogger(getClass());
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private FolioDao folioDao;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	
	@Transactional(propagation=Propagation.REQUIRED)
	public SolicitudDeModificacion salvar(SolicitudDeModificacion sol){
		if(sol.getId()==null){
			Folio folio=folioDao.buscarNextFolio(sol.getSucursal(), "SOLICITUD_MODIFICACION");
			sol.setFolio(folio.getFolio());
			folioDao.save(folio);
		}
		KernellSecurity.instance().registrarUserLog(sol, "log");
		KernellSecurity.instance().registrarAddressLog(sol, "addresLog");
		sol=(SolicitudDeModificacion)this.hibernateTemplate.merge(sol);
		return sol;
	}
	
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public void setFolioDao(FolioDao folioDao) {
		this.folioDao = folioDao;
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}
	

}
