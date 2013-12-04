package com.luxsoft.siipap.inventarios.dao;

import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.inventarios.model.Movimiento;

public class MovimientoDaoImpl extends GenericDaoHibernate<Movimiento, String> implements MovimientoDao{

	public MovimientoDaoImpl() {
		super(Movimiento.class);
	}

	@Transactional(propagation=Propagation.SUPPORTS)
	public Movimiento save(Movimiento object) {
		return super.save(object);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Movimiento get(String id) {
		
		Movimiento m=super.get(id);
		Hibernate.initialize(m.getPartidas());
		return m;
	}

	
	 

}
