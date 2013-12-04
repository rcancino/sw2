package com.luxsoft.siipap.cxc.util;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;

/**
 * Pruebas de Hibernate query
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class HQLQueryPruebas {
	
	public static void main(String[] args) {
		/*String hql="select x.devolucion from DevolucionDeVenta x " +
		" where  x.nota is null" +
		" and (x.devolucion.venta.total-x.devolucion.venta.devoluciones)>5 ";
		
		String hql2="select d.devolucion from DevolucionDeVenta d " +
				//"left join fetch d.ventaDet vdet " +
				" where d.nota is null " +
				"  and d.ventaDet.cantidad+d.ventaDet.devueltas)<0 ";
		
		String hql3="from Devolucion d where " +
				"d  not in(select nd.devolucion from NotaDeCreditoDevolucion nd)";
		List<Devolucion> devs=ServiceLocator2.getHibernateTemplate().find(hql3);
		for(Devolucion d:devs){
			System.out.println(d);
		}*/
		DBUtils.whereWeAre();
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				
				Cliente c=(Cliente)session.get(Cliente.class, new Long(4722L));
				System.out.println("Cliente: "+c);
				
				ScrollableResults rs=session.createQuery("from NotaDeCargo nc ")
				.scroll();
				while(rs.next()){
					NotaDeCargo nc=(NotaDeCargo)rs.get()[0];
					System.out.println("Procesando: "+nc);
					                                     
				}
				return null;
			}
			
		});
		
	}

}
