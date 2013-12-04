package com.luxsoft.siipap.cxp.dao;

import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.list.SetUniqueList;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.cxp.model.CXPCargo;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.CXPAnalisisDet;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;

public class FacturaDaoImpl extends GenericDaoHibernate<CXPFactura, Long> implements FacturaDao{

	public FacturaDaoImpl() {
		super(CXPFactura.class);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public CXPFactura save(CXPFactura object) {
		object.setSaldo(object.getSaldoCalculado());
		for(CXPAnalisisDet det:object.getPartidas()){
			det.actualizarInventario();
		}
		return super.save(object);
	}
	

	@Override
	public void remove(final Long id) {
		getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				CXPFactura fac=get(id);
				if(fac.getRecibo()!=null){
					fac.getRecibo().setCargoAbono(null);
					fac.setRecibo(null);
				}
				session.delete(fac);
				return null;
			}			
		});
	}

	public List<CXPFactura> buscarFacturas(final Proveedor proveedor) {
		
		return null;
	}

	public List<CXPFactura> buscarFacturas(final Periodo p) {
		final String hql="from CXPFactura f where f.fecha between ? and ?";
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				return session.createQuery(hql)
				.setParameter(0, p.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, p.getFechaFinal(),Hibernate.DATE)
				.list();
			}
			
		});
	}

	public List<CXPFactura> buscarFacturasPendientes() {
		String hql="from CXPFactura f left join fetch f.partidas where f.saldo!=0";
		List<CXPFactura> res= getHibernateTemplate().find(hql);
		SetUniqueList list=SetUniqueList.decorate(res);
		return list;
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.compras.dao.FacturaDao#buscarPorAnalisis(java.lang.Long)
	 */
	public CXPFactura buscarPorAnalisis(Long analisisId) {
		String hql="from CXPFactura f left join fetch f.partidas p where f.siipapAnalisis=?";
		List<CXPFactura> res=getHibernateTemplate().find(hql, analisisId);
		return res.isEmpty()?null:res.get(0);
	}
	
	/**
	 * Regresa el detalle del analisis para la factura indicada
	 * 
	 * @param factura
	 * @return
	 */
	public List<CXPAnalisisDet> buscarAnalisis(final CXPFactura factura){
		return getHibernateTemplate().find("from CXPAnalisisDet d where d.factura.id=?", factura.getId());
	}
	
	/**
	 * Busca todas las facturas con pagos pendientes
	 * 
	 * @param proveedor
	 * @param moneda
	 * @return
	 */
	public List<CXPFactura> buscarFacturasPorRequisitar(final Proveedor proveedor,final Currency moneda){
		final String hql="from CXPFactura f " +
				" where f.proveedor.id=? " 
				+" and f.saldoReal>0 " 
				+" and f.moneda=?" +
						" and f.fecha>=?" +
						" order by f.vencimiento asc"
				;
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				return session.createQuery(hql)
				//.setEntity(0, proveedor)
				.setLong(0, proveedor.getId())
				.setParameter(1, moneda,Hibernate.CURRENCY)
				.setParameter(2, DateUtil.toDate("01/01/2008"),Hibernate.DATE)
				.list();
			}
			
		});
	}

	public List<CXPCargo> buscarCuentasPorPagar(final Proveedor proveedor,final Currency moneda) {
		final String hql="from CXPFactura f " +
		" where f.proveedor.id=? " 
		+" and f.saldoReal>0 " 
		+" and f.moneda=?" +
				" order by f.vencimiento asc"
		;
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				List<CXPCargo> cargos= session.createQuery(hql)
				//.setEntity(0, proveedor)
				.setLong(0, proveedor.getId())
				.setParameter(1, moneda,Hibernate.CURRENCY)
				.list();
				// Mientras encontramos la manera de mantener sincronizado el saldo
				CollectionUtils.filter(cargos, new Predicate(){
					public boolean evaluate(Object object) {
						CXPCargo cargo=(CXPCargo)object;
						return cargo.getSaldoCalculado().doubleValue()>0;
					}
				});
				return cargos;
			}
	
		});
	}
	
	
	
	public static void actualizarSaldo(){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				ScrollableResults rs=session.createQuery("from CXPCargo c where year(c.fecha)>2008")
				.scroll();
				while(rs.next()){
					CXPCargo c=(CXPCargo)rs.get()[0];
					c.setSaldo(c.getSaldoCalculado());
				}
				return null;
			}
			
		});
	}
	
	
	public static void main(String[] args) {
		//actualizarSaldo();
		CXPFactura factura=CXPServiceLocator.getInstance().getFacturasManager().get(14021L);
		System.out.println("Cargos: "+factura.getTotalCargos());
		System.out.println("Por Req: "+factura.getPorRequisitar());
		System.out.println("DF: "+factura.getImporteDescuentoFinanciero());
//		Proveedor p=ServiceLocator2.getProveedorManager().buscarPorClave("C003");
//		List<CXPFactura> facs=CXPServiceLocator.getInstance().getFacturasManager().buscarFacturasPorRequisitar(p, MonedasUtils.PESOS);
//		for(CXPFactura fac:facs){
//			System.out.println("Fac: "+fac.getId()+" Saldo Real: "+fac.getSaldoReal()+" Sal Cal:"+fac.getSaldoCalculado()+" Saldo: "+fac.getSaldo()+ "Vto: "+DateUtil.getDate(fac.getVencimiento()));
//		}
	}

}
