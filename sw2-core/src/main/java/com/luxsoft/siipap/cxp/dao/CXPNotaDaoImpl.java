package com.luxsoft.siipap.cxp.dao;

import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPNota;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;

public class CXPNotaDaoImpl extends GenericDaoHibernate<CXPNota, Long> implements CXPNotaDao{

	public CXPNotaDaoImpl() {
		super(CXPNota.class);
		
	}

	@Override
	public CXPNota save(CXPNota object) {
		for(CXPAplicacion a:object.getAplicaciones()){
			a.setTipoAbono(object.getConcepto().name());
		}
		return super.save(object);
	}
	
	

}
