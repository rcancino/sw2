package com.luxsoft.siipap.compras.dao;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.compras.model.CompraDet;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;

public class EntradaPorCompraDaoImpl extends GenericDaoHibernate<EntradaPorCompra, String> implements EntradaPorCompraDao{

	public EntradaPorCompraDaoImpl() {
		super(EntradaPorCompra.class);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.compras.dao.CompraDao#buscarEntradas(java.lang.Long[])
	 */
	public List<CompraDet> buscarEntradas(final Long... comprasIds) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				Criteria c=session.createCriteria(CompraDet.class)
				.add(Restrictions.in("compraId", comprasIds));
				return c.list();
			}			
		});
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.compras.dao.EntradaPorCompraDao#buscarEntradas(com.luxsoft.siipap.model.Periodo)
	 */
	public List<EntradaPorCompra> buscarEntradas(final Periodo periodo){
		
		//return getHibernateTemplate().find(hql, new Object[]{periodo.getFechaInicial(),periodo.getFechaInicial()});
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				final String hql="from EntradaPorCompra e left join fetch e.compraDet where date(e.fecha) between ? and ?";
				return session.createQuery(hql)
				.setParameter(0, periodo.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, periodo.getFechaFinal(),Hibernate.DATE)
				.list();
				
			}
			
		});
	}
	
	public EntradaPorCompra buscarEntrada(Long comId){
		final String hql="from EntradaPorCompra e left join fetch e.compraDet where e.coms2Id=?";
		List<EntradaPorCompra> res=getHibernateTemplate().find(hql,comId);
		return res.isEmpty()?null:res.get(0);
	}
	
	public List<EntradaPorCompra> buscarAnalisisPendientes(final Proveedor proveedor,final Periodo periodo){
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				List<EntradaPorCompra> res= session.createQuery("from EntradaPorCompra e where " +
						" e.proveedor.id=?" +
						" and date(e.fecha) between ? and ?")
						.setLong(0, proveedor.getId())
						.setParameter(1, periodo.getFechaInicial(),Hibernate.DATE)
						.setParameter(2, periodo.getFechaFinal(),Hibernate.DATE)
						.list();
				CollectionUtils.filter(res, new Predicate(){
					public boolean evaluate(Object object) {
						EntradaPorCompra e=(EntradaPorCompra)object;
						return e.getPendienteDeAnalisis()>0;
					}
					
				});
				return res;
			}
			
		});
	}
	
	public static void main(String[] args) {
		EntradaPorCompraDao dao=(EntradaPorCompraDao)ServiceLocator2.instance().getContext().getBean("entradaPorCompraDao");
		/*EntradaPorCompra e=dao.get("8a8a81c7-20abad0f-0120-abad223e-0002");
		System.out.println("Entradas: "+e.getCantidad());
		System.out.println("Analizado: "+e.getAnalizado());
		System.out.println("Por Analizar "+e.getPendienteDeAnalisis());*/
		List data=dao.buscarEntradas(new Periodo("01/01/2009","31/01/2009"));
		System.out.println("Entradas: "+data.size());
	}

}
