package com.luxsoft.sw3.maquila.dao;

import org.springframework.stereotype.Service;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.sw3.maquila.model.OrdenDeCorte;

@Service("ordenDeCorteDao")
public class OrdenDeCorteDaoImpl extends GenericDaoHibernate<OrdenDeCorte, Long> implements OrdenDeCorteDao{

	public OrdenDeCorteDaoImpl() {
		super(OrdenDeCorte.class);
	}

}
