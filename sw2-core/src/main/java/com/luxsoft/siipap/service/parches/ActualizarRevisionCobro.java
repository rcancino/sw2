package com.luxsoft.siipap.service.parches;

import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.rules.RevisionDeCargosRules;
import com.luxsoft.siipap.model.core.ClienteCredito;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;

/**
 * Actualiza fechas de revision y cobro para las ventas de Credito
 * 
 * En un futuro esta se debe ejecutar desde la aplicacion
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ActualizarRevisionCobro {
	
	public void execute(){
		final Date fecha=ServiceLocator2.obtenerFechaDelSistema();
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				final String hql="from Cargo c left join fetch c.cobrador cc " +
				"  where c.origen=\'CRE\' " +
				"  and c.fecha>?" +
				"  and (c.total-c.aplicado)!=0 " +
				" order by c.nombre";
				ScrollableResults rs=session.createQuery(hql)
				.setParameter(0, DateUtil.toDate("31/12/2008"),Hibernate.DATE)
				.scroll();
				int count=0;
				while(rs.next()){
					Cargo c=(Cargo)rs.get()[0];
					System.out.println("Procesando cargo: "+c);
					ClienteCredito credito=c.getCliente().getCredito();					
					if(credito==null){
						System.out.println("Venta a credito sin cliente credito: "+c.getNombre()+ " "+c.getClave());
						continue;
					}
					RevisionDeCargosRules.instance().actualizar(c, fecha);
					count++;
					if(count%20==0){
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
		new ActualizarRevisionCobro().execute();
		
	}

}
