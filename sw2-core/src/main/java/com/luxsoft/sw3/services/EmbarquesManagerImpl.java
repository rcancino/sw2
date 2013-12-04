package com.luxsoft.sw3.services;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.core.FolioDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Folio;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.embarque.Entrega;

@Service("embarquesManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class EmbarquesManagerImpl implements EmbarquesManager{
	
	@SuppressWarnings("unused")
	private Logger logger=Logger.getLogger(getClass());
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	
	@Autowired
	private FolioDao folioDao;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	

	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	public Embarque getEmbarquer(String id) {
		Embarque r=(Embarque)this.hibernateTemplate.get(Embarque.class, id);
		
		for(Entrega e:r.getPartidas()){
			//e.getPartidas().iterator().next();
			e.getFactura().getPedido().getClave();
		}
		return r;
	}

	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	public Embarque salvarEmbarque(Embarque e,Sucursal sucursal) {
		registrarBitacora(e);
		boolean replicar=sucursal.getId()!=1L;
		Embarque res=doSaveInTransaction(e, sucursal,replicar);
		return res;
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	private Embarque doSaveInTransaction(final Embarque e,Sucursal sucursal,boolean replicar){
		if(e.getId()==null){			
			String tipo="EMBARQUE";
			Folio folio=folioDao.buscarNextFolio(sucursal, tipo);
			e.setDocumento(folio.getFolio());
			folioDao.save(folio);
		}
		if(replicar){
			e.setReplicado(null);//Garantizar  q modificaciones en la oficinas sean replicables
		}
		e.setImportado(null); //Garantiza q modificaciones en las sucursales se importen
		Embarque res= (Embarque)this.hibernateTemplate.merge(e);
		return getEmbarquer(res.getId());
	}
	
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public Entrega salvarEntrega(Entrega e, Sucursal sucursal) {
		registrarBitacora(e);
		Entrega res=(Entrega)this.hibernateTemplate.merge(e);
		return res;
	}

	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	private void registrarBitacora(Embarque bean){
		Date time=obtenerFechaDelSistema();
		
		String user=KernellSecurity.instance().getCurrentUserName();	
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();
		
		bean.getLog().setModificado(time);
		bean.getLog().setUpdateUser(user);
		bean.getAddresLog().setUpdatedIp(ip);
		bean.getAddresLog().setUpdatedMac(mac);
		
		
		if(bean.getId()==null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			bean.getAddresLog().setCreatedIp(ip);
			bean.getAddresLog().setUpdatedMac(mac);
		}
		
	}
	
	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	private void registrarBitacora(Entrega bean){
		Date time=obtenerFechaDelSistema();
		
		String user=KernellSecurity.instance().getCurrentUserName();	
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();
		
		bean.getLog().setModificado(time);
		bean.getLog().setUpdateUser(user);
		bean.getAddresLog().setUpdatedIp(ip);
		bean.getAddresLog().setUpdatedMac(mac);
		
		
		if(bean.getId()==null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			bean.getAddresLog().setCreatedIp(ip);
			bean.getAddresLog().setUpdatedMac(mac);
		}
		
	}

	private synchronized Date obtenerFechaDelSistema(){
		return (Date)jdbcTemplate.queryForObject("select now()", Date.class);
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}


	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}


	public FolioDao getFolioDao() {
		return folioDao;
	}


	public void setFolioDao(FolioDao folioDao) {
		this.folioDao = folioDao;
	}
	
	

}
