package com.luxsoft.sw3.tasks.parches;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;

public class ArreglarConsecutivos {
	
	
	public void execute(){
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from Venta v where " +
						" v.sucursal.id=3 " +
						" and v.pedido is not null" +
						" and v.origen=\'MOS\'" +
						" order by v.log.creado"; 
				ScrollableResults rs=session.createQuery(hql)
				.scroll();
				//long correcto=28112;//CAM
				long correcto=75308l; //MOS
				//long correcto=0;
				while(rs.next()){					
					Venta v=(Venta)rs.get()[0];
					long actual=v.getDocumento();
					System.out.println("Fecha: "+DateUtil.convertDateToString(v.getFecha())+ "  Actual_:"+actual+ " Correcto: "+correcto+ " Venta "+v.getId());
					correcto++;
					v.setDocumento(correcto);
					v.setNumeroFiscal((int)correcto);
						
				}
				return null;
			}
			
		});
	}
	
	public static void main(String[] args) {
		new ArreglarConsecutivos().execute();
	}

}
