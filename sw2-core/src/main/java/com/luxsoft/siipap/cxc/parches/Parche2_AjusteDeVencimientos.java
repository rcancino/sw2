package com.luxsoft.siipap.cxc.parches;

import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;

public class Parche2_AjusteDeVencimientos {
	
	public static void execute(){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from Venta c left join fetch c.cobrador cc " +
					"  where c.origen=\'CRE\' " +
					"    and (c.total-c.aplicado)!=0 " +
					"  	and c.fecha>?" +
					" order by c.clave desc";
				ScrollableResults rs=session.createQuery(hql).setParameter(0, DateUtil.toDate("31/12/2008"),Hibernate.DATE)
				.scroll();
				int buff=0;
				while(rs.next()){
					
					Venta cargo=(Venta)rs.get()[0];
					if(cargo.getCliente().getCredito()==null){
						System.out.println("Venta sin cliente credito");
						continue;
					}
					cargo.setPlazo(cargo.getCliente().getCredito().getPlazo());
					cargo.setFechaRevision(cargo.getFecha());
					
					int plazo=cargo.getPlazo();
					int tolerancia=0;
					if(cargo.getCliente().getCredito()!=null){
						if(!cargo.getCliente().getCredito().isVencimientoFactura())
							tolerancia=7;
					}
					final Date vto=DateUtils.addDays(cargo.getFecha(), plazo+tolerancia);
					cargo.setVencimiento(vto);
					System.out.println("Procesando venta: "+cargo.getDocumento()+" Cliente: "+cargo.getNombre());
					
					if((++buff)%20==0){
						session.flush();
						session.clear();
					}
				}
				return null;
			}
			
		});
	}
	
	
	public static void main(String[] args) {
		execute();
	}

}
