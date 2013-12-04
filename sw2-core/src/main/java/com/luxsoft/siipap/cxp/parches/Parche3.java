package com.luxsoft.siipap.cxp.parches;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxp.model.CXPAnalisisDet;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;

/**
 * Corrige la cantidad correcta de los analis importados
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class Parche3 {
	
	
	public void execute(){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from CXPAnalisisDet a left join fetch a.entrada b where a.siipapId is not null";
				ScrollableResults rs=session.createQuery(hql)
				.scroll();
				int buff=0;
				while(rs.next()){
					CXPAnalisisDet det=(CXPAnalisisDet)rs.get()[0];
					double cantidad=det.getCantidad();
					double fix=cantidad*det.getEntrada().getFactor();
					System.out.println("Det: "+det.getId()+" Cant:"+cantidad+ "   Fix: "+fix+  "Factor: "+det.getEntrada().getFactor());
					det.setCantidad(fix);
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
		new Parche3().execute();
	}

}
