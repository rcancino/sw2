package com.luxsoft.siipap.dao.tesoreria;

import org.hibernate.LockMode;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.tesoreria.Transferencia;

public class TransferenciaDaoImpl extends GenericDaoHibernate<Transferencia, Long> implements TransferenciaDao{

	public TransferenciaDaoImpl() {
		super(Transferencia.class);
	}

	@Transactional
	public void remove(final Long id) {		
		getHibernateTemplate().delete(get(id), LockMode.FORCE);
	}
	
	

	

	
	
}
