package com.luxsoft.siipap.dao.parches;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.service.ServiceLocator2;

public class Fix2 {
	
	
	
	public static void test1(){
		HibernateTemplate t=new HibernateTemplate(ServiceLocator2.getSessionFactory());
		t.execute(new HibernateCallback(){
			public Object doInHibernate(Session session)					throws HibernateException, SQLException {
				//GFacturaPorCompra fac=(GFacturaPorCompra)session.get(GFacturaPorCompra.class, 256859L);
				ScrollableResults rs=session.createQuery("from Requisicion").scroll();
				while(rs.next()){
					System.out.println(rs.get(0));
				}				
				return null;
			}
			
		});
		
	}
	
	public static void main(String[] args) {
		test1();
	}

}
