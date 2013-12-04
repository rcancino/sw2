package com.luxsoft.sw3.tesoreria.dao;

import org.springframework.stereotype.Service;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;;

@Service("solicitudDeDepositosDao")
public class SolicitudDeDepositosDaoImpl extends GenericDaoHibernate<SolicitudDeDeposito, String> implements SolicitudDeDepositosDao{

	public SolicitudDeDepositosDaoImpl() {
		super(SolicitudDeDeposito.class);		
	}

}
