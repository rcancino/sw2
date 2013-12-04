package com.luxsoft.siipap.service.core;



import java.util.List;

import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQTopic;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.compras.dao.ProveedorDao;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.impl.GenericManagerImpl;
import com.luxsoft.sw3.replica.EntityLog;

@Service("proveedoresManager")
public class ProveedorManagerImpl extends GenericManagerImpl<Proveedor, Long> implements ProveedorManager{

	@Autowired	
	public ProveedorManagerImpl(GenericDao<Proveedor, Long> genericDao) {
		super(genericDao);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Proveedor get(Long id) {
		Proveedor p=super.get(id);
		p.getComentarios().size();
		Hibernate.initialize(p.getComentarios());
		Hibernate.initialize(p.getProductos());
		return p;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Proveedor buscarInicializado(final String clave){
		Proveedor p=buscarPorClave(clave);
		Hibernate.initialize(p.getComentarios());
		Hibernate.initialize(p.getProductos());
		return p;
	}
	

	private ProveedorDao getProveedorDao(){
		return (ProveedorDao)genericDao;
	}
	
	public Proveedor buscarPorClave(String clave) {
		Proveedor res= getProveedorDao().buscarPorClave(clave);
		
		return res;
	}

	public Proveedor buscarPorNombre(String nombre) {
		return getProveedorDao().buscarPorNombre(nombre);
	}

	public Proveedor buscarPorRfc(String rfc) {
		return getProveedorDao().buscarPorRfc(rfc);
	}

	

	public List<Proveedor> buscarActivos(){
		return getProveedorDao().buscarActivos();
	}

	public List<Proveedor> buscarImportadores() {
		return getProveedorDao().buscarImportadores();
	}
	
	private JmsTemplate jmsTemplate;

	private void enviarJms(Proveedor res){
		try {
			EntityLog log=new EntityLog(res,res.getId(),"OFICINAS",EntityLog.Tipo.CAMBIO);
			Topic topic=new ActiveMQTopic("REPLICA.TOPIC");
			jmsTemplate.convertAndSend(topic, log);			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

}
