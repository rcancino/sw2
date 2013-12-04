package com.luxsoft.siipap.inventarios.dao;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.inventarios.model.InventarioAnual;

public class InventarioAnualDaoImpl extends GenericDaoHibernate<InventarioAnual, Long> implements InventarioAnualDao{
	
	
	public InventarioAnualDaoImpl() {
		super(InventarioAnual.class);
	}
	
	

}
