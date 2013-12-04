package com.luxsoft.siipap.ventas.dao;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVenta;

public class ListaDePreciosVentaDaoImpl extends GenericDaoHibernate<ListaDePreciosVenta, Long>
	implements ListaDePreciosVentaDao{

	public ListaDePreciosVentaDaoImpl() {
		super(ListaDePreciosVenta.class);
		
	}

	
}
