package com.luxsoft.siipap.dao.gastos;

import java.util.List;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;

public class GClasificacionDaoImpl extends GenericDaoHibernate<ConceptoDeGasto, Long> implements GClasificacionDao{

	public GClasificacionDaoImpl() {
		super(ConceptoDeGasto.class);
		
	}

	@SuppressWarnings("unchecked")
	public ConceptoDeGasto buscarPorClave(String clave) {
		List<ConceptoDeGasto> l=getHibernateTemplate().find("from ConceptoDeGasto c where c.clave=?", clave);
		return l.isEmpty()?null:l.get(0);
	}

}
