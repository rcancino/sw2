package com.luxsoft.siipap.cxc.dao;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;

public class PagoDaoImpl extends GenericDaoHibernate<Pago, String>implements PagoDao{

	public PagoDaoImpl() {
		super(Pago.class);
	}

	@Override
	public Pago save(Pago pago) {
		pago.acutalizarDisponible();		
		return super.save(pago);
	}

	public List<Pago> buscarPagosDisponibles(Cliente cliente) {
		
		String hql="from Pago p left join fetch p.aplicaciones ap where p.clave=? and p.total-p.aplicado>0";
		List data=getHibernateTemplate().find(hql, cliente.getClave());
		final EventList<Pago> source=GlazedLists.eventList(data);
		UniqueList<Pago> pagos=new UniqueList<Pago>(source,GlazedLists.beanPropertyComparator(Pago.class, "id"));
		return pagos;
		
	}

	public List<Pago> buscarPagos(final Periodo periodo) {		
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				final String hql="from Pago p left join fetch p.aplicaciones ap" +
						" left join fetch p.cobrador c " +
						"where p.fecha between ? and ?";
				List res=session.createQuery(hql)
				.setParameter(0, periodo.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, periodo.getFechaFinal(),Hibernate.DATE)
				.list();
				
				final EventList<Pago> source=GlazedLists.eventList(res);
				UniqueList<Pago> pagos=new UniqueList<Pago>(source,GlazedLists.beanPropertyComparator(Pago.class, "id"));				
				return pagos;
			}
			
		});
		
	}
	
	/**
	 * Regresa la lista de los pagos validos para administrar en tesoreria
	 * 
	 * @param periodo
	 * @return
	 */
	public List<Pago> buscarPagosEnTesoreria(final Periodo periodo){
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				/*final String hql="from Pago p left join fetch p.aplicaciones ap" +
						" left join fetch p.cobrador c " +
						"where p.tipo not in(\'PAGO_DIF\',\'PAGO_ESP\')  and p.fecha between ? and ?";*/
				final String hql="from Pago p " +
				"where p.tipo not in(\'PAGO_DIF\',\'PAGO_ESP\')  and p.fecha between ? and ?";
				List res=session.createQuery(hql)
				.setParameter(0, periodo.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, periodo.getFechaFinal(),Hibernate.DATE)
				.list();
				
				/*final EventList<Pago> source=GlazedLists.eventList(res);
				UniqueList<Pago> pagos=new UniqueList<Pago>(source,GlazedLists.beanPropertyComparator(Pago.class, "id"));				
				return pagos;
				*/
				return res;
			}
			
		});
	}
	
	public static void main(String[] args) {
		System.out.println("Data: "+ServiceLocator2.getCXCManager().getPagoDao().buscarPagosDisponibles(new Cliente("U050008","")).size());
	}

	
	
}
