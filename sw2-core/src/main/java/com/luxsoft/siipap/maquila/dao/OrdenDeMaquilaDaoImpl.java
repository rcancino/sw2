package com.luxsoft.siipap.maquila.dao;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;

public class OrdenDeMaquilaDaoImpl extends GenericDaoHibernate<RecepcionDeMaquila, Long> implements OrdenDeMaquilaDao{

	public OrdenDeMaquilaDaoImpl() {
		super(RecepcionDeMaquila.class);
	}

	public EntradaDeMaquila buscarPorComId(Long comId, int renglon) {
		// TODO Auto-generated method stub
		return null;
	}

	public RecepcionDeMaquila buscarPorFolioSucursal(int sucursal, int folio) {
		// TODO Auto-generated method stub
		return null;
	}

}
