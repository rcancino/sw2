package com.luxsoft.siipap.cxp.dao;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxp.model.ContraRecibo;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;

public class ContraReciboDaoImpl extends GenericDaoHibernate<ContraRecibo, Long> implements ContraReciboDao{

	public ContraReciboDaoImpl() {
		super(ContraRecibo.class);
	}
	
	public ContraRecibo buscarInicializado(final Long id){
		List<ContraRecibo> res=getHibernateTemplate()
		.find("from ContraRecibo c left join fetch c.partidas p where c.id=?", id);
		return res.isEmpty()?null:res.get(0);
	}

	public List<ContraReciboDet> buscarPartidas(final ContraRecibo recibo){
		return getHibernateTemplate().find("from ContraReciboDet d where d.recibo.id=?", recibo.getId());
	}

	public List<ContraRecibo> buscarRecibos(final Periodo p) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				return session.createQuery("from ContraRecibo c where c.fecha between ? and ?")
				.setParameter(0, p.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, p.getFechaFinal(),Hibernate.DATE)
				.list();
			}
			
		});
	}
	
	/**
	 * Buscar recibos pendientes de analisis
	 * 
	 * @param p
	 * @param tipo
	 * @return
	 */
	public List<ContraReciboDet> buscarRecibosPendientes(final Proveedor p,ContraReciboDet.Tipo tipo){
		String hql="from ContraReciboDet d where d.recibo.proveedor.id=? and d.cargoAbono is null and d.tipo=\'@DAT\'";
		hql=hql.replaceAll("@DAT", tipo.name());
		
		return getHibernateTemplate().find(hql, p.getId());
	}
	
	/**
	 * Busca recibos pendientes de analisis
	 * 
	 * @param tipo
	 * @return
	 */
	public List<ContraReciboDet> buscarRecibosPendientes(ContraReciboDet.Tipo tipo){
		String hql="from ContraReciboDet d where d.cargoAbono is null and d.tipo=\'@DAT\'";
		hql=hql.replaceAll("@DAT", tipo.name());
		
		return getHibernateTemplate().find(hql);
	}

}
