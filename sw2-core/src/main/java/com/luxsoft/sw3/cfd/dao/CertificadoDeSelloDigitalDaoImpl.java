package com.luxsoft.sw3.cfd.dao;



import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.sw3.cfd.model.CFDException;
import com.luxsoft.sw3.cfd.model.CertificadoDeSelloDigital;

@Component("certificadoDeSelloDigitalDao")
public class CertificadoDeSelloDigitalDaoImpl extends GenericDaoHibernate<CertificadoDeSelloDigital, Long> 
	implements CertificadoDeSelloDigitalDao{

	public CertificadoDeSelloDigitalDaoImpl() {
		super(CertificadoDeSelloDigital.class);
		
	}

	@Override
	public CertificadoDeSelloDigital save(CertificadoDeSelloDigital object) {
		if(object.getId()==null)
			object.setId(1L);
		object.resolverNumero();
		object.resolverVencimiento();
		object.setReplicado(null);
		registrarBitacora(object);
		return super.save(object);
	}


	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	private void registrarBitacora(CertificadoDeSelloDigital bean){
		Date time=new Date();		
		String user=KernellSecurity.instance().getCurrentUserName();	
		//String ip=KernellSecurity.getIPAdress();
		//String mac=KernellSecurity.getMacAdress();
		
		bean.getLog().setModificado(time);
		bean.getLog().setUpdateUser(user);
		//bean.getAddresLog().setUpdatedIp(ip);
		//bean.getAddresLog().setUpdatedMac(mac);
		
		
		if(bean.getId()==null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			//bean.getAddresLog().setCreatedIp(ip);
			//bean.getAddresLog().setUpdatedMac(mac);
		}
		
	}

	/**
	 * En esta implementación el sello deigital se busca mediante el numero
	 * de almacenado como propiedad del sistema
	 * 
	 */
	public CertificadoDeSelloDigital buscarCertificadoVigente(Map contexto){
		String serie=System.getProperty("cfd.certificado.serie");
		String hql="from CertificadoDeSelloDigital c where c.numeroDeCertificado=?";
		List<CertificadoDeSelloDigital> res=getHibernateTemplate().find(hql, serie);
		if(res.isEmpty())
			throw new CFDException("No existe un bean de certificado de sello digital registrado con el numero de serie: "+serie);
		return res.get(0);
	}

}
