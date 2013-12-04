package com.luxsoft.sw3.tasks.parches;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxc.model.PagoDeDiferencias;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

public class AjusteDeSaldos {
	
	
	public static void execute(){
		
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback(){

			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from Venta v where v.sucursal.id=? and " +
						" v.origen=\'CAM\' and v.fecha>?" +
						" order by v.fecha";
				ScrollableResults rs=session.createQuery(hql)
				.setLong(0, 3)
				.setParameter(1, DateUtil.toDate("15/12/2009"))
				.scroll();
			
				int buff=0;
				while(rs.next()){
					Venta v=(Venta)rs.get()[0];
					System.out.println("Saldando venta: "+v.getDocumento()+
							" Fecha:"+DateUtil.convertDateToString(v.getFecha())+"  Total: "+v.getTotal()+ "  Saldo:"+v.getSaldoCalculado());
					buff++;
					if(buff%20==0){
						session.flush();
						session.clear();
					}
				}
				System.out.println("Registros  procesados: "+rs.getRowNumber());
				return null;
			}
			
		});
		
		
		
	}
	
	private static PagoDeDiferencias generarPago(final Venta v){
		return null;
	}
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		execute();
	}

}
