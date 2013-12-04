package com.luxsoft.siipap.service.ventas;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.utils.LoggerHelper;


@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class PedidosManagerImpl implements PedidosManager{
	
	private Logger logger=LoggerHelper.getLogger();
	
	private HibernateTemplate hibernateTemplate;

	@Transactional(propagation=Propagation.REQUIRED)
	public Pedido salvar(Pedido pedido) {
		
		return (Pedido)getHibernateTemplate().merge(pedido);
	}


	public Pedido get(String id) {
		Pedido p= (Pedido)getHibernateTemplate().get(Pedido.class,id);
		Hibernate.initialize(p.getCliente().getTelefonos());
		Hibernate.initialize(p.getCliente().getContactos());
		Hibernate.initialize(p.getCliente().getComentarios());
		//System.out.println("Telefonos inicializados: "+p.getCliente().getTelefonos().size());
		return p;
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}




	
	
	
	
	
}
