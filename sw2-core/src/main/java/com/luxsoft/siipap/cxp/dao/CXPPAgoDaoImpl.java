package com.luxsoft.siipap.cxp.dao;

import com.luxsoft.siipap.cxp.model.CXPAnticipo;
import com.luxsoft.siipap.cxp.model.CXPPago;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;

public class CXPPAgoDaoImpl extends GenericDaoHibernate<CXPPago, Long> implements CXPPAgoDao{

	public CXPPAgoDaoImpl() {
		super(CXPPago.class);
	}
	
	public CXPAnticipo salvarAnticipo(CXPAnticipo anticipo){
		return (CXPAnticipo)getHibernateTemplate().merge(anticipo);
	}

}
