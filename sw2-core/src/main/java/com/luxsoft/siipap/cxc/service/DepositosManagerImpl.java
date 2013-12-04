package com.luxsoft.siipap.cxc.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.dao.core.FolioDao;
import com.luxsoft.siipap.model.core.Folio;
import com.luxsoft.siipap.service.KernellSecurity;

public class DepositosManagerImpl extends HibernateDaoSupport implements DepositosManager{
	
	@Autowired
	private FolioDao folioDao;
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Ficha save(Ficha ficha) {
		updateBitacora(ficha);
		if(ficha.getId()==null){
			String tipo="FICHAS";
			Folio folio=folioDao.buscarNextFolio(ficha.getSucursal(), tipo);
			ficha.setFolio(folio.getFolio().intValue());			
			folioDao.save(folio);
		}
		ficha.setImportado(null);
		ficha.setReplicado(null);
		ficha.actualizarTotal();
		return (Ficha)getHibernateTemplate().merge(ficha);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Ficha cancelarDeposito(final String fichaId) {
		Ficha bean=(Ficha)getHibernateTemplate().get(Ficha.class, fichaId);
		Assert.isNull(bean.getCorte(),"La ficha ya ha sido contabilizada en un ingreso al sistema de bancos");
		if(bean.getPartidas().size()==0)
			return bean;
		bean.getPartidas().clear();
		bean.setComentario("CANCELADO");
		bean.setCancelada(new Date());
		bean.getImporte();
		return save(bean);
		
		
	}
	
	private void updateBitacora(Ficha bean){
		
		String user=KernellSecurity.instance().getCurrentUserName();
		Date time=new Date();
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();
		if(bean.getId()==null){
			bean.getAddresLog().setCreatedIp(ip);
			bean.getAddresLog().setCreatedMac(mac);
			bean.setCreateUser(user);
			bean.setCreacion(time);
		}
		bean.getAddresLog().setUpdatedIp(ip);
		bean.getAddresLog().setUpdatedMac(mac);
		bean.setUpdateUser(user);
		bean.setModificado(time);
	}

	public FolioDao getFolioDao() {
		return folioDao;
	}

	public void setFolioDao(FolioDao folioDao) {
		this.folioDao = folioDao;
	}

	

}
