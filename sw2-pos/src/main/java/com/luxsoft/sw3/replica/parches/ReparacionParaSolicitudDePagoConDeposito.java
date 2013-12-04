package com.luxsoft.sw3.replica.parches;

import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

public class ReparacionParaSolicitudDePagoConDeposito {
	
	public static void execute(){
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				ScrollableResults rs=session.createQuery("from SolicitudDeDeposito s " +
						"where DAY(s.fecha)=?" +
						"  and MONTH(s.fecha)=?" +
						"  and YEAR(s.fecha)=?" +
						"")
				.setParameter(0, 27)
				.setParameter(1, 10)
				.setParameter(2, 2010)
				.scroll();
				while(rs.next()){
					SolicitudDeDeposito sol=(SolicitudDeDeposito)rs.get()[0];
					System.out.println("Solicitud: "+sol);
				}
				return null;
			}
			
		});
	}
	
	public static void main(String[] args) {
		execute();
	}

}
