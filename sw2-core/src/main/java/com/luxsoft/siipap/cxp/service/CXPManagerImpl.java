package com.luxsoft.siipap.cxp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Currency;
import java.util.Date;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;

public class CXPManagerImpl extends HibernateDaoSupport implements CXPManager{
	
	
	/**
	 * Regresa el saldo de un proveedor a la fecha(no inclusiva) indicado
	 *  Es decir incluye movimientos anteriores a la fecha indicada
	 *  
	 * @param p
	 * @param fecha La fecha de corte, No inclusiva.
	 * @return
	 */
	public BigDecimal getSaldo(final Proveedor p,final Date fecha){
		return (BigDecimal) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				Number cargos=(Number)session.createQuery("select sum(c.total*c.tc) " +
						" from CXPCargo c " +
						"where c.proveedor.id=? " +
						"  and c.fecha<?")
				.setLong(0, p.getId())
				.setParameter(1, fecha,Hibernate.DATE)
				.uniqueResult();
				logger.info("Cargos: "+BigDecimal.valueOf(cargos.doubleValue()).setScale(2,RoundingMode.HALF_EVEN));
				Number abonos=(Number)session.createQuery("select sum(c.total*c.tc) " +
						" from CXPAbono c " +
						"where c.proveedor.id=? " +
						"  and c.fecha<?")
				.setLong(0, p.getId())
				.setParameter(1, fecha,Hibernate.DATE)
				.uniqueResult();
				logger.info("Abonos: "+BigDecimal.valueOf(abonos.doubleValue()).setScale(2));
				return BigDecimal.valueOf(cargos.doubleValue()-abonos.doubleValue());
			}
			
		});
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.cxp.service.CXPManager#getSaldo(com.luxsoft.siipap.model.core.Proveedor, java.util.Currency, java.util.Date)
	 */
	public BigDecimal getSaldo(final Proveedor p, final Currency moneda,final Date fecha) {
		return (BigDecimal) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				Number cargos=(Number)session.createQuery("select sum(c.total) " +
						" from CXPCargo c " +
						"where c.proveedor.id=? " +
						"  and c.fecha<?" +
						"  and c.moneda=?")
				.setLong(0, p.getId())
				.setParameter(1, fecha,Hibernate.DATE)
				.setParameter(2, moneda,Hibernate.CURRENCY)
				.uniqueResult();
				logger.info("Cargos: "+BigDecimal.valueOf(cargos.doubleValue()).setScale(2,RoundingMode.HALF_EVEN));
				Number abonos=(Number)session.createQuery("select sum(c.total) " +
						" from CXPAbono c " +
						"where c.proveedor.id=? " +
						"  and c.fecha<?" +
						"  and c.moneda=?")
				.setLong(0, p.getId())
				.setParameter(1, fecha,Hibernate.DATE)
				.setParameter(2, moneda,Hibernate.CURRENCY)
				.uniqueResult();
				logger.info("Abonos: "+BigDecimal.valueOf(abonos.doubleValue()).setScale(2));
				return BigDecimal.valueOf(cargos.doubleValue()-abonos.doubleValue());
			}
			
		});
	}



	public static void main(String[] args) {
		DBUtils.whereWeAre();
		Date fecha=DateUtil.toDate("01/02/2009");
		Proveedor p=new Proveedor("");
		p.setId(new Long(36));
		BigDecimal res=CXPServiceLocator.getInstance().getCXPManager().getSaldo(p, fecha);
		System.out.println(res);
	}

}
