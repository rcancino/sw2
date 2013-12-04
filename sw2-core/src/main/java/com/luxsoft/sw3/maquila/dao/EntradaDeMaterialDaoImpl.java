package com.luxsoft.sw3.maquila.dao;

import org.springframework.stereotype.Service;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterial;

@Service("entradaDeMaterialDao")
public class EntradaDeMaterialDaoImpl extends GenericDaoHibernate<EntradaDeMaterial, Long> implements EntradaDeMaterialDao{

	public EntradaDeMaterialDaoImpl() {
		super(EntradaDeMaterial.class);
	}

}
