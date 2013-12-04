package com.luxsoft.siipap.dao.tesoreria;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.tesoreria.Conciliacion;

public class ConciliacionDaoImpl extends GenericDaoHibernate<Conciliacion, Long> implements ConciliacionDao{

	public ConciliacionDaoImpl() {
		super(Conciliacion.class);
		
	}

}
