package com.luxsoft.sw3.maquila.dao;

import org.springframework.stereotype.Service;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.sw3.maquila.model.AnalisisDeMaterial;


@Service("analisisDeMaterialDao")
public class AnalisisDeMaterialDaoImpl extends GenericDaoHibernate<AnalisisDeMaterial, Long> implements AnalisisDeMaterialDao{

	public AnalisisDeMaterialDaoImpl() {
		super(AnalisisDeMaterial.class);
	}

}
