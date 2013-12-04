package com.luxsoft.siipap.dao;

import java.util.List;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Sucursal;

public class SucursalDaoImpl extends GenericDaoHibernate<Sucursal, Long> implements SucursalDao{

	public SucursalDaoImpl() {
		super(Sucursal.class);
		
	}

	public Sucursal buscarPorClave(Integer clave) {
		String hql="from Sucursal s where s.clave=?";
		List<Sucursal> l=getHibernateTemplate().find(hql, clave);
		return l.isEmpty()?null:l.get(0);
	}

}
