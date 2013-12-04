package com.luxsoft.sw3.services;

import java.util.Date;
import java.util.List;

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
import com.luxsoft.siipap.ventas.dao.ListaDePreciosClienteDao;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.siipap.ventas.model.ListaDePreciosClienteDet;

/**
 * Implementacion de {@link ListaDePreciosClienteManager}
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Service("listaDePreciosClienteManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class ListaDePreciosClienteManagerImpl implements ListaDePreciosClienteManager{
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private ListaDePreciosClienteDao listaDePreciosClienteDao;
	
	protected Logger logger=Logger.getLogger(getClass());
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public ListaDePreciosCliente get(Long id) {
		ListaDePreciosCliente lista= listaDePreciosClienteDao.get(id);
		hibernateTemplate.initialize(lista.getPrecios());
		return lista;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public ListaDePreciosCliente save(ListaDePreciosCliente sol) {
		registrarBitacora(sol);
		sol.setReplicado(null);
		sol.setImportado(null);
		sol=listaDePreciosClienteDao.save(sol);
		return sol;
	}
	
	private void registrarBitacora(ListaDePreciosCliente bean){
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
	public ListaDePreciosCliente copiarListaDePrecios(Long id, Cliente c) {
		Assert.notNull(c);
		Assert.notNull(c.getCredito());
		ListaDePreciosCliente source=get(id);
		ListaDePreciosCliente target=new ListaDePreciosCliente();
		BeanUtils.copyProperties(source, target,new String[]{"id","cliente","version","precios"});
		target.setCliente(c);
		target.setComentario("Copia de lista: "+source.getId());
		for(ListaDePreciosClienteDet det:source.getPrecios()){
			ListaDePreciosClienteDet tar=new ListaDePreciosClienteDet(); 
			BeanUtils.copyProperties(det, tar,new String[]{"id","lista"});
			target.agregarPrecio(tar);
		}
		return save(target);
	}
	
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void remove(Long id) {
		ListaDePreciosCliente lista=get(id);
		lista.getPrecios().clear();
		lista.setComentario("ELIMINADA");
		lista.setActivo(false);
		save(lista);		
	}

	

	public boolean exists(Long id) {
		return this.listaDePreciosClienteDao.exists(id);
	}

	public List<ListaDePreciosCliente> getAll() {
		return this.listaDePreciosClienteDao.getAll();
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public ListaDePreciosClienteDao getListaDePreciosClienteDao() {
		return listaDePreciosClienteDao;
	}

	public void setListaDePreciosClienteDao(
			ListaDePreciosClienteDao listaDePreciosClienteDao) {
		this.listaDePreciosClienteDao = listaDePreciosClienteDao;
	}

	

}
