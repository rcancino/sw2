package com.luxsoft.siipap.inventarios.dao;

import java.util.List;

import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.cxp.model.ContraRecibo;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.model.Periodo;

public class TrasladoDaoImpl extends GenericDaoHibernate<Traslado,Long> implements TrasladoDao{

	public TrasladoDaoImpl() {
		super(Traslado.class);
	}
	
	@Override
	@Transactional (propagation=Propagation.REQUIRED)
	public Traslado save(Traslado c) {		
		return super.save(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Traslado get(Long id) {		
		final String hql="from Traslado t " +
				"  left join fetch t.partidas p" +
				"  where t.id=?";
		final List<Traslado> traslados=getHibernateTemplate().find(hql, id);

        if (traslados.isEmpty()) {
            log.warn("Uh oh, '" + Traslado.class+ "' object with id '" + id + "' not found...");
            throw new ObjectRetrievalFailureException(Traslado.class, id);
        }
        return traslados.get(0);
	}

	public List<Traslado> buscarTraslado(Periodo p) {
		return null;
	}

	public Traslado inicializarTraslado(Long id) {
		return null;
	}
	
	

}
