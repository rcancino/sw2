package com.luxsoft.sw3.services.parches;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;

public class AjustesRMD2013 {
	
	public void run(){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				
				List<Devolucion> rmds=session.createQuery("from Devolucion d where date(d.fecha)=?")
						.setParameter(0, DateUtil.toDate("31/12/2013"),Hibernate.DATE)
						.list();
				for(Devolucion d:rmds){
					for(DevolucionDeVenta det:d.getPartidas()){
						det.setDocumento(d.getNumero());
					}
				}
				
				return null;
			}
		});
	}
	
	public static void main(String[] args) {
		new AjustesRMD2013().run();
	}

}
