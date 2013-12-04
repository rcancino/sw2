package com.luxsoft.sw3.services;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.cxp.model.AnalisisDeTransformacion;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.service.KernellSecurity;




@Service("analisisTransformacionesManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class AnalisisDeTransfomracionesManager  {
	
	@Autowired
	private HibernateTemplate hibernateTemplate;

	
	@SuppressWarnings("unused")
	private Logger logger=Logger.getLogger(getClass());
	
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public AnalisisDeTransformacion getAnalisis(Long id) {
		List<AnalisisDeTransformacion> res=getHibernateTemplate().find("from AnalisisDeTransformacion" +
				" e left join fetch e.partidas p where e.id=?",id);
		return res.isEmpty()?null:res.get(0);
	}

	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public AnalisisDeTransformacion salvarAnalisis(final AnalisisDeTransformacion bean){
		Date time=new Date();		
		String user=KernellSecurity.instance().getCurrentUserName();
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();		
		bean.getLog().setUpdateUser(user);
		bean.getLog().setModificado(time);
		bean.getAddresLog().setUpdatedIp(ip);
		bean.getAddresLog().setUpdatedMac(mac);
		if(bean.getId()!=null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			bean.getAddresLog().setCreatedIp(ip);
			bean.getAddresLog().setCreatedMac(mac);
		}		
		AnalisisDeTransformacion res=(AnalisisDeTransformacion)getHibernateTemplate().merge(bean);
		return res;
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public void eliminarAnalisis(final AnalisisDeTransformacion a){
		getHibernateTemplate().delete(a);
	}	
	
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public AnalisisDeTransformacion generarCuentaPorPagar(final AnalisisDeTransformacion a){
		String user=KernellSecurity.instance().getCurrentUserName();
		CXPFactura cxp=new CXPFactura();
		cxp.setComentario(a.getComentario());
		cxp.setCreateUser(user);
		cxp.setDocumento(a.getFactura());
		cxp.setFecha(a.getFechaFactura());
		
		//cxp.setFlete(a.get);
		cxp.setImporte(a.getImporte());
		cxp.setImpuesto(a.getImpuesto());
		cxp.setImpuestoAnalizado(a.getImpuesto());
		//cxp.setImpuestoflete(impuestoflete);
		cxp.setProveedor(a.getProveedor());
		cxp.setRetencionflete(a.getRetencion());
		cxp.setTotal(a.getTotal());
		cxp.setTotalAnalizado(a.getTotal());
		cxp.setUpdateUser(user);
		cxp.actualizarVencimiento();
		a.setCxpFactura(cxp);
		return salvarAnalisis(a);
	}
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

}
