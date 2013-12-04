package com.luxsoft.sw3.services;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.sw3.bi.SimuladorDePreciosPorCliente;
import com.luxsoft.sw3.bi.SimuladorDePreciosPorClienteDet;

/**
 * 
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Service("simuladorDePreciosManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class SimuladorDePreciosManager {
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	
	protected Logger logger=Logger.getLogger(getClass());
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public SimuladorDePreciosPorCliente get(Long id) {
		SimuladorDePreciosPorCliente lista= (SimuladorDePreciosPorCliente)hibernateTemplate.get(SimuladorDePreciosPorCliente.class,id);
		hibernateTemplate.initialize(lista.getPrecios());
		return lista;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public SimuladorDePreciosPorCliente save(SimuladorDePreciosPorCliente sol) {
		registrarBitacora(sol);
		sol=(SimuladorDePreciosPorCliente)hibernateTemplate.merge(sol);
		return sol;
	}
	
	private void registrarBitacora(SimuladorDePreciosPorCliente bean){
		Date time=new Date();
		
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
			bean.getAddresLog().setCreatedMac(mac);
		}
	}	
	
	@Transactional(propagation=Propagation.REQUIRED)
	public SimuladorDePreciosPorCliente copiarListaDePrecios(Long id, Cliente c) {
		Assert.notNull(c);
		Assert.notNull(c.getCredito());
		SimuladorDePreciosPorCliente source=get(id);
		SimuladorDePreciosPorCliente target=new SimuladorDePreciosPorCliente();
		BeanUtils.copyProperties(source, target,new String[]{"id","cliente","version","precios"});
		target.setCliente(c);
		target.setComentario("Copia de lista: "+source.getId());
		for(SimuladorDePreciosPorClienteDet det:source.getPrecios()){
			SimuladorDePreciosPorClienteDet tar=new SimuladorDePreciosPorClienteDet(); 
			BeanUtils.copyProperties(det, tar,new String[]{"id","lista"});
			target.agregarPrecio(tar);
		}
		return save(target);
	}
	
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void remove(Long id) {
		SimuladorDePreciosPorCliente lista=get(id);
		lista.getPrecios().clear();
		lista.setComentario("ELIMINADA");
		
		save(lista);		
	}

	

	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	
	

}
