package com.luxsoft.siipap.cxp.parches;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxp.model.CXPUtils;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Acutaliza el vencimiento de los contrarecibos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class Parche2 {
	
	
	public void execute(){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from ContraReciboDet r left join fetch r.recibo";
				ScrollableResults rs=session.createQuery(hql)
				.scroll();
				while(rs.next()){
					ContraReciboDet det=(ContraReciboDet)rs.get()[0];
					det.setVencimiento(CXPUtils.calcularVencimiento(det.getRecibo().getFecha(), det.getFecha(), det.getRecibo().getProveedor()));
				}
				return null;
			}
			
		});
	}
	
	
	public static void main(String[] args) {
		new Parche2().execute();
	}

}
