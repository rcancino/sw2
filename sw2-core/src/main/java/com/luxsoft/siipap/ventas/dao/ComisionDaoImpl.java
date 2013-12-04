package com.luxsoft.siipap.ventas.dao;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.ventas.model.ComisionVenta;

public class ComisionDaoImpl extends GenericDaoHibernate<ComisionVenta, Long> implements ComisionDao{

	public ComisionDaoImpl() {
		super(ComisionVenta.class);
		
	}
	
}
