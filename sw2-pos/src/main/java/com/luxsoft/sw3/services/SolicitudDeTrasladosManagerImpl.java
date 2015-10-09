package com.luxsoft.sw3.services;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.dao.core.FolioDao;
import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.inventarios.model.SolicitudDeTrasladoDet;
import com.luxsoft.siipap.model.core.Folio;
import com.luxsoft.siipap.service.KernellSecurity;

@Service("solicitudDeTrasladosManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class SolicitudDeTrasladosManagerImpl implements SolicitudDeTrasladosManager{
	
	@Autowired
	private UniversalDao universalDao;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private FolioDao folioDao;
	
	public boolean exists(String id) {
		throw new UnsupportedOperationException("TO BE IMPLEMENTED");
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public SolicitudDeTraslado get(String id) {
		String hql="SolicitudDeTraslado s left join fetch s.partidas where s.id=?";
		List<SolicitudDeTraslado> sols=hibernateTemplate.find(hql, id);
		return sols.isEmpty()?null:sols.get(0);
	}

	public List<SolicitudDeTraslado> getAll() {
		return universalDao.getAll(SolicitudDeTraslado.class);
	}

	public void remove(String id) {
		universalDao.remove(SolicitudDeTraslado.class, id);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public SolicitudDeTraslado cancelar(SolicitudDeTraslado sol) {
		sol.getPartidas().clear();
		sol.setComentario("CANCELADO");
		return (SolicitudDeTraslado)universalDao.save(sol);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public SolicitudDeTraslado save(SolicitudDeTraslado sol) {
		
		Iterator<SolicitudDeTrasladoDet> iter=sol.getPartidas().listIterator();
		//Eliminamos las posibles partidas en cero
		while(iter.hasNext()){
			SolicitudDeTrasladoDet sd=iter.next();
			if(sd.getSolicitado()<=0)
				iter.remove();
		}
		
		if(sol.getClasificacion().equals("CONTRAVALE")){
			
			
			for(SolicitudDeTrasladoDet det :sol.getPartidas()){
		
				det.setRecibido(det.getSolicitado());
			
			}
		}
		
		Folio folio=folioDao.buscarNextFolio(sol.getSucursal(), "TRASLADO_SOL");
		sol.setDocumento(folio.getFolio());
		folioDao.save(folio);
		return (SolicitudDeTraslado)universalDao.save(sol);
	}

}
