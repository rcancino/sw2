package com.luxsoft.sw3.maquila.dao;

import org.springframework.stereotype.Service;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.sw3.maquila.model.SalidaDeBobinas;


@Service("salidaDeBobinasDao")
public class SalidaDeBobinasDaoImpl extends GenericDaoHibernate<SalidaDeBobinas, Long> implements SalidaDeBobinasDao{

	public SalidaDeBobinasDaoImpl() {
		super(SalidaDeBobinas.class);
	}

}
