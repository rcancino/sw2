package com.luxsoft.siipap.dao.gastos;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.gastos.ActivoFijo;

public class ActivoFijoDaoImpl extends GenericDaoHibernate<ActivoFijo, Long> implements ActivoFijoDao{

	public ActivoFijoDaoImpl() {
		super(ActivoFijo.class);
		
	}

}
