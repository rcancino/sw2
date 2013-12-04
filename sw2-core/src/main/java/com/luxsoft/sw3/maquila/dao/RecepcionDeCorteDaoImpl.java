package com.luxsoft.sw3.maquila.dao;

import org.springframework.stereotype.Service;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorte;

@Service("recepcionDeCorteDao")
public class RecepcionDeCorteDaoImpl extends GenericDaoHibernate<RecepcionDeCorte, Long> implements RecepcionDeCorteDao{

	public RecepcionDeCorteDaoImpl() {
		super(RecepcionDeCorte.class);
	}

}
