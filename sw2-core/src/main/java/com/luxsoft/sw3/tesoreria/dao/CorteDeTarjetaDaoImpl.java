package com.luxsoft.sw3.tesoreria.dao;

import org.springframework.stereotype.Service;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;

@Service("corteDeTarjetaDao")
public class CorteDeTarjetaDaoImpl extends GenericDaoHibernate<CorteDeTarjeta, Long> implements CorteDeTarjetaDao{

	public CorteDeTarjetaDaoImpl() {
		super(CorteDeTarjeta.class);		
	}

}
