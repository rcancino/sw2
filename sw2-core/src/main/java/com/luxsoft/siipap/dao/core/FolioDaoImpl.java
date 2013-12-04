package com.luxsoft.siipap.dao.core;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Folio;
import com.luxsoft.siipap.model.core.FolioId;

@Component("folioDao")
public class FolioDaoImpl extends GenericDaoHibernate<Folio, FolioId> implements FolioDao{

	public FolioDaoImpl() {
		super(Folio.class);
	}
	
	

	@Transactional(propagation=Propagation.REQUIRED)
	public Folio save(Folio object) {		
		return super.save(object);
	}



	@Transactional(propagation=Propagation.REQUIRED)
	public Folio buscarNextFolio(Sucursal s, String tipo) {
		String hql="from Folio f" +
				
				" where f.id.sucursal=? and f.id.tipo=?";
		List<Folio> res=getHibernateTemplate().find(hql, new Object[]{s.getId(),tipo});
		Folio f;
		if(res.isEmpty()){
			FolioId id=new FolioId(s.getId(),tipo);
			f=new Folio();
			f.setId(id);
			f.next();
			//return save(f);
			return f;
		}else{
			f=res.get(0);
			f.next();
			//return save(f);
			return f;
		}
	}

}
