package com.luxsoft.siipap.service.core;



import java.util.Date;
import java.util.List;

import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.impl.GenericManagerImpl;
import com.luxsoft.sw3.replica.EntityLog;

public class ProductoManagerImpl extends GenericManagerImpl<Producto, Long> implements ProductoManager{
	
	

	public ProductoManagerImpl(GenericDao<Producto, Long> genericDao) {
		super(genericDao);		
	}
	
	private ProductoDao getDao(){
		return (ProductoDao)genericDao;
	}

	@Override
	public Producto get(Long id) {
		return getDao().get(id);
	}

	public Producto buscarPorClave(String clave) {
		return ((ProductoDao)genericDao).buscarPorClave(clave);
	}
	
	@Override
	@Transactional( propagation=Propagation.REQUIRED)
	public Producto save(Producto object) {	
		if(object.getId()!=null)
			object.getUserLog().setModificado(new Date());
		Producto res=super.save(object);
		//enviarJms(res);
		return res;
	}

	public List<Producto> buscarProductosActivos() {
		return ((ProductoDao)genericDao).buscarActivos();
	}
	
	public List<Producto> buscarProductosActivosYDeLinea() {
		return ((ProductoDao)genericDao).buscarProductosActivosYDeLinea();
	}
	
	private JmsTemplate jmsTemplate;

	private void enviarJms(Producto res){
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
