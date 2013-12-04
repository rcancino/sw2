package com.luxsoft.sw3.contabilidad.dao;


import java.util.List;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;

public class CuentaContableDaoImpl extends GenericDaoHibernate<CuentaContable, Long> implements CuentaContableDao{

	public CuentaContableDaoImpl() {
		super(CuentaContable.class);
	}

	public CuentaContable buscarPorClave(String clave) {
		String hql="from CuentaContable c where c.clave=?";
		List<CuentaContable> res=getHibernateTemplate().find(hql, clave);
		return res.isEmpty()?null:res.get(0);
	}

}
