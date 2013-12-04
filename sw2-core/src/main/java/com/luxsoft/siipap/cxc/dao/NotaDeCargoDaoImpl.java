package com.luxsoft.siipap.cxc.dao;

import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.service.ServiceLocator2;

public class NotaDeCargoDaoImpl extends GenericDaoHibernate<NotaDeCargo, String> implements NotaDeCargoDao{

	public NotaDeCargoDaoImpl() {
		super(NotaDeCargo.class);
	}

	
	
	
	public static void main(String[] args) {
		ServiceLocator2.getCXCManager().cancelarNotaDeCargo("8a8a8189-215fc6d8-0121-5fc77a54-00c2");
	}

}
