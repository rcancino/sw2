package com.luxsoft.sw3.cfd.dao;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

public class ComprobanteFiscalDaoImpl extends GenericDaoHibernate<ComprobanteFiscal, String> implements ComprobanteFiscalDao{

	public ComprobanteFiscalDaoImpl() {
		super(ComprobanteFiscal.class);
		
	}

}
