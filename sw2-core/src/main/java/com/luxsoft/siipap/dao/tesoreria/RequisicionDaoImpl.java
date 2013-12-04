package com.luxsoft.siipap.dao.tesoreria;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.collections.list.SetUniqueList;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;

/**
 * Implementacion de {@link RequisicionDao}
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public class RequisicionDaoImpl extends GenericDaoHibernate<Requisicion, Long> implements RequisicionDao{

	public RequisicionDaoImpl() {
		super(Requisicion.class);
	}
	
	@Override
	public Requisicion get(Long id) {
		List<Requisicion> data= getHibernateTemplate().find("from Requisicion r left join fetch r.partidas p where r.id=?",id);
		if(data.isEmpty()){
			throw new ObjectRetrievalFailureException(Requisicion.class,id);
		}
		return data.get(0);
	}

	

	public List<Requisicion> buscarRequisicionesDeGastos() {
		List res=getHibernateTemplate().find(
				"from Requisicion r left join fetch r.partidas p where r.origen=?"
				, "GASTOS");
		return SetUniqueList.decorate(res);
	}

	public List<RequisicionDe> buscarAnticiposDisponibles(final GProveedor proveedor){		
		return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery("from RequisicionDe r " +
						" left join fetch r.requisicion rm " +
						" where r.requisicion.afavor=? and r.facturaDeGasto is null")
				.setString(0, proveedor.getNombreRazon())
				.list();
				
			}
			
		});
	}

	public List<Requisicion> buscarRequisicionesDeGastos(final Periodo p) {
		List res=getHibernateTemplate().find(
				"from Requisicion r left join fetch r.partidas p where r.origen=?" +
				"  and r.fecha between ? and ?"
				, new Object[]{"GASTOS",p.getFechaInicial(),p.getFechaFinal()});
		return SetUniqueList.decorate(res);
	}

	
	/**
	 * Regresa una lista de las requisiciones elaboradas en el modulo de compras
	 * en el periodo indicado
	 * 
	 * @param p
	 * @return
	 */
	public List<Requisicion> buscarRequisicionesDeCompras(final Periodo p){
		List res=getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				return session.createQuery("from Requisicion r " +
						" where r.origen=?" +
						"   and r.fecha between ? and ?"						
						).setString(0, "COMPRAS")
						.setParameter(1, p.getFechaInicial(),Hibernate.DATE)
						.setParameter(2,p.getFechaFinal(),Hibernate.DATE)
						.list();
			}
			
		});
		System.out.println("Reqs: "+res.size()+ "Per: "+p);
		return res;
	}

	public Requisicion buscarRequisicionDeCompras(final Long id) {
		return (Requisicion)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Requisicion r=(Requisicion)session.load(Requisicion.class, id);
				for(RequisicionDe det:r.getPartidas()){
					det.getFacturaDeCompras().getFecha();
				}
				return r;
			}
			
		});
	}
	
	
}
