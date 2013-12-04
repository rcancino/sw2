package com.luxsoft.siipap.cxp.parches;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;

/**
 * Actualiza el precio neto en el detalle de las listas de precios
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class Parche4 {
	
	
	public void execute(){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from ListaDePreciosDet a";
				ScrollableResults rs=session.createQuery(hql)
				.scroll();
				int buff=0;
				while(rs.next()){
					ListaDePreciosDet det=(ListaDePreciosDet)rs.get()[0];
					det.setNeto(det.getCosto().amount());
					System.out.println("Acutalizado: "+det);
					while(buff++%20==0){
						session.flush();
						session.clear();
					}
				}
				return null;
			}
			
		});
	}
	
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		new Parche4().execute();
	}

}
