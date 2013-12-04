package com.luxsoft.sw3.maquila.dao;

import org.springframework.stereotype.Service;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;

@Service("recepcionDeMaquilaDao")
public class RecepcionDeMaquilaDaoImpl extends GenericDaoHibernate<RecepcionDeMaquila, String> implements RecepcionDeMaquilaDao{

	public RecepcionDeMaquilaDaoImpl() {
		super(RecepcionDeMaquila.class);
	}

}
