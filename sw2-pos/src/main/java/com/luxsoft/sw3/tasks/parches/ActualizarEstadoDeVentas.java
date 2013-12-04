package com.luxsoft.sw3.tasks.parches;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.EstadoDeVenta;

public class ActualizarEstadoDeVentas {
	
	
	public static void execute(){
		//final User admin=KernellSecurity.instance().findUser("sysadmin", Services.getInstance().getHibernateTemplate());
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback(){

			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from Venta v where v.sucursal.id=?  " +
						" and date(v.fecha)>=?" +
						" order by v.fecha";
				ScrollableResults rs=session.createQuery(hql)
				.setLong(0, 3)
				.setParameter(1, DateUtil.toDate("21/10/2013"))
				.scroll();
			
				int buff=0;
				while(rs.next()){
					Venta v=(Venta)rs.get()[0];
					System.out.println("Generando estado para : "+v.getDocumento()+
							" Fecha:"+DateUtil.convertDateToString(v.getFecha())+"  Total: "+v.getTotal()+ "  Saldo:"+v.getSaldoCalculado());
					EstadoDeVenta e=new EstadoDeVenta(v);
					//KernellSecurity.instance().registrarUserLog(e, "log");
					//KernellSecurity.instance().registrarAddressLog(e, "addresLog");
					try {
						session.save(e);
						session.flush();
						session.clear();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
					
					
				}
				System.out.println("Registros  procesados: "+rs.getRowNumber());
				return null;
			}
			
		});
		
	}
	
	
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		execute();
	}

}
