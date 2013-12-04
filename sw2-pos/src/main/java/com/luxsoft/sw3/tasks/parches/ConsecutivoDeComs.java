package com.luxsoft.sw3.tasks.parches;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.compras.model.RecepcionDeCompra;
import com.luxsoft.sw3.services.Services;

public class ConsecutivoDeComs {
	
	
	public static void execute(){
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				long inicial=8120L;
				ScrollableResults rs=session.createQuery("from RecepcionDeCompra r order by r.id")
				.scroll();
				while(rs.next()){
					RecepcionDeCompra r=(RecepcionDeCompra)rs.get()[0];
					r.setDocumento(inicial);
					for(EntradaPorCompra e:r.getPartidas()){
						e.setDocumento(r.getDocumento());
					}
					inicial++;
				}
				return null;
			}
			
		});
	}
	
	public static void main(String[] args) {
		execute();
	}

}
