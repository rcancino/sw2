package com.luxsoft.sw3.cfd.dao;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.sw3.cfd.model.FolioFiscal;
import com.luxsoft.sw3.cfd.model.FolioFiscalId;
import com.luxsoft.sw3.cfd.model.SerieNoExistenteException;

@Component("folioFiscalDao")
public class FolioFiscalDaoImpl extends GenericDaoHibernate<FolioFiscal, FolioFiscalId> implements FolioFiscalDao{

	public FolioFiscalDaoImpl() {
		super(FolioFiscal.class);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public FolioFiscal save(FolioFiscal folio) {		
		return super.save(folio);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public FolioFiscal buscarFolio(Sucursal s, String serie) throws SerieNoExistenteException{
		String hql="from FolioFiscal f where f.id.sucursal=? and f.id.serie=?";
		List<FolioFiscal> res=getHibernateTemplate().find(hql, new Object[]{s.getId(),serie});
		if(res.isEmpty()){
			throw new SerieNoExistenteException(serie);
		}
		else 
			return res.get(0);
	}

}
